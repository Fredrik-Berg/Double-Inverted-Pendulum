import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import se.lth.control.*;
import se.lth.control.plot.*;


public class LQRGUI{
    private Control c; 
    private int priority; //Priority of this thread
    private ModeMonitor modeMon;
	private JFrame frame; 

    private BoxPanel measPanel;
    private PlotterPanel lowerPanel, upperPanel, ctrlPanel;
	private boolean isInitialized = false;

    //Constructor
    public LQRGUI(int plotterPriority, ModeMonitor modeMon) {
		
        this.priority = plotterPriority;
        this.modeMon = modeMon;
	}
	
	// Starting the different threads in this class
    public void start() {
		lowerPanel.start();
        upperPanel.start();
		ctrlPanel.start();
	}

	// Here we create the panel where we show the different angles theta1 and theta2 as well as u.

    public void initializeGUI() {
		// Create main frame.
		frame = new JFrame("DIPC GUI");

		// Create a panel for the three plotters.
        measPanel = new BoxPanel(BoxPanel.VERTICAL);
		// Create PlotterPanels.
		lowerPanel = new PlotterPanel(2, priority);
		lowerPanel.setYAxis(8, -4, 2, 2);
		lowerPanel.setXAxis(10, 5, 5);
		lowerPanel.setUpdateFreq(10);
		lowerPanel.setTitle("Theta 1");

		upperPanel = new PlotterPanel(2, priority);
		upperPanel.setYAxis(8, -4, 2, 2);
		upperPanel.setXAxis(10, 5, 5);
		upperPanel.setUpdateFreq(10);
		upperPanel.setTitle("Theta 2");

        ctrlPanel = new PlotterPanel(1, priority);
		ctrlPanel.setYAxis(8, -4, 2, 2);
		ctrlPanel.setXAxis(10, 5, 5);
		ctrlPanel.setUpdateFreq(10);
		ctrlPanel.setTitle("Control signal");

		measPanel.add(upperPanel);
		measPanel.addFixed(10);
		measPanel.add(lowerPanel);
        measPanel.addFixed(10);
        measPanel.add(ctrlPanel);

        frame.add(measPanel);
		

        // WindowListener that exits the system if the main window is closed.
	    frame.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e) {
                c.shutDown();
                lowerPanel.stopThread();
                upperPanel.stopThread();
				ctrlPanel.stopThread();
                System.exit(0);
            }
        });

		frame.pack();
		frame.setVisible(true);
		isInitialized = true;

    }

    // Called by Control to plot a control signal data point. 
	public synchronized void putControlData(double t, double u) {
		if (isInitialized) {
			ctrlPanel.putData(t, u);
		} else {
			System.out.println("Note: GUI not yet initialized. Ignoring call to putControlData().");
		}
	} 

	//Called by Control to plot both current angle of L1 and also its desired angle
    public synchronized void putInnerData(double t, double x1, double x1ref) {
		if (isInitialized) {
			lowerPanel.putData(t, x1, x1ref);
		} else {
			System.out.println("Note: GUI not yet initialized. Ignoring call to putControlData().");
		}
	} 

	//Called by Control to plot both current angle of L2 and also its desired angle
    public synchronized void putOuterData(double t, double x2, double x2ref) {
		if (isInitialized) {
			upperPanel.putData(t, x2, x2ref);
		} else {
			System.out.println("Note: GUI not yet initialized. Ignoring call to putControlData().");
		}
	} 

	// Sets the control correctly to avoid issues in the main-class
    public void setControl(Control control) {
    	this.c = control;
    }

    

} 
        
    