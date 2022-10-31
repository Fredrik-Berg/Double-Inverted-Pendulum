/**
 *  class ModeMonitor:
 *
 * Monitor class for the mode currently selected
 */
public class ModeMonitor {

    private Mode mode = Mode.OFF;

    /**
     * Synchronized method setMode:
     *
     * @param:
     *     newMode (Mode): Sets the new mode to be newMode
     */
    public synchronized void setMode(Mode newMode) {
        mode = newMode;
    }

    /**
     * Synchronized method getMode:
     *
     * @return:
     *     Mode: The current mode in the ModeMonitor
     */
    public synchronized Mode getMode() {
        return mode;
    }

    /**
     *  enum Mode:
     *
     * Mode enumerator
     */
    public enum Mode {
        // D = Down, U = Up
        DD, DU, UD, UU, OFF; // The enum values are DD = 0, DU = 1, UD = 2, UU = 3, OFF = 4;
    }
}


