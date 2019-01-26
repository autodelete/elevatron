package elevatron.simulator;

import elevatron.interfaces.*;

import java.util.ArrayList;
import java.util.stream.IntStream;

public class ElevatorSystemSimulator implements ElevatorSystem {
    final ElevatorSimulator[] elevators;
    final FloorSimulator[] floors;
    final ClockSimulator clock = new ClockSimulator();
    final private ArrayList<HardwareEventsNotificationSink> eventSinks = new ArrayList<>();

    public ElevatorSystemSimulator(int numFloors, int numElevators) {
        elevators =
                IntStream.range(0, numElevators)
                        .mapToObj(i -> new ElevatorSimulator(i, numFloors, clock))
                        .toArray(ElevatorSimulator[]::new);
        floors =
                IntStream.range(0, numFloors)
                        .mapToObj(i -> new FloorSimulator(i, numElevators, clock))
                        .toArray(FloorSimulator[]::new);
    }

    @Override
    public Clock getClock() {
        return clock;
    }

    @Override
    public Elevator[] getElevators() {
        return elevators.clone();
    }

    @Override
    public Floor[] getFloors() {
        return floors.clone();
    }

    public void advanceTime(double newNow) {
        clock.advanceTime(newNow);
    }

}
