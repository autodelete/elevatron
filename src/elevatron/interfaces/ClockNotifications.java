package elevatron.interfaces;

public interface ClockNotifications {
    /**
     * Called when the alarm on the clock is triggered.
     * NOTE: This is called once - if you want repeated invocations set up the timer again
     */
    void onAlarmTriggered();
}
