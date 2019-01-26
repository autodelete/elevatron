package elevatron.interfaces;

/** Interface to control clock hardware. */
public interface Clock {
    /**
     * Sets the alarm timer to trigger after given amount of seconds.
     */
    void setAlarm(double secondsFromNow);

    /**
     * Returns current time in seconds from the system start.
     */
    double now();

    /**
     * Subscribe for alarm notifications.
     */
    void subscribe(ClockNotifications subscriber);
}
