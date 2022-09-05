from matplotlib import pyplot as plt
from matplotlib import style
from matplotlib.ticker import MaxNLocator
from numpy import genfromtxt

data = genfromtxt('Elbow.csv', delimiter=',', names=['x', 'y', 'z'])
y = 'z'
switcher = {
    'y': "WCSS ->",
    'z': "Distortion ->",
}
plt.plot(data['x'], data[y])
plt.title('Elbow Method')
plt.ylabel(switcher[y])
plt.xlabel('K ->')
plt.xticks(range(1, len(data['y'])+1))
plt.show()
