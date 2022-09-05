package com.clustering.mapreduce;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
        Mapper<ClusterCenter, Vector, ClusterCenter, Vector> {
    private static final Log LOG = LogFactory.getLog(KMeansMapper.class);
    private final List<ClusterCenter> centers = new LinkedList<ClusterCenter>();
    private String distanceFormula;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        super.setup(context);
        Configuration conf = context.getConfiguration();
        distanceFormula = context.getConfiguration().get("distanceFormula");
        Path centroids = new Path(conf.get("centroid.path"));
        FileSystem fs = FileSystem.get(conf);
        SequenceFile.Reader reader = new SequenceFile.Reader(fs, centroids, conf);
        ClusterCenter key = new ClusterCenter();
        IntWritable value = new IntWritable();
        LOG.info("Centroid Setup");
        int index = 0;
        while (reader.next(key, value)) {
            ClusterCenter clusterCenter = new ClusterCenter(key);
            clusterCenter.setClusterIndex(index++);
            centers.add(clusterCenter);
        }
        for (ClusterCenter center : centers) {
            LOG.info(center);
        }
        reader.close();
    }

    @Override
    protected void map(ClusterCenter key, Vector value, Context context) throws IOException, InterruptedException {
        ClusterCenter nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (ClusterCenter c : centers) {
            double dist = DistanceMeasurer.measureDistance(c, value, distanceFormula);
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
        LOG.info("Mapper Output");
        LOG.info(nearest + " - " + value);
    }
}
