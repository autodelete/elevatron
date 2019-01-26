package elevatron.interfaces;

/** Interface to control elevator's abin hardware */
public interface Cabin {
    Door getDoor();
    /**
     * Set the numeric indicator of the floor displayed inside the cabin and on all floors.
     * Setting to -1 to turns off the indicator
     */
    void setNumericIndicator(int number);

    /**
     * Set the direction indicator of the floor displayed inside the elevator and on all floors.
     * -1 - display down arrow
     *  0 - do not display anything
     *  1 - display up arrow
     */
    void setDirectionIndicator(int direction);

    /**
     * Returns array of buttons #i-s button corresponds to #i-s floor.
     */
    Button[] getFloorButtons();

    /**
     * Subscribe to receive notifications.
     */
    void subscribe(CabinNotifications subscriber);
}
