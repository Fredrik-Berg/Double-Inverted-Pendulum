import static java.lang.Math.PI;

public class LQRdu{
    private readK rK; // Txt-file reader
    private double[] Kdu; // The desired gain of the controller, imported from matlab
    private double u; // Control signal
    private double x1ref; // Intial condition for inner pendulum
    private double x2ref; // Initial condition for outer pendulum

    public LQRdu(){
        this.Kdu = new double[6];
        this.rK = new readK();
        this.u = 0.0;
        this.x1ref = Math.PI;
        this.x2ref = 0;
        Kfetch();
    }
    
    // Reads the K-vector from an txt-file and stores it in Kdu
    public synchronized void Kfetch(){
        rK.Openfile("Kdownup.txt");
        this.Kdu = rK.readKval();
        rK.Closefile();
    }

    // Calculating the control signal based upon the states of the DIPC and the K-vector
    public double Calcu(double[] states){
        this.u = -(Kdu[0]*states[0]) + (Kdu[1]*(x1ref - states[1])) + (Kdu[2]*(x2ref - states[2])) - (Kdu[3]*states[3]) - (Kdu[4]*states[4]) - (Kdu[5]*states[5]);
        return this.u;
    }    
}