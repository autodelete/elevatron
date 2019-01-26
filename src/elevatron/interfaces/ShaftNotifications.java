package elevatron.interfaces;

public interface ShaftNotifications {
    /**
     * Notifies when the elevator's cabin changes its vertical position.
     * Integer numbers represent position which perfectly matches the given floor.
     * Fraction values - elevator between floors.
     * 0.0 floor #0
     * 2.0 floor #2
     * 2.5 in between of 2 an 3.
     *
     * This notification is called at least once at the beginning to notify the
     * elevator system controller about initial position of each elevator
     */
    void onCabinVerticalPositionChange(int elevatorIdx, double position);
}
