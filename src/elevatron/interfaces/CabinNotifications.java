package elevatron.interfaces;

public interface CabinNotifications {
    /**
     * Numeric floor button is pressed inside an elevator cabin.
     */
    void onButtonPressed(int elevatorIdx, int floorIdx);

    /**
     * Sends true or false when cabin door sensor changes the state.
     */
    void onDoorSensorStatusChange(int elevatorIdx, boolean obstacleDetected);

    /**
     * Called when numeric indicator changes it's state
     */
    void onNumericIndicatorChange(int idx, int numericValue);

    /**
     * Called when direction indicator changes it's state
     */
    void onDirectionIndicatorChange(int idx, int direction);
}
