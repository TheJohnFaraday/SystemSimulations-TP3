import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from matplotlib.animation import FuncAnimation
import matplotlib.patches as patches
import argparse
from typing import Union
import logging

parser = argparse.ArgumentParser(description="Parse Kotlin output file and generate animations and plots.")
parser.add_argument("-o", "--fixed_obstacle", action='store_true',
                      help="Animate with a fixed obstacle")
parser.add_argument("-f", "--output_file", type=str, required=True,
                      help="Output file to animate")

args = parser.parse_args()

MAX_DESIRED_FPS = 24

PARTICLE_RADIUS = 0.0005
BOARD_RADIUS = 0.05
OBSTACLE_RADIUS = 0.005 if args.fixed_obstacle else None
output_file = args.output_file
print(f"OBSTACLE_RADIUS: {OBSTACLE_RADIUS}")

df = pd.read_csv(f'./output/{output_file}', 
                 sep=',',           # separator (default is comma)
                 header=0,          # use first row as header
                 index_col=None,    # don't use any column as index
                 skiprows=0)        # number of rows to skip 


print(df)

# Set Time as index
df.set_index('time', inplace=True)

# Get unique times
times = df.index.unique()

# Calculate interval to make animation last exactly 15 seconds
TOTAL_DURATION = 15  # seconds
interval = (TOTAL_DURATION * 1000) / len(times)  # convert to milliseconds

# Create figure and axis
fig, ax = plt.subplots(figsize=(10, 10))
ax.set_xlim(-BOARD_RADIUS - 0.1, BOARD_RADIUS + 0.1)
ax.set_ylim(-BOARD_RADIUS - 0.1, BOARD_RADIUS + 0.1)
ax.set_xlabel('X Position')
ax.set_ylabel('Y Position')
ax.set_title('Particle System Animation')
ax.set_aspect('equal')

# Initialize list to store circle patches
circles = []
quiver = None
board_circle = None
obstacle_circle = None

def init():
    # Clear any existing circles
    for circle in circles:
        circle.remove()
    circles.clear()
    
    # Get initial data for quiver plot
    initial_data = df.loc[times[0]]
    x = initial_data['x'].values
    y = initial_data['y'].values
    vx = initial_data['vx'].values
    vy = initial_data['vy'].values
    
    """
    # Initialize quiver plot with initial data
    # The quiver is used to represent vectors in the plot
    global quiver
    if quiver is not None:
        quiver.remove()
    quiver = ax.quiver(x, y, vx, vy, color='red', alpha=0.6, 
                      scale=20, scale_units='xy', angles='xy')
    """
    
    # Create board boundary (circumference)
    global board_circle
    board_circle = patches.Circle((0, 0), BOARD_RADIUS, 
                                fill=False, color='black', linewidth=2)
    ax.add_patch(board_circle)
    
    # Create central obstacle if radius is specified
    global obstacle_circle
    if OBSTACLE_RADIUS is not None:
        obstacle_circle = patches.Circle((0, 0), OBSTACLE_RADIUS,
                                       fill=True, color='gray', alpha=0.5)
        ax.add_patch(obstacle_circle)
    
    # Return all artists that need to be updated
    artists = [quiver, board_circle]
    if OBSTACLE_RADIUS is not None:
        artists.append(obstacle_circle)
    return artists

def update(frame):
    # Get data for current time
    current_time = times[frame]
    time_data = df.loc[current_time]
    
    # Clear existing particle circles
    for circle in circles:
        circle.remove()
    circles.clear()

    """
    global quiver
    if quiver is not None:
        quiver.remove()
    """

    # Create new circles for each particle
    for _, particle in time_data.iterrows():
        if abs(particle['x']) > 0.1 or abs(particle['y']) > 0.1:
            print(f"Particle: ({particle['x']}, {particle['y']})")
        circle = patches.Circle((particle['x'], particle['y']), 
                              radius=PARTICLE_RADIUS,
                              fill=True, color='blue', alpha=0.6)
        """
        quiver = ax.quiver(particle['x'], particle['y'], particle['vx'], particle['vy'], color='red', alpha=0.6,
                      scale=20, scale_units='xy', angles='xy')
        """
        ax.add_patch(circle)
        circles.append(circle)
    
    # Update title with current time
    ax.set_title(f'Particle System Animation - Time: {current_time:.2f}')
    
    # Return all artists that need to be updated
    artists = circles + [quiver, board_circle]
    if OBSTACLE_RADIUS is not None:
        artists.append(obstacle_circle)
    return artists

# Create animation
ani = FuncAnimation(fig, update, frames=len(times),
                   init_func=init, blit=False,
                   interval=interval)

# Save the animation
print("Saving animation...")
ani.save(f'./animations/{output_file}-simulation.mp4', 
         writer='ffmpeg', 
         fps=min(len(times)/TOTAL_DURATION, MAX_DESIRED_FPS),
         dpi=100,
         extra_args=['-crf', '26', '-preset', 'veryfast'])  # smaller file, faster encode
print("Animation saved successfully!")

# Close the figure to free memory
plt.close(fig)