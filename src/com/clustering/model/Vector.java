package com.clustering.model;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import org.apache.hadoop.io.WritableComparable;

public class Vector implements WritableComparable<Vector> {
    private double[] vector;

    public Vector() {
        super();
    }

    public Vector(Vector v) {
        super();
        int l = v.vector.length;
        this.vector = new double[l];
        System.arraycopy(v.vector, 0, this.vector, 0, l);
    }

    public Vector(double x, double y) {
        super();
        this.vector = new double[] { x, y };
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        int size = in.readInt();
        vector = new double[size];
        for (int i = 0; i < size; i++)
            vector[i] = in.readDouble();
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeInt(vector.length);
        for (int i = 0; i < vector.length; i++)
            out.writeDouble(vector[i]);
    }

    @Override
    public int compareTo(Vector o) {
        boolean equals = true;
        for (int i = 0; i < vector.length; i++) {
            if (vector[i] != o.vector[i]) {
                equals = false;
                break;
            }
        }
        if (equals)
            return 0;
        else
            return 1;
    }

    @Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Vector other = (Vector) obj;
		if (vector == null) {
			if (other.vector != null)
				return false;
		} else if (!vector.equals(other.vector))
			return false;
		return true;
	}

    public double[] getVector() {
        return vector;
    }

    public void setVector(double[] vector) {
        this.vector = vector;
    }

    public String toString() {
        return "Vector [" + Arrays.toString(vector) + "]";
    }

    public String print() {
        String s = "";
        for (int i = 0; i < vector.length; i++) {
            s = s + vector[i];
            if (i < vector.length - 1) {
                s = s + ',';
            }
        }
        return s;
    }
}