package elevatron.simulator;

import elevatron.interfaces.Button;
import elevatron.interfaces.ButtonNotifications;
import elevatron.interfaces.Clock;

import java.util.ArrayList;

public class ButtonSimulator implements Button {

    private boolean currentLightState = false;
    private boolean lastReportedLightState = false;
    private boolean first = true;
    private final ArrayList<ButtonNotifications> subscribers = new ArrayList<>();

    public ButtonSimulator(ClockSimulator clock) {
        clock.subscribe(this::tick);
    }

    @Override
    public void setLightState(boolean isOn) {
        currentLightState = isOn;
    }

    @Override
    public void subscribe(ButtonNotifications subscriber) {
        subscribers.add(subscriber);
        first = true;
    }

    public void press() {
        for (ButtonNotifications s : subscribers) {
            s.onPressed();
        }
    }

    private void tick(double dt) {
        if (first || currentLightState != lastReportedLightState) {
            first = false;
            lastReportedLightState = currentLightState;
            for (ButtonNotifications s : subscribers) {
                s.onLightStateChange(currentLightState);
            }
        }
    }

    public boolean getLightState() {
        return currentLightState;
    }
}
