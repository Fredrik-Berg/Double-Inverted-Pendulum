import static java.lang.Math.PI;

public class LQRud{
    private readK rK; // Txt-file reader
    private double[] Kud; // The desired gain of the controller, imported from matlab
    private double u; // Control signal 
    private double x1ref; // Intial condition for inner pendulum
    private double x2ref; // Initial condition for outer pendulum

    public LQRud(){
        this.Kud = new double[6];
        this.rK = new readK();
        this.u = 0.0;
        this.x1ref = 0;
        this.x2ref = Math.PI;
        Kfetch();
    }

    // Reads the K-vector from an txt-file and stores it in Kud
    public synchronized void Kfetch(){
        rK.Openfile("Kupdown.txt");
        this.Kud = rK.readKval();
        rK.Closefile();
    }

    // Calculating the control signal based upon the states of the DIPC and the K-vector    
    public double Calcu(double[] states){
        this. u = -(Kud[0]*states[0]) + (Kud[1]*(x1ref - states[1])) + (Kud[2]*(x2ref - states[2])) - (Kud[3]*states[3]) - (Kud[4]*states[4]) - (Kud[5]*states[5]);
        return this.u;
    }
}