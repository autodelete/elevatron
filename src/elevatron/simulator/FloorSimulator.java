package elevatron.simulator;

import elevatron.interfaces.Button;
import elevatron.interfaces.Floor;

public class FloorSimulator implements Floor {
    private final int idx;
    private final int numElevators;
    private final ButtonSimulator upCallButton;
    private final ButtonSimulator downCallButton;

    public FloorSimulator(int idx, int numElevators, ClockSimulator clock) {
        this.idx = idx;
        this.numElevators = numElevators;
        this.upCallButton = new ButtonSimulator(clock);
        this.downCallButton = new ButtonSimulator(clock);
    }

    @Override
    public int getIdx() {
        return idx;
    }

    @Override
    public Button getUpCallButton() {
        return upCallButton;
    }

    @Override
    public Button getDownCallButton() {
        return downCallButton;
    }
}
