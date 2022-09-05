package com.clustering.model;

import java.lang.Math;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DistanceMeasurer {
    private static final Log LOG = LogFactory.getLog(DistanceMeasurer.class);

    public static final double measureDistance(ClusterCenter center, Vector v, String distanceFormula) {
        double sum = 0;
        int length = v.getVector().length;
        char distance = distanceFormula.charAt(0);

        LOG.info(distance);
        switch (distance) {
            case 'm':
                // Manhattan
                LOG.info("Manhattan");
                for (int i = 0; i < length; i++) {
                    sum += Math.abs(center.getCenter().getVector()[i] - v.getVector()[i]);
                }
                return sum;
            case 'e':
                // Euclidian
                LOG.info("Euclidian");
                for (int i = 0; i < length; i++) {
                    double diff = center.getCenter().getVector()[i] - v.getVector()[i];
                    // multiplication is faster than Math.pow() for ^2.
                    sum += (diff * diff);
                }
                return Math.sqrt(sum);
            case 'j':
                // jaccard
                LOG.info("jaccard");
                double[] v1 = center.getCenter().getVector();
                double[] v2 = v.getVector();
                double dot = dot(v1, v2);
                double set1Length = sumOfSquares(v1);
                double set2Length = sumOfSquares(v2);
                return 1.0d - (dot / (set1Length + set2Length - dot));
            default:
                // Manhattan
                LOG.info("Manhattan default");
                for (int i = 0; i < length; i++) {
                    sum += Math.abs(center.getCenter().getVector()[i] - v.getVector()[i]);
                }
                return sum;
        }
    }

    private static double dot(double[] v1, double[] v2) {
        double dotProduct = 0.0d;
        for (int i = 0; i < v1.length; i++) {
            dotProduct += v1[i] * v2[i];
        }
        return dotProduct;
    }

    private static double sumOfSquares(double[] v1) {
        double dotProduct = 0.0d;
        for (double aV1 : v1) {
            dotProduct += aV1 * aV1;
        }
        return dotProduct;
    }
}
