import matplotlib.pyplot as plt

x1 = [7.666666666666667,7,9,7]
y1 = [7.666666666666667,6,7,10]

x2=[17.75,16,25,17,13]
y2=[8.75,3,1,16,15]

x3=[1.4,1,3,2,2,-1]
y3=[-2.6,2,3,2,3,-23]

# plotting points as a scatter plot
plt.scatter(x1[0], y1[0], label= "Centroid1", color= "red",marker= "*", s=30)
plt.scatter(x1[1:], y1[1:], label= "Cluster1", color= "red",marker= ".", s=30)
plt.scatter(x2[0], y2[0], label= "Centroid2", color= "green",marker= "*", s=30)
plt.scatter(x2[1:], y2[1:], label= "Cluster2", color= "green",marker= ".", s=30)
plt.scatter(x3[0], y3[0], label= "Centroid3", color= "blue",marker= "*", s=30)
plt.scatter(x3[1:], y3[1:], label= "Cluster3", color= "blue",marker= ".", s=30)

 
# x-axis label
plt.xlabel('X - >')
# frequency label
plt.ylabel('Y - >')
# plot title
plt.title('K-Means Clustering Output')
# showing legend
plt.legend()
 
# function to show the plot
plt.show()