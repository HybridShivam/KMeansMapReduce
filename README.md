# K-Means Clustering using Hadoop

## Getting Started

1. Install hadoop, alternatively, follow steps 2-4 for hadoop installation in docker environment.
1. Launch a Linux VM on Windows; install and configure `docker` and `docker-compose` on the virtual machine.
1. Clone the [big-data-europe/docker-hadoop](https://github.com/big-data-europe/docker-hadoop) repo containing the docker compose file with all the necessary information of the Hadoop containers to be created.
1. Run the `docker-compose file` in the cloned directory with 
    ```
    docker-compose up -d
    ```
    
1. Export the `.jar` file of the project with `KMeansClusteringJob` Class as the main class and transfer it to the datanode container where it can be run on the Hadoop architecture.

1. Run the `.jar` file on Hadoop with the appropriate arguments. 

    ```
    hadoop KMeansMapReduce.jar input.txt m 7 0
    ```

    The various arguments created for the `.jar` file are:
    * Inputfile: Location of the input csv file.
    * Distanceformula : Character indicating the distance algorithm to be used;
        * m ; Manhattan Distance
        * e : Euclidian Distance
        * j : Jaccard Distance
    * K: No of cluster to be created
    * Error: Maximum amount of error in the convergence check
    * Debug: “y” for skipping all subsequent iterations and no file output.

1. Use the python scripts to visualize the data.


## Input DataSet (100,000)
![image](https://user-images.githubusercontent.com/28728749/188441186-d7b54404-de24-4ea8-b103-46f60292af67.png)

## Hadoop Result
![image](https://user-images.githubusercontent.com/28728749/188441228-79c74844-79b3-4f3b-a719-4b7638044d7c.png)

## Elbow Method for the optimal value of K

Elbow method for WCSS:

![image](https://user-images.githubusercontent.com/28728749/188441294-0127a64e-a1e5-4ae9-ad4c-e1aafbd5e64a.png)

Elbow method for Distortion:

![image](https://user-images.githubusercontent.com/28728749/188441331-2ba5af39-8b1a-40fb-9203-e223faccce85.png)

## Clustering Plot (Dataset100.csv)

###  K = 7 and Euclidian Distance
![image](https://user-images.githubusercontent.com/28728749/188441365-18fd5fa6-b25c-42e7-92ee-9a472db23e35.png)

###  K = 7 and Manhattan Distance


![image](https://user-images.githubusercontent.com/28728749/188441404-98dacd9c-c6d1-430d-9c27-0aadb33a7c0a.png)

###  K = 7 and Jaccard Distance

![image](https://user-images.githubusercontent.com/28728749/188441422-a25f56a0-3593-4e42-9c3b-28e4cc2a42e0.png)

<br>

# Documentation

## Folder Structure

The workspace contains two folders by default, where:

- `src`: the folder to maintain sources
- `lib`: the folder to maintain dependencies
- `out`: the folder containing the output from Previous MapReduce Jobs

Meanwhile, the compiled output files will be generated in the `bin` folder by default.

## Vector Class
This model class contains the definition for the Vectors that store the `key value` pairs. Height and Weight in the case of my project. The `Vector` class implements `WritableComparable<Vector>`.

```
//Imports//
public class Vector implements WritableComparable<Vector> {
    private double[] vector;
    ----
}
```

## DistanceMeasurer Class

We need a measurement of a distance between two vectors, especially between a center and a vector. The class containers multiple implementations of multiple Distance metric algorithms including Euclidian Distance, Manhattan Distance and Jaccard Distance. The selected Distance Metric is passed as an command line argument that is passed to the measureDistance() method as an argument distanceFormula.

```
//Imports//
public class DistanceMeasurer {
    public static final double measureDistance(ClusterCenter center, Vector v) {
        double sum = 0;
        int length = v.getVector().length;
        char distanceFormula = KMeansClusteringJob.distanceFormula;
        switch (distanceFormula) {
            case 'm':
                // Manhattan
                for (int i = 0; i < length; i++) {
                    sum += Math.abs(center.getCenter().getVector()[i] - v.getVector()[i]);
                }
                return sum;
            case 'e':
                // Euclidian
                for (int i = 0; i < length; i++) {
                    double diff = center.getCenter().getVector()[i] - v.getVector()[i];
                    // multiplication is faster than Math.pow() for ^2.
                    sum += (diff * diff);
                }
                return Math.sqrt(sum);
            case 'j':
                // Jaccard
                double[] v1 = center.getCenter().getVector();
                double[] v2 = v.getVector();
                double dot = dot(v1, v2);
                double set1Length = sumOfSquares(v1);
                double set2Length = sumOfSquares(v2);
                return 1.0d - (dot / (set1Length + set2Length - dot));
            default:
                // Manhattan
                for (int i = 0; i < length; i++) {
                    sum += Math.abs(center.getCenter().getVector()[i] - v.getVector()[i]);
                }
                return sum;
        }
    }
private static double dot(double[] v1, double[] v2) { --- }
private static double sumOfSquares(double[] v1) { --- }
}
```

## ClusterCenter Class
The ClusterCenter class also encapsulates a Vector class object along with an integer Cluster Index for unique indentification of each cluster center. It just delegates the read/write/compareTo method to the vector. It has a converged method which checks if the distance between the class vector and the passed argument vector is within the specified allowable error value.

```
//Imports//
public class ClusterCenter implements WritableComparable<ClusterCenter> {
    private Vector center;
    private int clusterIndex;
    ----
}
```

## KMeansMapper Class
The Mapper class is responsible for the mapping phase of the program.
Let's assume that there is a list or a list-like sequencefile-iterating interface that is called centers. It contains ClusterCenter objects that represent the current centers. The DistanceMeasurer class contains the static method we defined in the last part.

```
@SuppressWarnings("deprecation")
public class KMeansMapper extends
        Mapper<ClusterCenter, Vector, ClusterCenter, Vector> {
    private static final Log LOG = LogFactory.getLog(KMeansMapper.class);
    private final List<ClusterCenter> centers = new LinkedList<ClusterCenter>();
    @Override
    protected void setup(Context context) throws IOException, InterruptedException { --- }
    @Override
    protected void map(ClusterCenter key, Vector value, Context context) throws IOException, InterruptedException { --- }
}
```

## KMeansReducer Class
The Reducer Class is responsible for the reducer phase of the compiler.
Once again let's have a list or a list-like sequencefile-iterating interface that is called centers. The first loop only dumps the values in the iterable into a list and sums up each component of the vector in a newly created center. Then we are averaging it in another loop and we are writing the new center along with each vector we held in memory the whole time. Afterwards we are just checking if the vector has changed, this method is just a delegating to the underlying vectors compareTo. If the centers are not equal it returns true. And therefore, it updates a counter. 

```
//Imports//
@SuppressWarnings("deprecation")
public class KMeansReducer extends
        Reducer<ClusterCenter, Vector, ClusterCenter, Vector> {
    public static enum Counter { CONVERGED }
    private static final Log LOG = LogFactory.getLog(KMeansReducer.class);
    final List<ClusterCenter> centers = new LinkedList<ClusterCenter>();
    @Override
    protected void reduce(ClusterCenter key, Iterable<Vector> values, Context context)throws IOException, InterruptedException { --- }
    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException { --- }
}
```

## KMeansClusteringJob Class
This is the Main Class of the project and it contains the main method as well.
The various arguments created for the `.jar` file are:
*	inputfile: Location of the input `csv` file.
*	Distanceformula : Character indicating the distance algorithm to be used:
    *	m ; Manhattan Distance
    *	e : Euclidian Distance
    *	j : Jaccard Distance
*	K : no of cluster to be created
*	Error: maximum amount of error in the convergence check
*	Debug : “y” for skipping all subsequent iterations and no file output.

```
//Imports//
@SuppressWarnings("deprecation")
public class KMeansClusteringJob {
    private static final Log LOG = LogFactory.getLog(KMeansClusteringJob.class);
    //public static final List<ClusterCenter> centers = new LinkedList<ClusterCenter>();
    public static char distanceFormula = 'm';
    public static double error = 0;
    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
        Boolean arguments = false;
        int k = 3;// No of Centroids
        char debug = 'n';
        // Optional arguments
        if (args.length > 0) {
            arguments = true;}
        if (args.length > 1) {
            distanceFormula = args[1].charAt(0);}
        if (args.length > 2) {
            k = Integer.parseInt(args[2]);}
        if (args.length > 3) {
            error = Double.parseDouble(args[3]);}
        if (args.length > 4) {
            debug = args[4].charAt(0);}
        ---
    }
}
```

<br>

# Python Scripts

The python scripts use libraries like `pandas` and `matplotlib` to read the output data from the csv files and display it as scatter plots for analysis and comparison.
The KMeansClustering Job returns two files as the output: 

`Centroids.csv` and `Output.csv`.

`Centroids.csv` contains all the centroids.
`Output.csv` contains all the data along with the centroid they are clustered into. 
The script `GraphPlotter.py` can read and plot these data as scatter plots.

Another python script `BatchGraphPlotter.py`, takes multiple K Means Outputs (For different values of K) as input and calculates both Distortion and Inertia (WCSS) for the Output Clusters by reading Centroids.csv and Output.csv files outputs the result to Elbow.csv.
This Elbow.csv contains data in the format of (K, WCSS, Distortion) can be plotted using the `ElbowMethod.py` script.
This WCSS data can be calculated for different values of K and the result can be used to plot a graph for the elbow method.