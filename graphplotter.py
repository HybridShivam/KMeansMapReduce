import pandas as pd
import matplotlib.pyplot as plt
from matplotlib.pyplot import cm
import csv
import numpy as np
import math

parent='out/100/m/7/'
cen = pd.read_csv(parent+'Centroids.csv', header=None)

k = len(cen)
centroids = []
p = []
ss = []
for i in range(k):
    p.append([[], []])
    ss.append(0)
with open(parent+'Centroids.csv', 'r') as cf:
    reader = csv.reader(cf, delimiter=',')
    for row in reader:
        centroids.append([float(row[0]), float(row[1])])
n = [0]*k
with open(parent+'Output.csv', 'r') as f:
    reader = csv.reader(f, delimiter=',')
    for row in reader:
        for i in range(k):
            x = float(row[0])
            y = float(row[1])
            x1 = float(row[2])
            y1 = float(row[3])
            if(x == centroids[i][0] and y == centroids[i][1]):
                n[i] = n[i]+1
                p[i][0].append(x1)
                p[i][1].append(y1)
                d1 = (x-x1)
                d2 = (y-y1)
                ss[i] = ss[i]+d1*d1+d2*d2
                break
wcss = sum(ss)
print("WCSS: "+ str(sum(ss)))
for i in range(k):
    ss[i] = math.sqrt(ss[i]/n[i])
# Mean Distortion
print("Mean Distortion: "+ str(sum(ss)/k))
# plotting points as a scatter plot
color = iter(cm.rainbow(np.linspace(0, 1, k)))
for i in range(k):
    c = next(color)
    ax = plt.gca()
    ax.add_patch(plt.Circle(
        (centroids[i][0],  centroids[i][1]), ss[i], facecolor='#FF000000', edgecolor=c))
    ax.set_aspect('equal')
    plt.scatter(p[i][0], p[i][1], color=c, marker=".", s=30)
    plt.scatter(centroids[i][0], centroids[i][1],
                label="Centroid " + str(i+1), color=c, marker="*", s=60)

# x-axis label
plt.xlabel('X (cm) - >')
# frequency label
plt.ylabel('Y (kg) - >')
# plot title
plt.title('K-Means Clustering Output')
# showing legend
plt.legend()

# function to show the plot
plt.show()
