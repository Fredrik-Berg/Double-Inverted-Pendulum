import static java.lang.Math.PI;

public class LQRuu{
    private readK rK; // Txt-file reader
    private double[] Kuu; // The desired gain of the controller, imported from matlab
    private double u; // Control signal
    private double x1ref; // Intial condition for inner pendulum
    private double x2ref; // Initial condition for outer pendulum

    public LQRuu(){
        this.Kuu = new double[6];
        this.rK = new readK();
        this.u = 0.0;
        this.x1ref = 0;
        this.x2ref = 0;
        Kfetch();
    }

    // Reads the K-vector from an txt-file and stores it in Kuu
    public synchronized void Kfetch(){
        rK.Openfile("Kupup.txt");
        this.Kuu = rK.readKval();
        rK.Closefile();
    }

    // Calculating the control signal based upon the states of the DIPC and the K-vector    
    public double Calcu(double[] states){
        this.u = -(Kuu[0]*states[0]) + (Kuu[1]*(x1ref - states[1])) + (Kuu[2]*(x2ref - states[2])) - (Kuu[3]*states[3]) - (Kuu[4]*states[4]) - (Kuu[5]*states[5]);
        return this.u;
    }    
}