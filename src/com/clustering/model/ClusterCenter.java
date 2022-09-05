package com.clustering.model;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.hadoop.io.WritableComparable;

public class ClusterCenter implements WritableComparable<ClusterCenter> {
    private Vector center;
    private int clusterIndex;

    public ClusterCenter() {
        super();
        this.center = null;
    }

    public ClusterCenter(ClusterCenter center) {
        super();
        this.center = new Vector(center.center);
    }

    public ClusterCenter(Vector center) {
        super();
        this.center = center;
    }

    public boolean converged(ClusterCenter c, double error) {
        double sum = 0;
        int length = center.getVector().length;
        for (int i = 0; i < length; i++) {
            sum += Math.abs(center.getVector()[i] - c.getCenter().getVector()[i]);
        }
        return sum > error;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        center.write(out);
        out.writeInt(clusterIndex);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        this.center = new Vector();
        center.readFields(in);
        clusterIndex = in.readInt();
    }

    @Override
    public int compareTo(ClusterCenter o) {
        return Integer.compare(clusterIndex, o.clusterIndex);
    }

    @Override
	public final boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClusterCenter other = (ClusterCenter) obj;
		if (center == null) {
			if (other.center != null)
				return false;
		} else if (!center.equals(other.center))
			return false;
		return true;
	}


    public Vector getCenter() {
        return center;
    }

    public int getClusterIndex() {
		return clusterIndex;
	}

	public void setClusterIndex(int clusterIndex) {
		this.clusterIndex = clusterIndex;
	}

    @Override
    public String toString() {
        return "ClusterCenter [" + center + "]";
    }

    public String print() {
        String s = "";
        for (int i = 0; i < center.getVector().length; i++) {
            s = s + center.getVector()[i];
            if (i < center.getVector().length - 1) {
                s = s + ',';
            }
        }
        return s;
    }
}
