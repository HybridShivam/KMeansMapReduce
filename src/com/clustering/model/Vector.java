package com.clustering.model;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import org.apache.hadoop.io.WritableComparable;
public class Vector implements WritableComparable<Vector>{  
    private double[] vector;
    public Vector(){
        super();
    }
    public Vector(Vector v){
        super();
        int l= v.vector.length;
        this.vector= new double[l];
        System.arraycopy(v.vector, 0,this.vector, 0, l);
    }
    public Vector(double x, double y){
        super();
        this.vector = new double []{x,y};
    }
    @Override
    public void readFields(DataInput in) throws IOException {
// TODO Auto-generated method stub
        int size = in.readInt();
        vector = new double[size];
        for(int i=0;i<size;i++)
            vector[i]=in.readDouble();
    }
    @Override
    public void write(DataOutput out) throws IOException {
// TODO Auto-generated method stub
        out.writeInt(vector.length);
        for(int i=0;i<vector.length;i++)
            out.writeDouble(vector[i]);
    }
    @Override
    public int compareTo(Vector o) {
// TODO Auto-generated method stub
        boolean equals = true;
        for (int i=0;i<vector.length;i++){
            if (vector[i] != o.vector[i]) {
               equals = false;
               break;
           }
       }
       if(equals)
        return 0;
    else
        return 1;
}
public double[] getVector(){
    return vector;
}
public void setVector(double[]vector){
    this.vector=vector;
}
public String toString(){
    return "Vector [vector=" + Arrays.toString(vector) + "]";
}
}