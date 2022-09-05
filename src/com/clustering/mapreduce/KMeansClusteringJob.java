package com.clustering.mapreduce;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.lang.String;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import com.clustering.model.ClusterCenter;
import com.clustering.model.Vector;

@SuppressWarnings("deprecation")
public class KMeansClusteringJob {

    private static final Log LOG = LogFactory.getLog(KMeansClusteringJob.class);

    // public static final List<ClusterCenter> centers = new
    // LinkedList<ClusterCenter>();

    // private static double error = 0;

    // public static double getError(){
    // return error;
    // }

    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {

        // Arguments(in order) are: inputfile, distanceformula, k, error, debug

        Boolean arguments = false;
        int k = 3;// No of Centroids
        char debug = 'n';
        // Optional arguments
        if (args.length > 0) {
            arguments = true;
        }
        if (args.length > 2) {
            k = Integer.parseInt(args[2]);
        }
        // if (args.length > 3) {
        // error = Double.parseDouble(args[3]);
        // }
        if (args.length > 4) {
            debug = args[4].charAt(0);
        }
        int iteration = 1;
        Configuration conf = new Configuration();
        conf.set("num.iteration", iteration + "");
        conf.set("distanceFormula", args[1]);
        conf.set("errorThreshold", args[3]);
        Path in = new Path("files/clustering/import/data");
        Path center = new Path("files/clustering/import/center/cen.seq");
        conf.set("centroid.path", center.toString());
        Path out = new Path("files/clustering/depth_1");
        Job job = new Job(conf);
        job.setJobName("KMeans Clustering");
        job.setMapperClass(KMeansMapper.class);
        job.setReducerClass(KMeansReducer.class);
        job.setJarByClass(KMeansMapper.class);
        job.setNumReduceTasks(1);
        SequenceFileInputFormat.addInputPath(job, in);
        FileSystem fs = FileSystem.get(conf);
        if (fs.exists(out))
            fs.delete(out, true);
        if (fs.exists(center))
            fs.delete(out, true);
        if (fs.exists(in))
            fs.delete(out, true);
        final SequenceFile.Writer centerWriter = SequenceFile.createWriter(fs, conf, center, ClusterCenter.class,
                IntWritable.class);
        final SequenceFile.Writer dataWriter = SequenceFile.createWriter(fs, conf, in, ClusterCenter.class,
                Vector.class);
        final IntWritable value = new IntWritable(0);
        if (!arguments) {
            // HardCoded K-Center Vectors
            centerWriter.append(new ClusterCenter(new Vector(157, 75)), value);
            centerWriter.append(new ClusterCenter(new Vector(190, 100)), value);
            centerWriter.append(new ClusterCenter(new Vector(146, 60)), value);
            centerWriter.close();
            // HardCoded Input Vectors
            dataWriter.append(new ClusterCenter(new Vector(0, 0)), new Vector(157, 69));
            dataWriter.append(new ClusterCenter(new Vector(0, 0)), new Vector(190, 59));
            dataWriter.append(new ClusterCenter(new Vector(0, 0)), new Vector(146, 100));
            dataWriter.append(new ClusterCenter(new Vector(0, 0)), new Vector(139, 93));
            dataWriter.append(new ClusterCenter(new Vector(0, 0)), new Vector(156, 105));
            dataWriter.append(new ClusterCenter(new Vector(0, 0)), new Vector(187, 85));
            dataWriter.append(new ClusterCenter(new Vector(0, 0)), new Vector(178, 54));
            dataWriter.append(new ClusterCenter(new Vector(0, 0)), new Vector(177, 88));
            dataWriter.append(new ClusterCenter(new Vector(0, 0)), new Vector(167, 92));
            dataWriter.append(new ClusterCenter(new Vector(0, 0)), new Vector(149, 89));
            dataWriter.append(new ClusterCenter(new Vector(0, 0)), new Vector(179, 62));
            dataWriter.append(new ClusterCenter(new Vector(0, 0)), new Vector(157, 87));
            dataWriter.append(new ClusterCenter(new Vector(0, 0)), new Vector(168, 103));
            dataWriter.append(new ClusterCenter(new Vector(0, 0)), new Vector(177, 102));
            dataWriter.append(new ClusterCenter(new Vector(0, 0)), new Vector(189, 95));
            dataWriter.append(new ClusterCenter(new Vector(0, 0)), new Vector(165, 91));
            dataWriter.append(new ClusterCenter(new Vector(0, 0)), new Vector(183, 82));
        } else {
            // Reading CSV Input File
            List<List<String>> records = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(args[0]))) {
                String line;
                int i = 0;
                br.readLine();
                while ((line = br.readLine()) != null) {
                    String[] values = line.split(",");
                    records.add(Arrays.asList(values));
                    if (i < k) {
                        centerWriter.append(
                                new ClusterCenter(new Vector(Integer.parseInt(values[0]), Integer.parseInt(values[1]))),
                                value);
                        i++;
                    }
                    dataWriter.append(new ClusterCenter(new Vector(0, 0)),
                            new Vector(Integer.parseInt(values[0]), Integer.parseInt(values[1])));
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

        }
        centerWriter.close();
        dataWriter.close();

        SequenceFileOutputFormat.setOutputPath(job, out);
        job.setInputFormatClass(SequenceFileInputFormat.class);
        job.setOutputFormatClass(SequenceFileOutputFormat.class);
        job.setOutputKeyClass(ClusterCenter.class);
        job.setOutputValueClass(Vector.class);
        job.waitForCompletion(true);
        long counter = job.getCounters().findCounter(KMeansReducer.Counter.CONVERGED).getValue();
        iteration++;
        if (debug == 'y') {
            for (int z = 2; z <= iteration; z++) {
                LOG.info("Iteration: " + (z - 1));
                Path result = new Path("files/clustering/depth_" + (z - 1) + "/");
                FileStatus[] stati = fs.listStatus(result);
                for (FileStatus status : stati) {
                    if (!status.isDir() && !status.getPath().toString().contains("/_")) {
                        Path path = status.getPath();
                        LOG.info("FOUND " + path.toString());
                        SequenceFile.Reader reader = new SequenceFile.Reader(fs, path, conf);
                        ClusterCenter key = new ClusterCenter();
                        Vector v = new Vector();
                        while (reader.next(key, v)) {
                            // Logging
                            LOG.info(key + " / " + v);
                        }
                        reader.close();
                    }
                }
            }
        } else {
            while (counter > 0) {
                LOG.info("Iteration: " + iteration);
                conf = new Configuration();
                conf.set("centroid.path", center.toString());
                conf.set("num.iteration", iteration + "");
                conf.set("distanceFormula", args[1]);
                conf.set("errorThreshold", args[3]);
                job = new Job(conf);
                job.setJobName("KMeans Clustering " + iteration);
                job.setMapperClass(KMeansMapper.class);
                job.setReducerClass(KMeansReducer.class);
                job.setJarByClass(KMeansMapper.class);
                in = new Path("files/clustering/depth_" + (iteration - 1) + "/");
                out = new Path("files/clustering/depth_" + iteration);
                SequenceFileInputFormat.addInputPath(job, in);
                if (fs.exists(out))
                    fs.delete(out, true);
                SequenceFileOutputFormat.setOutputPath(job, out);
                job.setInputFormatClass(SequenceFileInputFormat.class);
                job.setOutputFormatClass(SequenceFileOutputFormat.class);
                job.setOutputKeyClass(ClusterCenter.class);
                job.setOutputValueClass(Vector.class);
                job.waitForCompletion(true);
                iteration++;
                counter = job.getCounters().findCounter(KMeansReducer.Counter.CONVERGED).getValue();
            }

            Path result = new Path("files/clustering/depth_" + (iteration - 1) + "/");
            FileStatus[] stati = fs.listStatus(result);
            for (FileStatus status : stati) {
                if (!status.isDir() && !status.getPath().toString().contains("/_")) {
                    Path path = status.getPath();
                    LOG.info("FOUND " + path.toString());
                    SequenceFile.Reader reader = new SequenceFile.Reader(fs, path, conf);
                    ClusterCenter key = new ClusterCenter();
                    Vector v = new Vector();
                    try {
                        File file = new File("Output.csv");
                        FileWriter writer = new FileWriter(file.getAbsoluteFile(), false);
                        while (reader.next(key, v)) {
                            // Logging
                            LOG.info(key + " / " + v);
                            // Writing
                            writer.write(key.print() + "," + v.print() + "\n");
                        }
                        writer.close();
                    } catch (IOException e) {
                        System.out.println("An error occurred during Output.csv creation.");
                        e.printStackTrace();
                    }
                    reader.close();
                }
            }
            FileStatus[] stat = fs.listStatus(center);
            for (FileStatus status : stat) {
                if (!status.isDir() && !status.getPath().toString().contains("/_")) {
                    Path path = status.getPath();
                    LOG.info("FOUND " + path.toString());
                    SequenceFile.Reader reader = new SequenceFile.Reader(fs, path, conf);
                    ClusterCenter key = new ClusterCenter();
                    try {
                        File file2 = new File("Centroids.csv");
                        FileWriter writer2 = new FileWriter(file2.getAbsoluteFile(), false);
                        LOG.info("Centroids: ");
                        IntWritable val = new IntWritable(0);
                        while (reader.next(key, val)) {
                            // Logging
                            LOG.info(key);
                            // Writing
                            writer2.write(key.print() + "\n");
                        }
                        writer2.close();
                    } catch (IOException e) {
                        System.out.println("An error occurred during Centroids.csv creation.");
                        e.printStackTrace();
                    }
                    reader.close();
                }
            }
        }
    }
}
