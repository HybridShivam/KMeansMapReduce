package com.clustering.mapreduce;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.mapreduce.Mapper;
import com.clustering.model.ClusterCenter;
import com.clustering.model.DistanceMeasurer;
import com.clustering.model.Vector;
@SuppressWarnings("deprecation")
public class KMeansMapper extends
Mapper<ClusterCenter, Vector, ClusterCenter, Vector>{
    List<ClusterCenter> centers = new LinkedList<ClusterCenter>();
    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
       super.setup(context);
       Configuration conf = context.getConfiguration();
       Path centroids = new Path(conf.get("centroid.path"));
       FileSystem fs = FileSystem.get(conf);
       SequenceFile.Reader reader = new SequenceFile.Reader(fs, centroids, conf);
       ClusterCenter key = new ClusterCenter();
       IntWritable value = new IntWritable();
       while (reader.next(key, value)) {
           centers.add(new ClusterCenter(key));
       }
       reader.close();
   }
   @Override
   protected void map(ClusterCenter key, Vector value, Context context) throws IOException, InterruptedException {
       ClusterCenter nearest = null;
       double nearestDistance = Double.MAX_VALUE;
       for (ClusterCenter c : centers) {
           double dist = DistanceMeasurer.measureDistance(c, value);
           if (nearest == null) {
               nearest = c;
               nearestDistance = dist;
           } else {
               if (nearestDistance > dist) {
                   nearest = c;
                   nearestDistance = dist;
               }
           }
       }
       context.write(nearest, value);
   }
}
