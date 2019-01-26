package elevatron.interfaces;

public interface FloorNotifications {
    /**
     * Invoked when call button pressed on a floor
     * direction: -1 down,  1 up
     */
    void onCallButtonPressed(int floorIdx, int direction);
}
