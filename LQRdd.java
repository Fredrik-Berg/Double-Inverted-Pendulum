import static java.lang.Math.PI;

public class LQRdd{
    private readK rK; // Txt-file reader
    private double[] Kdd; // The desired gain of the controller, imported from matlab
    private double u; // Control signal
    private double x1ref; // Intial condition for inner pendulum
    private double x2ref; // Initial condition for outer pendulum

    public LQRdd(){
        this.Kdd = new double[6];
        this.rK = new readK();
        this.u = 0.0;
        this.x1ref = Math.PI;
        this.x2ref = Math.PI;
        Kfetch();
    }

    // Reads the K-vector from an txt-file and stores it in Kdd
    public synchronized void Kfetch(){
        rK.Openfile("Kdowndown.txt");
        this.Kdd = rK.readKval();
        rK.Closefile();
    }


    // Calculating the control signal based upon the states of the DIPC and the K-vector
    public double Calcu(double[] states){
        this.u = -(Kdd[0]*states[0]) + (Kdd[1]*(x1ref - states[1])) + (Kdd[2]*(x2ref - states[2])) - (Kdd[3]*states[3]) - (Kdd[4]*states[4]) - (Kdd[5]*states[5]);
        return this.u;
    }
}