import java.util.*;
import java.io.*;

class KMeans {

  public static void runAnIteration (String dataFile, String clusterFile) {
    
    // start out by opening up the two input files
    BufferedReader inData, inClusters;
    try {
      inData = new BufferedReader (new FileReader (dataFile));
      inClusters = new BufferedReader (new FileReader (clusterFile)); 
    } catch (Exception e) {
      throw new RuntimeException (e); 
    }
    
    // this is the list of current clusters
    ArrayList <VectorizedObject> oldClusters = new ArrayList <VectorizedObject> (0);
    
    // this is the list of clusters that come out of the current iteration
    ArrayList <VectorizedObject> newClusters = new ArrayList <VectorizedObject> (0);
    
    try {     
      
      // read in each cluster... each cluster is on one line of the clusters file
      String cur = inClusters.readLine (); 
      while (cur != null) {
        
        // first, read in the old cluster
        VectorizedObject cluster = new VectorizedObject (cur);
        oldClusters.add (cluster);
        
        // now create the new cluster with the same name (key) as the old one, with zero points
        // assigned, and with a vector at the origin
        VectorizedObject newCluster = new VectorizedObject (cluster.getKey (), "0", 
                                           new SparseDoubleVector (cluster.getLocation ().getLength ())); 
        newClusters.add (newCluster);
        
        // read in the next line
        cur = inClusters.readLine (); 
      }
      inClusters.close ();
      
    } catch (Exception e) {
      throw new RuntimeException (e); 
    }

    // now, process the data
    try {     
      
      // read in each data point... each point is on one line of the file
      String cur = inData.readLine (); 
      int numPoints = 0;
      
      // while we have not hit the end of the file
      while (cur != null) {
        
        // process the next data point
        VectorizedObject temp = new VectorizedObject (cur);
        
        // now, compare it with each of the existing cluster centers to find the closet one
        double minDist = 9e99;
        int bestIndex = -1;
        for (int i = 0; i < oldClusters.size (); i++) {
          if (temp.getLocation ().distance (oldClusters.get (i).getLocation ()) < minDist) {
            bestIndex = i;
            minDist = temp.getLocation ().distance (oldClusters.get (i).getLocation ());
          }
        }
          
        // since we have found the closest one, we add outselves in
        temp.getLocation ().addMyselfToHim (newClusters.get (bestIndex).getLocation ());
        newClusters.get (bestIndex).incrementValueAsInt ();
      
        // let people know that we are progressing
        if (numPoints++ % 1000 == 0)
          System.out.format (".");
        
        // read in the next line from the file
        cur = inData.readLine (); 
      }
      
      System.out.println ("Done with pass thru data.");
      inData.close ();
      
    } catch (Exception e) {
      e.printStackTrace ();
      throw new RuntimeException (e); 
    }
    
    // loop through all of the clusters, finding the one with the most points, and the one with the fewest
    int bigIndex = -1, big = 0, smallIndex = -1, small = 999999999, curIndex = 0;
    
    // loop thru the clusters
    for (VectorizedObject i : newClusters) {
      
      // if we get one that has fewer than "small" points, remember it
      if (i.getValueAsInt () < small) {
        small = i.getValueAsInt ();
        smallIndex = curIndex;
      }
      
      // if we get one with more than "big" points, remember it
      if (i.getValueAsInt () > big) {
        big = i.getValueAsInt ();
        bigIndex = curIndex; 
      }
      curIndex++;
    }
    
    // if the big one is less than 1/20 the size of the small one, then split the big one and use
    // it to replace the small one
    if (small < big / 20) {
      String temp = newClusters.get (bigIndex).writeOut ();
      VectorizedObject newObj = new VectorizedObject (temp);
      newObj.setKey (newClusters.get (smallIndex).getKey ());
      newObj.getLocation ().multiplyMyselfByHim (1.00000001);
      newClusters.set (smallIndex, newObj);
    }
    
    // lastly, divide each cluster by its count and write it out
    PrintWriter out;
    try {
      out = new PrintWriter (new BufferedWriter (new FileWriter (clusterFile)));
    } catch (Exception e) {
      throw new RuntimeException (e); 
    }
    
    // loops through all of the clusters and writes them out
    for (VectorizedObject i : newClusters) {
      i.getLocation ().multiplyMyselfByHim (1.0 / i.getValueAsInt ());
      String stringRep = i.writeOut ();
      out.println (stringRep);
    }
    
    // and get outta here!
    out.close ();
  }
  
  public static void main (String [] args) {
    
    BufferedReader myConsole = new BufferedReader(new InputStreamReader(System.in));
    if (myConsole == null) {
      throw new RuntimeException ("No console.");
    }
    
    String dataFile = null;
    System.out.format ("Enter the file with the data vectors: ");
    while (dataFile == null) {
      try {
        dataFile = myConsole.readLine ();
      } catch (Exception e) {
        System.out.println ("Problem reading data file name.");
      }
    }
    
    String clusterFile = null;
    System.out.format ("Enter the name of the file where the clusers are loated: ");
    while (clusterFile == null) {
      try {
        clusterFile = myConsole.readLine ();
      } catch (Exception e) {
        System.out.println ("Problem reading file name.");
      }
    }
    
    Integer k = null;
    System.out.format ("Enter the number of iterations to run: ");
    while (k == null) {
      try {
        String temp = myConsole.readLine ();
        k = Integer.parseInt (temp);
      } catch (Exception e) {
        System.out.println ("Problem reading the number of clusters.");
      }
    } 
    
    for (int i = 0; i < k; i++)
      runAnIteration (dataFile, clusterFile); 
  }
}

