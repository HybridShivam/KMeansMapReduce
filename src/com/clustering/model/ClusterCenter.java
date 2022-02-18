package com.clustering.model;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.hadoop.io.WritableComparable;
public class ClusterCenter implements WritableComparable<ClusterCenter> {
    private Vector center;
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
   public boolean converged(ClusterCenter c) {
       return compareTo(c) == 0 ? false : true;
   }
   @Override
   public void write(DataOutput out) throws IOException {
       center.write(out);
   }
   @Override
   public void readFields(DataInput in) throws IOException {
       this.center = new Vector();
       center.readFields(in);
   }
   @Override
   public int compareTo(ClusterCenter o) {
       return center.compareTo(o.getCenter());
   }
/**
 * @return the center
 */
public Vector getCenter() {
   return center;
}
@Override
public String toString() {
   return "ClusterCenter [center=" + center + "]";
}
public String print(){
    String s="";
    for(int i=0;i<center.getVector().length;i++){
        s=s+center.getVector()[i];
        if(i<center.getVector().length-1){
            s=s+',';
        }
    }
    return s;
}
}
