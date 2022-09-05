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
import org.apache.hadoop.mapreduce.Reducer;
import com.clustering.model.ClusterCenter;
import com.clustering.model.Vector;

@SuppressWarnings("deprecation")
public class KMeansReducer extends
		Reducer<ClusterCenter, Vector, ClusterCenter, Vector> {
	public static enum Counter {
		CONVERGED
	}

	private String error;

	private static final Log LOG = LogFactory.getLog(KMeansReducer.class);

	final List<ClusterCenter> centers = new LinkedList<ClusterCenter>();

	@Override
	protected void setup(Context context) throws IOException, InterruptedException {
		super.setup(context);
		Configuration conf = context.getConfiguration();
		error = context.getConfiguration().get("errorThreshold");
	}

	@Override
	protected void reduce(ClusterCenter key, Iterable<Vector> values, Context context)
			throws IOException, InterruptedException {
		Vector newCenter = new Vector();
		List<Vector> vectorList = new LinkedList<Vector>();
		int vectorSize = key.getCenter().getVector().length;
		newCenter.setVector(new double[vectorSize]);
		LOG.info("Key: " + key);
		LOG.info("Values: ");
		for (Vector value : values) {
			vectorList.add(new Vector(value));
			LOG.info(value);
			for (int i = 0; i < value.getVector().length; i++) {
				newCenter.getVector()[i] += value.getVector()[i];
			}
		}
		for (int i = 0; i < newCenter.getVector().length; i++) {
			newCenter.getVector()[i] = newCenter.getVector()[i] / vectorList.size();
		}
		// New Calculated Centroid
		ClusterCenter center = new ClusterCenter(newCenter);
		LOG.info("New Center :" + center);
		centers.add(center);
		LOG.info("Reduce Centers");
		for (ClusterCenter p : centers) {
			LOG.info(p);
		}
		LOG.info("Reduce Output");
		for (Vector vector : vectorList) {
			context.write(center, vector);
			LOG.info(center + " - " + vector);
		}

		// if not equal
		if (center.converged(key, Double.parseDouble(error)))
			context.getCounter(Counter.CONVERGED).increment(1);
		LOG.info(Counter.CONVERGED);
	}

	@Override
	protected void cleanup(Context context) throws IOException, InterruptedException {
		super.cleanup(context);
		Configuration conf = context.getConfiguration();
		Path outPath = new Path(conf.get("centroid.path"));
		FileSystem fs = FileSystem.get(conf);
		fs.delete(outPath, true);
		final SequenceFile.Writer out = SequenceFile.createWriter(fs,
				context.getConfiguration(),
				outPath, ClusterCenter.class, IntWritable.class);
		final IntWritable value = new IntWritable(0);
		for (ClusterCenter center : centers) {
			out.append(center, value);
		}
		out.close();
	}
}
