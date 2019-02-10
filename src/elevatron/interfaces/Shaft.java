package elevatron.interfaces;

public interface Shaft {
    /** Controls the shaft motor (sets acceleration - floors/sec^2).
     * Positive values corresponds to UP direction.
     * allowed range [-4.0 .. 4.0]
     */
    void controlShaftMotor(double acceleration);

    /** Get all shaft doors (#i door corresponds to #is floor)*/
    Door[] getDoors();

    /** Subscribe to receive notifications from the shaft hardware. */
    void subscribe(ShaftNotifications subscriber);

    /** Stops shaft. TODO: possible with controlShaftMotor and acceleration only? */
    void stopShaftMotor();
}
