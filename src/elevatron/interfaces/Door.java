package elevatron.interfaces;

/** Interface to control door's hardware */
public interface Door {
    /** Starts closing this door. Overrides all previous commands. */
    void startClosing();

    /** Starts opening this door. Overrides all previous commands. */
    void startOpening();

    /** Subscribe to receive notifications from the door hardware. */
    void subscribe(DoorNotifications subscriber);
}
