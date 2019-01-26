package elevatron.interfaces;

public interface Button {
    /** Allow to turn ON/OFF button light */
    void setLightState(boolean isOn);

    void subscribe(ButtonNotifications subscriber);
}
