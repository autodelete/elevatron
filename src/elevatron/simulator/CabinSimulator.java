package elevatron.simulator;

import elevatron.interfaces.*;

import java.util.ArrayList;
import java.util.stream.IntStream;

public class CabinSimulator implements Cabin {
    private final DoorSimulator door;
    private final int idx;
    private final ArrayList<HardwareEventsNotificationSink> eventSinks = new ArrayList<>();
    private final int numFloors;
    private final Button[] floorButtons;
    private int numericIndicator = -1;
    private int directionIndicator = 0;
    private final ArrayList<CabinNotifications> subscribers = new ArrayList<>();
    private boolean first = true;
    private int lastReportedNumericIndicator = -1;
    private int lastReportedDirectionIndicator = 0;

    public CabinSimulator(int idx, int numFloors, ClockSimulator clock) {
        this.idx = idx;
        this.numFloors = numFloors;
        door = new DoorSimulator(idx, -1, clock);
        floorButtons =
                IntStream.range(0, numFloors)
                        .mapToObj(i -> new ButtonSimulator(clock))
                        .toArray(Button[]::new);
        clock.subscribe(this::tick);
    }

    @Override
    public Door getDoor() {
        return door;
    }

    @Override
    public void setNumericIndicator(int number) {
        numericIndicator = number;
    }

    @Override
    public void setDirectionIndicator(int direction) {
        assert direction >= -1 && direction <= 1;
        directionIndicator = direction;
    }

    @Override
    public Button[] getFloorButtons() {
        return floorButtons;
    }

    @Override
    public void subscribe(CabinNotifications subscriber) {
        subscribers.add(subscriber);
        first = true;
    }

    private void tick(double dt) {
        if (first || numericIndicator != lastReportedNumericIndicator) {
            lastReportedNumericIndicator = numericIndicator;
            for (CabinNotifications s : subscribers) {
                s.onNumericIndicatorChange(idx, numericIndicator);
            }
        }
        if (first || directionIndicator != lastReportedDirectionIndicator) {
            lastReportedDirectionIndicator = directionIndicator;
            for (CabinNotifications s : subscribers) {
                s.onDirectionIndicatorChange(idx, directionIndicator);
            }
        }
        first = false;
    }
}
