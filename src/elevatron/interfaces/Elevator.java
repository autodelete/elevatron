package elevatron.interfaces;

/** Interface to control the elevator hardware */
public interface Elevator {
    /** Returns interface to control this elevator's cabin */
    Cabin getCabin();

    /** Returns interface to control this elevator's shaft */
    Shaft getShaft();

    /** Returns index of this elevator; */
    int getIdx();
}
