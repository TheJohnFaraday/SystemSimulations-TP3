import argparse
import os
from dataclasses import dataclass

import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import seaborn as sns

# ------------------------------

CUSTOM_PALETTE = [
    "#508fbe",  # blue
    "#f37120",  # orange
    "#4baf4e",  # green
    "#f2cb31",  # yellow
    "#c178ce",  # purple
    "#cd4745",  # red
    "#9ef231",  # light green
    "#50beaa",  # green + blue
    "#8050be",  # violet
    "#cf1f51",  # magenta
]
GREY = "#6f6f6f"
LIGHT_GREY = "#bfbfbf"

PLT_THEME = {
    "axes.prop_cycle": plt.cycler(color=CUSTOM_PALETTE),  # Set palette
    "axes.spines.top": False,  # Remove spine (frame)
    "axes.spines.right": False,
    "axes.spines.left": True,
    "axes.spines.bottom": True,
    "axes.edgecolor": LIGHT_GREY,
    "axes.titleweight": "normal",  # Optional: ensure title weight is normal (not bold)
    "axes.titlelocation": "center",  # Center the title by default
    "axes.titlecolor": GREY,  # Set title color
    "axes.labelcolor": GREY,  # Set labels color
    "axes.labelpad": 12,
    "axes.titlesize": 10,
    "xtick.bottom": False,  # Remove ticks on the X axis
    "ytick.labelcolor": GREY,  # Set Y ticks color
    "ytick.color": GREY,  # Set Y label color
    "savefig.dpi": 128,
    "legend.frameon": False,
    "legend.labelcolor": GREY,
    "figure.titlesize": 16,  # Set suptitle size
}
plt.style.use(PLT_THEME)
sns.set_palette(CUSTOM_PALETTE)
sns.set_style(PLT_THEME)

DPI = 100
FIGSIZE = (1920 / DPI, 1080 / DPI)

# ------------------------------


@dataclass
class SimulationParameters:
    r_container: float
    n_particles: int
    r_obstacle: float
    m_obstacle: float | None
    seed: int
    perimeter_container: float
    perimeter_obstacle: float


def format_power_of_10(x):
    if x == 0:
        return "0"

    # Get the base and exponent
    base, exp = f"{x:.2e}".split("e")
    base = float(base)
    exp = int(exp)

    # Check if the decimal part is zero (e.g. 1.00 → 1)
    if round(base % 1, 2) == 0:
        base_str = f"{int(base)}"
    else:
        base_str = f"{base:.2f}"

    return f"{base_str}x10^{exp}"


def y_fmt(x, pos):
    """Format number as power of 10"""
    return format_power_of_10(x)


def plot_pressure(pressure_df: pd.DataFrame, output_dir: str):
    plot_df = pressure_df.reset_index().rename(
        columns={"pressure_container": "container", "pressure_obstacle": "obstacle"}
    )
    unified_plot_df = pressure_df.reset_index().melt(  # index to column
        id_vars="time",
        value_vars=["pressure_container", "pressure_obstacle"],
        var_name="boundary",
        value_name="pressure_Pa",
    )

    # Unified plot
    plt.figure(figsize=FIGSIZE)
    sns.lineplot(
        data=unified_plot_df,
        x="time",
        y="pressure_Pa",
        hue="boundary",
        style="boundary",
    )
    plt.xlabel("Tiempo (t)", fontsize=14)
    plt.ylabel("Presión (Pa)", fontsize=14)
    plt.grid(True)
    # plt.show()
    plt.savefig(f"./{output_dir}/pressure" ".png")
    plt.clf()
    plt.close()

    # Container only
    plt.figure(figsize=FIGSIZE)
    sns.lineplot(data=plot_df, x="time", y="container")
    plt.xlabel("Tiempo (t)", fontsize=14)
    plt.ylabel("Presión (N/m²)", fontsize=14)
    plt.grid(True)
    # plt.show()
    plt.savefig(f"./{output_dir}/pressure" "_container" ".png")
    plt.clf()
    plt.close()

    # Obstacle only
    plt.figure(figsize=FIGSIZE)
    sns.lineplot(data=plot_df, x="time", y="obstacle")
    plt.xlabel("Tiempo (t)", fontsize=14)
    plt.ylabel("Presión (N/m²)", fontsize=14)
    plt.grid(True)
    # plt.show()
    plt.savefig(f"./{output_dir}/pressure" "_obstacle" ".png")
    plt.clf()
    plt.close()


def calculate_j(sim_params: SimulationParameters, snap: pd.DataFrame):
    hit_container = (snap["v_n"] > 0) & (
        snap["r"] + snap["radius"] >= sim_params.r_container
    )
    hit_obstacle = (snap["v_n"] < 0) & (
        snap["r"] - snap["radius"] <= sim_params.r_obstacle
    )
    j_container = (2 * snap["m"] * np.abs(snap["v_n"][hit_container])).sum()
    j_obstacle = (2 * snap["m"] * np.abs(snap["v_n"][hit_obstacle])).sum()
    return {"container": j_container, "obstacle": j_obstacle}


def calculate_pressure(sim_params: SimulationParameters, df: pd.DataFrame):
    # P = J / (delta t * L)

    # sorted list of unique times in the file
    times = np.sort(df.index.unique())

    # Δt_k  =  t_k  -  t_{k-1}   (Δt_0 is measured from t=0.0)
    delta_t = np.diff(np.concatenate(([0.0], times)))

    records = []
    for dt, t in zip(delta_t, times):
        j = calculate_j(sim_params, df.loc[t])

        P_cont = j["container"] / (dt * sim_params.perimeter_container)  # Pa
        P_obs = j["obstacle"] / (dt * sim_params.perimeter_obstacle)  # Pa

        # Skip times without pressure
        # if P_cont == 0.0 and P_obs == 0.0:
        #     continue

        records.append(
            {
                "time": t,
                "pressure_container": P_cont,
                "pressure_obstacle": P_obs,
            }
        )

    pressure_df = pd.DataFrame.from_records(records).set_index("time").sort_index()
    return pressure_df


def read_csv(filepath: str):
    config_df = pd.read_csv(filepath, nrows=1, header=0, keep_default_na=False)
    config = SimulationParameters(
        r_container=float(config_df["L/2"][0]),
        n_particles=int(config_df["n"][0]),
        r_obstacle=float(config_df["R"][0]),
        m_obstacle=(
            None if config_df["m_R"][0] == "null" else float(config_df["m_R"][0])
        ),
        seed=int(config_df["seed"][0]),
        perimeter_container=(2 * np.pi * config_df["L/2"][0]),
        perimeter_obstacle=(2 * np.pi * config_df["R"][0]),
    )

    df = pd.read_csv(
        filepath,
        sep=",",  # separator (default is comma)
        header=0,  # use first row as header
        index_col=None,  # don't use any column as index
        skiprows=2,  # number of rows to skip
    )
    df.set_index("time", inplace=True)

    return config, df


def main(output_file: str, fixed_obstacle: bool):
    input_dir = "./output"
    output_base_dir = "./analysis"
    os.makedirs(output_base_dir, exist_ok=True)

    simulation_params, df = read_csv(f"{input_dir}/{output_file}")
    print(df)

    pressure_df = calculate_pressure(simulation_params, df)
    plot_pressure(pressure_df, output_base_dir)
    print(pressure_df.head())

    count_hits = 0
    count_assisted_hits = 0
    for index, row in df.iterrows():
        vn = row["v_n"] < 0
        len_val = row["r"] - row["radius"] - simulation_params.r_obstacle
        hit = len_val <= 0.0
        if hit:
            count_hits += 1
        elif vn and len_val < 1e-5:
            count_assisted_hits += 1
    print(f"{count_hits = }\t{count_assisted_hits = }")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        description="Parse Kotlin output file and generate animations and plots."
    )
    parser.add_argument(
        "-o",
        "--fixed_obstacle",
        action="store_true",
        help="If present, it will assume that the obstacle is fixed at the center, otherwise it will use the last particle index as the obstacle to run calculations",
    )
    parser.add_argument(
        "-f", "--output_file", type=str, required=True, help="Output file to animate"
    )

    args = parser.parse_args()

    main(
        output_file=args.output_file,
        fixed_obstacle=True if args.fixed_obstacle else False,
    )
