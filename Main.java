import javax.swing.*;

public class Main {

    public static void main(String[] argv) {

        final int controlPriority   = 5; 
        final int plotterPriority   = 1; 

        ModeMonitor modeMon = new ModeMonitor();
		DoublePendulum dp = new DoublePendulum(modeMon, 0.382, 0.618);	
        LQRGUI gui = new LQRGUI(1, modeMon);
        Control C = new Control(modeMon, dp, gui);

        // Set dependencies
        gui.setControl(C); 
        C.setGUI(gui); 

        // Run GUI on event thread
        Runnable initializeGUI = new Runnable(){
            public void run(){
                gui.initializeGUI();
                gui.start();
            }
        };
        try{
            SwingUtilities.invokeAndWait(initializeGUI);
        }catch(Exception e){
            return;
        }

        // Start remaining threads
        C.start(); 


    }
}
