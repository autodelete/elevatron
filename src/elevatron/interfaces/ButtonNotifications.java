package elevatron.interfaces;

public interface ButtonNotifications {
    /** Called when the button light state changed */
    void onLightStateChange(boolean isOn);

    /** Called when the button is pressed */
    void onPressed();
}
