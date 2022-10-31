import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.signum;
import static java.lang.Math.abs;

import java.util.*;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.SwingUtilities;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.beans.property.*;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial; 
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.scene.transform.Transform;
import javafx.scene.SceneAntialiasing;
import javafx.scene.Camera;
import javafx.scene.PerspectiveCamera;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.geometry.Pos;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.util.StringConverter;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class DoublePendulum {

    private final int WIDTH = 1000;
    private final int HEIGHT = 600;
    private final int frameduration = 20;     // animation cycle time (ms)

    private final DoubleProperty theta0 = new SimpleDoubleProperty(0);  // cart position
    private final DoubleProperty theta1 = new SimpleDoubleProperty(0);  // inner pendulum angle
    private final DoubleProperty theta2 = new SimpleDoubleProperty(0);  // outer pendulum angle

    private double L1, L2;  
    private DoublePendSim doublePendSim;                 // reference to pendulum simulator
    private ModeMonitor modeMon;

    private void initFXAnimation(JFXPanel fxPanel) {  // This method is invoked on the JavaFX thread

        double innerpendlength = 90*L1;
        double outerpendlength = 90*L2;
        int cartheight = 8;

        Group cartgroup = new Group();
        Cylinder cartbody = new Cylinder(20, cartheight);
        cartgroup.getChildren().add(cartbody);
        Sphere wheel1 = new Sphere(4);
        wheel1.setTranslateY(cartheight/2);
        wheel1.setTranslateX(-10);
        cartgroup.getChildren().add(wheel1);
        Sphere wheel2 = new Sphere(4);
        wheel2.setTranslateY(cartheight/2);
        wheel2.setTranslateX(10);
        cartgroup.getChildren().add(wheel2);

        Translate translatecart = new Translate(0, 0, 0);
        translatecart.xProperty().bind(theta0);
        cartgroup.getTransforms().add(translatecart);

        Group pendgroup = new Group();
        Sphere joint1 = new Sphere(4);
        joint1.setTranslateY(-cartheight/2);
        pendgroup.getChildren().add(joint1);

        Cylinder arm1 = new Cylinder(3,innerpendlength);
        arm1.setTranslateY(-innerpendlength/2-cartheight/2);
        pendgroup.getChildren().add(arm1);

        Rotate rotatepend = new Rotate(0, 0, -cartheight/2, 0, Rotate.Z_AXIS);
        rotatepend.angleProperty().bind(theta1);
        pendgroup.getTransforms().add(rotatepend);

        Sphere joint2 = new Sphere(4);
        joint2.setTranslateY(-innerpendlength-cartheight/2);
        pendgroup.getChildren().add(joint2);

        Group outerpendgroup = new Group();
        Cylinder arm2 = new Cylinder(3,outerpendlength);
        arm2.setTranslateY(-innerpendlength-outerpendlength/2-cartheight/2);
        outerpendgroup.getChildren().add(arm2);

        Sphere joint3 = new Sphere(4);
        joint3.setTranslateY(-innerpendlength-outerpendlength-cartheight/2);
        outerpendgroup.getChildren().add(joint3);

        Rotate rotateouterpend = new Rotate(0, 0, -innerpendlength-cartheight/2, 0, Rotate.Z_AXIS);
        rotateouterpend.angleProperty().bind(theta2);
        outerpendgroup.getTransforms().add(rotateouterpend);
        pendgroup.getChildren().add(outerpendgroup);

        cartgroup.getChildren().add(pendgroup);

        Scene scene = new Scene(cartgroup, WIDTH, HEIGHT, true, SceneAntialiasing.BALANCED);
        scene.setFill(Color.SILVER);
        Camera camera = new PerspectiveCamera();	
        scene.setCamera(camera);

        // maingroup.setRotationAxis(Rotate.X_AXIS); 
        // maingroup.setRotate(8);
        cartgroup.translateXProperty().set(WIDTH / 2);
        cartgroup.translateYProperty().set(HEIGHT / 2);
        cartgroup.translateZProperty().set(-350);

        fxPanel.setScene(scene);

        // Generate animation events
        Timeline periodicAction = new Timeline(new KeyFrame(Duration.millis(frameduration), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                theta0.set(Math.max(-250, Math.min(250, 100.0 * doublePendSim.th0))); 
                theta1.set(doublePendSim.th1 * 180.0 / PI); 
                theta2.set((doublePendSim.th2-doublePendSim.th1) * 180.0 / PI);   
            }
        }));
        periodicAction.setCycleCount(Timeline.INDEFINITE);
        periodicAction.play();  // start the animation
    }

    private void initFXButtons(JFXPanel fxPanel) {  // This method is invoked on the JavaFX thread
        // Buttons
        final RadioButton offB = new RadioButton("Off");
        final RadioButton ddB = new RadioButton("Down-Down");
        final RadioButton duB = new RadioButton("Down-Up");
        final RadioButton udB = new RadioButton("Up-Down");
        final RadioButton uuB = new RadioButton("Up-Up");

        // Add to group
        ToggleGroup group = new ToggleGroup();
        offB.setToggleGroup(group);
        offB.setPrefSize(60, 30);
        offB.setSelected(true);
        ddB.setToggleGroup(group);
        ddB.setPrefSize(120, 30);
        duB.setToggleGroup(group);
        duB.setPrefSize(100, 30);
        udB.setToggleGroup(group);
        udB.setPrefSize(100, 30);
        uuB.setToggleGroup(group);
        uuB.setPrefSize(100, 30);

        // Add events to buttons
        offB.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> obs, Boolean wasSelected, Boolean isSelected) {
                if (isSelected) {
                    modeMon.setMode(ModeMonitor.Mode.OFF);
                    doublePendSim.reset(ModeMonitor.Mode.OFF);
                }
            }
        });
        ddB.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> obs, Boolean wasSelected, Boolean isSelected) {
                if (isSelected) {
                    modeMon.setMode(ModeMonitor.Mode.DD);
                    doublePendSim.reset(ModeMonitor.Mode.DD);
                }
            }
        });
        duB.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> obs, Boolean wasSelected, Boolean isSelected) {
                if (isSelected) {
                    modeMon.setMode(ModeMonitor.Mode.DU);
                    doublePendSim.reset(ModeMonitor.Mode.DU);
                }
            }
        });
        udB.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> obs, Boolean wasSelected, Boolean isSelected) {
                if (isSelected) {
                    modeMon.setMode(ModeMonitor.Mode.UD);
                    doublePendSim.reset(ModeMonitor.Mode.UD);
                }
            }
        });
        uuB.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> obs, Boolean wasSelected, Boolean isSelected) {
                if (isSelected) {
                    modeMon.setMode(ModeMonitor.Mode.UU);
                    doublePendSim.reset(ModeMonitor.Mode.UU);
                }
            }
        });

        // Add buttons to pane
        HBox hbox = new HBox();
        HBox.setHgrow(hbox, Priority.ALWAYS);
        HBox.setHgrow(offB, Priority.ALWAYS);
        HBox.setHgrow(ddB, Priority.ALWAYS);
        HBox.setHgrow(duB, Priority.ALWAYS);
        HBox.setHgrow(udB, Priority.ALWAYS);
        HBox.setHgrow(uuB, Priority.ALWAYS);
        hbox.getChildren().addAll(offB, ddB, duB, udB, uuB);
        hbox.setAlignment(Pos.CENTER);

        // Text input pattern matching
        Pattern validEditingState = Pattern.compile("-?(([1-9][0-9]*)|0)?(\\.[0-9]*)?");

        UnaryOperator<TextFormatter.Change> filter = c -> {
            String text = c.getControlNewText();
            if (validEditingState.matcher(text).matches()) {
                return c ;
            } else {
                return null ;
            }
        };

        // Convert string to double
        StringConverter<Double> converter = new StringConverter<Double>() {
            @Override
            public Double fromString(String s) {
                if (s.isEmpty() || "-".equals(s) || ".".equals(s) || "-.".equals(s)) {
                    return 0.0;
                } else {
                    return Double.valueOf(s);
                }
            }
            @Override
            public String toString(Double d) {
                return d.toString();
            }
        };

        // Create textfields which filter text and converts it
        TextFormatter<Double> theta1Formatter = new TextFormatter<>(converter, 0.0, filter);
        TextField theta1Field = new TextField();
        theta1Field.setTextFormatter(theta1Formatter);
        theta1Formatter.valueProperty().addListener((ObservableValue<? extends Double> obs, Double oldValue, Double newValue) -> {
            doublePendSim.setOffsetTh1(newValue.doubleValue());
        });

        TextFormatter<Double> theta2Formatter = new TextFormatter<>(converter, 0.0, filter);
        TextField theta2Field = new TextField();
        theta2Field.setTextFormatter(theta2Formatter);
        theta2Formatter.valueProperty().addListener((ObservableValue<? extends Double> obs, Double oldValue, Double newValue) -> {
            doublePendSim.setOffsetTh2(newValue.doubleValue());
        });

        // Add label and text field to pane and add to scene.
        HBox h1 = new HBox();
        HBox.setHgrow(theta1Field, Priority.ALWAYS);
        h1.getChildren().addAll(new Label("Theta 1 Offset: (Degrees) "), theta1Field);
        HBox h2 = new HBox();
        HBox.setHgrow(theta2Field, Priority.ALWAYS);
        h2.getChildren().addAll(new Label("Theta 2 Offset: (Degrees) "), theta2Field);

        VBox vbox = new VBox();
        VBox.setVgrow(h1, Priority.ALWAYS);
        VBox.setVgrow(h2, Priority.ALWAYS);
        vbox.getChildren().addAll(h1, h2);

        HBox combine = new HBox();
        combine.getChildren().addAll(hbox, vbox);
        Scene scene = new Scene(combine, WIDTH, (int) (HEIGHT/10));
        fxPanel.setScene(scene);
    }

    // Returns the full state of the double pendulum:
    //     x = [theta0, theta1, theta2, dtheta0, dtheta1, dtheta2]
    public double[] getFullState() {
        return doublePendSim.getFullState();
    }

    public void setControlSignal(double u) {
        doublePendSim.setControlSignal(u);
    }

    public DoublePendulum(ModeMonitor mon, double L1, double L2) {

        modeMon = mon;
        this.L1 = L1;
        this.L2 = L2;

        doublePendSim = new DoublePendSim(L1, L2);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // This method is invoked on the EDT thread
                JFrame frame = new JFrame("Double Pendulum Animation");
                JPanel combinedPanel = new JPanel(new BorderLayout());

                final JFXPanel fxPanelScene = new JFXPanel();
                final JFXPanel fxPanelButtons = new JFXPanel();

                combinedPanel.add(fxPanelScene, BorderLayout.CENTER);
                combinedPanel.add(fxPanelButtons, BorderLayout.SOUTH);

                frame.add(combinedPanel);
                frame.setSize(WIDTH, (int) (HEIGHT*1.1));
                frame.setVisible(true);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                // Add reset on keypress or mouseclick

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        initFXAnimation(fxPanelScene);
                        initFXButtons(fxPanelButtons);
                    }
                });
            }
        });
    }

    private class DoublePendSim {
        // physical constants
        private double d1, d2, d3, d4, d5, d6, f1, f2; // set in constructor
        private double m0, m1, m2, l1, l2, I1, I2, g;

        // Initial values for the four modes (and OFF)
        private final double[][] init = {{PI, PI}, {PI, 0.0}, {0.0, PI}, {0.0, 0.0}, {PI, PI}};
        private double th1Off, th2Off;

        // The state variables and control signal
        private double th0, th1, th2;
        private double dth0, dth1, dth2;
        private double u;

        private double ulim = 10.0;

        private Random rg = new Random();

        private DoublePendSim(double L1, double L2) {
            reset(modeMon.getMode());

            m0 = 5.0;
            m1 = L1;
            m2 = L2;
            l1 = L1/2;
            l2 = L2/2;
            I1 = m1*L1*L1/12;
            I2 = m2*L2*L2/12;
            g = 9.81;

            d1 = m0 + m1 + m2;
            d2 = m1*l1 + m2*L1;
            d3 = m2*l2;
            d4 = m1*l1*l1 + m2*L1*L1 + I1;
            d5 = m2*L1*l2;
            d6 = m2*l2*l2 + I2;
            f1 = (m1*l1 + m2*L1)*g;
            f2 = m2*l2*g;

            final int period = 1;   // task period (in milliseconds)

            // Create periodic timer 
            Timer timer = new Timer();
            TimerTask timertask = new TimerTask() {
                @Override
                public void run() {
                    timeStep(period);  // time step the dynamics simulator
                }
            };
            timer.scheduleAtFixedRate(timertask,1000,period);      	
        }

        private void timeStep(int millis) {

            double h = 0.001 * millis; // time step in seconds

            double c1  = cos(th1);
            double c2  = cos(th2);
            double c12 = cos(th1-th2);
            double s1  = sin(th1);
            double s2  = sin(th2);
            double s12 = sin(th1-th2);

            synchronized (this) {
                double invDdet = 1.0 / (d2*d2*d6*c1*c1 - d1*d4*d6 + d3*d3*d4*c2*c2 + d1*d5*d5*c12*c12 - 2*d2*d3*d5*c12*c1*c2);
                double A12 = d5*dth1*s12*(d3*d4*c2 - d2*d5*c12*c1) - d2*dth1*s1*(d4*d6 - d5*d5*c12*c12);
                double A13 = -d3*dth2*s2*(d4*d6 - d5*d5*c12*c12) - d5*dth2*s12*(d2*d6*c1 - d3*d5*c12*c2);
                double A22 = d2*dth1*s1*(d2*d6*c1 - d3*d5*c12*c2) + d5*dth1*s12*(d1*d5*c12 - d2*d3*c1*c2);
                double A23 = d5*dth2*s12*(d1*d6 - d3*d3*c2*c2) + d3*dth2*s2*(d2*d6*c1 - d3*d5*c12*c2);
                double A32 = d2*dth1*s1*(d3*d4*c2 - d2*d5*c12*c1) - d5*dth1*s12*(d1*d4 - d2*d2*c1*c1);
                double A33 = d3*dth2*s2*(d3*d4*c2 - d2*d5*c12*c1) - d5*dth2*s12*(d1*d5*c12 - d2*d3*c1*c2);
                double G1 = f1*s1*(d2*d6*c1 - d3*d5*c12*c2) + f2*s2*(d3*d4*c2 - d2*d5*c12*c1);
                double G2 = f2*s2*(d1*d5*c12 - d2*d3*c1*c2) - f1*s1*(d1*d6 - d3*d3*c2*c2);
                double G3 = f1*s1*(d1*d5*c12 - d2*d3*c1*c2) - f2*s2*(d1*d4 - d2*d2*c1*c1);
                double B1 = d5*d5*c12*c12 - d4*d6;
                double B2 = d2*d6*c1 - d3*d5*c12*c2;
                double B3 = d3*d4*c2 - d2*d5*c12*c1;
                double ddth0 = invDdet * (A12*dth1 + A13*dth2 + G1 + B1*u);
                double ddth1 = invDdet * (A22*dth1 + A23*dth2 + G2 + B2*u);
                double ddth2 = invDdet * (A32*dth1 + A33*dth2 + G3 + B3*u);

                th0 = th0 + h*dth0 + 0.5*h*h*ddth0;
                th1 = th1 + h*dth1 + 0.5*h*h*ddth1;
                th2 = th2 + h*dth2 + 0.5*h*h*ddth2;
                dth0 = 0.9995*dth0 + h*ddth0;
                dth1 = 0.9995*dth1 + h*ddth1;
                dth2 = 0.9995*dth2 + h*ddth2;
            }
        }

        // Returns the state x = [theta0, theta1, theta2, dtheta0, dtheta1, dtheta2]
        private synchronized double[] getFullState() {
            return new double[]{th0, th1, th2, dth0, dth1, dth2};
        }

        private synchronized void setControlSignal(double u) {
            if (u == 0.0)
                this.u = 0.0;
            else if (u > ulim)
                this.u = ulim;
            else if (u < -ulim)
                this.u = -ulim;
            else
                this.u = u + 0.001 * rg.nextGaussian();
        }

        // Used by gui (input is in degrees)
        private void setOffsetTh1(double th1Off) { this.th1Off = th1Off * PI / 180.0; }
        private void setOffsetTh2(double th2Off) { this.th2Off = th2Off * PI / 180.0; }

        private void reset(ModeMonitor.Mode mode) {
            th0 = 0.0; 
            dth0 = 0.0; 
            dth1 = 0.0; 
            dth2 = 0.0;
            u = 0.0;

            th1 = (mode == ModeMonitor.Mode.OFF) ? init[mode.ordinal()][0] : init[mode.ordinal()][0] + th1Off;
            th2 = (mode == ModeMonitor.Mode.OFF) ? init[mode.ordinal()][1] : init[mode.ordinal()][1] + th2Off;
        }
    }
}
