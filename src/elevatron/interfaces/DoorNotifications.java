package elevatron.interfaces;

public interface DoorNotifications {
    /**
     * Notifies when door changes its position.
     * [0.0..1.0] - 0.0 fully closed  - 1.0 fully open
     * <p>
     * If floorIdx == -1 then this status change event is coming
     * from the cabin door, otherwise this is the shaft door at floorIdx
     */
    void onDoorPositionChange(int elevatorIdx, int floorIdx, double position);
}
