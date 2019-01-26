package elevatron.simulator;

import elevatron.interfaces.Cabin;
import elevatron.interfaces.Elevator;
import elevatron.interfaces.Shaft;

public class ElevatorSimulator implements Elevator {
    private CabinSimulator cabin;
    private ShaftSimulator shaft;

    private final int idx;
    private final int numFloors;

    public ElevatorSimulator(int idx, int numFloors, ClockSimulator clock) {
        this.idx = idx;
        this.numFloors = numFloors;
        this.cabin = new CabinSimulator(idx, numFloors, clock);
        this.shaft = new ShaftSimulator(idx, numFloors, clock);
    }

    @Override
    public Cabin getCabin() {
        return cabin;
    }

    @Override
    public Shaft getShaft() {
        return shaft;
    }

    @Override
    public int getIdx() {
        return idx;
    }
}