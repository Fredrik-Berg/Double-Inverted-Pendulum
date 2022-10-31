import java.io.*;
import java.util.*;

public class readK {
    private Scanner scan;
    private double[] K;

    //Opens the txt-file which we want to read from and stores it a scan-object.
    public void Openfile(String str){
        try{
            scan = new Scanner(new File(str));
        }
        catch(Exception e){
            System.out.println("could not find file");
        }
    }

    /*Reads the different K-values from the scan-object and converts them into doubles which can be util
    ized in our code*/
    public double[] readKval(){
        K = new double[6];
        int i = 0;
        double newK = 0;
        while(scan.hasNext()){
            newK = Double.parseDouble(scan.next());
            K[i] = newK;
            System.out.println(K[i]);
            i = i + 1; 
        }
        
        return K;
    }
    // Closes the txt-file after we have collected the data
    public void Closefile(){
        scan.close();
    }
}