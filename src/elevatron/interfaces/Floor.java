package elevatron.interfaces;

public interface Floor {
    /** return floor index */
    int getIdx();

    /** Returns button which allows to call the elevator to UP direction */
    Button getUpCallButton();
    /** Returns button which allows to call the elevator to DOWN direction */
    Button getDownCallButton();
}
