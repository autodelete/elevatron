package elevatron.interfaces;

/**
 * Interface to the hardware of the elevator system.
 */
public interface ElevatorSystem {
    /** Returns the clock device */
    Clock getClock();

    /** Return all elevators */
    Elevator[] getElevators();

    /** Returns all floors */
    Floor[] getFloors();
}
