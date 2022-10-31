import java.io.*;
import java.util.*;

public class Control extends Thread{
    private LQRud lqrud = new LQRud(); /* Initializes the four different controllers here*/
    private LQRdu lqrdu = new LQRdu();
    private LQRuu lqruu = new LQRuu();
    private LQRdd lqrdd = new LQRdd();
    private boolean shouldRun = true; 
    private double u = 0.0; // control signal
    private double h = 0.02; // sample time
    private ModeMonitor modemon; // selector of intial conditions
    private DoublePendulum dpend; // the DIPC to control 
    private LQRGUI gui; // GUI for visualizing the angles of the two pendulums and also the control signal
    private double x1ref; //Desired angle of the inner pendulum
    private double x2ref; //Desired angle of the outer pendulum
    double[] states; // Vector for all the state variables

    public Control (ModeMonitor modemon, DoublePendulum dpend, LQRGUI gui){
        this.modemon = modemon;
        this.dpend = dpend;
        this.gui = gui;
        this.states = new double[6];
    }

    //Function to make sure that the Main-thread can run as intended
    public void setGUI(LQRGUI gui){
        this.gui = gui;
    }
    //Shuts down the controller
    public void shutDown(){
        shouldRun = false;
    }

    //Running the control for our different cases!
    public void run(){
        long duration;
        long currenttime;
        long t = System.currentTimeMillis();
        long starttime = t;
        

        while (shouldRun) {
            states = dpend.getFullState();

            switch (modemon.getMode()) {
                case OFF: {
                    break;
                }

                case DD: {
                    u = lqrdd.Calcu(states);
                    //System.out.println(u);
                    dpend.setControlSignal(u);
                    x1ref = Math.PI;
                    x2ref = Math.PI;
                    break;
                }
                case DU: {                        
                    u = lqrdu.Calcu(states);
                    //System.out.println(u);
                    dpend.setControlSignal(u);
                    x1ref = Math.PI;
                    x2ref = 0.0;
                    break;
                }
            
                case UD: {                   
                    u = lqrud.Calcu(states);
                    //System.out.println(u);
                    dpend.setControlSignal(u);
                    x1ref = 0.0;
                    x2ref = Math.PI;
                    break;
                }

                case UU: {                  
                    u = lqruu.Calcu(states);
                    //System.out.println(u);
                    dpend.setControlSignal(u);
                    x1ref = 0.0;
                    x2ref = 0.0;
                    break;
                }

                default: {
                    System.out.println("Error: Illegal mode.");
                    break;
                }

            }

            if( u > 10){
                u = 10;
            }
            else if (u < -10){
                u = -10;
            }
            
            currenttime = (System.currentTimeMillis() - starttime)/1000;
            gui.putControlData(currenttime, u);
            gui.putInnerData(currenttime, states[1], x1ref);
            gui.putOuterData(currenttime, states[2], x2ref);
        
            t = t + (long)(1000*h);
            duration = t - System.currentTimeMillis();
            if (duration > 0) {
                try {
                    sleep(duration);
                } catch (InterruptedException x) {}
            } else {
                System.out.println("Lagging behind...");
            }
        }
    }
}
    
