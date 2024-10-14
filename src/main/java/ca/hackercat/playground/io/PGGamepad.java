package ca.hackercat.playground.io;

import ca.hackercat.logging.Logger;
import com.github.strikerx3.jxinput.XInputAxes;
import com.github.strikerx3.jxinput.XInputButtons;
import com.github.strikerx3.jxinput.XInputDevice;
import com.github.strikerx3.jxinput.enums.XInputAxis;
import com.github.strikerx3.jxinput.enums.XInputButton;

public class PGGamepad {

    public enum Axis {
        LEFT_STICK_X,
        LEFT_STICK_Y,
        RIGHT_STICK_X,
        RIGHT_STICK_Y
    }

    public enum Button {

    }

    private static final Logger LOGGER = Logger.get(PGGamepad.class);

    private int playerNum;
    private XInputDevice device;

    private PGGamepad(int playerNum) {
        this.playerNum = playerNum;
//        try {
//            device = XInputDevice.getDeviceFor(playerNum);
//        }
//        catch (XInputNotLoadedException e) {
//            LOGGER.warn("XInput is not available!");
//        }
    }
    private static PGGamepad[] instances = new PGGamepad[4];

    public static PGGamepad get(int playerNum) {
        if (instances[playerNum - 1] == null) {
            instances[playerNum - 1] = new PGGamepad(playerNum);
        }
        return instances[playerNum - 1];
    }

    public double getAxis(XInputAxis axis) {
        XInputAxes axes = device.getComponents().getAxes();
        return axes.get(axis);
    }
    public boolean isButtonHeld(XInputButton button) {
        XInputButtons buttons = device.getComponents().getButtons();
        return false;
    }
    public boolean isButtonPressed(XInputButton button) {
        return false;
    }

}
