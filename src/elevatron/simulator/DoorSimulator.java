package elevatron.simulator;

import elevatron.interfaces.Door;
import elevatron.interfaces.DoorNotifications;

import java.util.ArrayList;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class DoorSimulator implements Door{
    private static double DOOR_SPEED = 0.5;
    private double currentPosition = 0.0;
    private double lastReportedDoorPosition = 0.0;
    private double direction = 0.0;
    private final ArrayList<DoorNotifications> subscribers = new ArrayList<>();
    private final int elevatorIdx;
    private final int floorIdx;

    public DoorSimulator(int elevatorIdx, int floorIdx, ClockSimulator clock) {
        this.elevatorIdx = elevatorIdx;
        this.floorIdx = floorIdx;
        clock.subscribe(this::tick);
    }

    private void tick(double dt) {
        currentPosition = max(0.0, min(1.0,
                currentPosition + direction * DOOR_SPEED * dt));

        if (currentPosition != lastReportedDoorPosition) {
            lastReportedDoorPosition = currentPosition;
            for (DoorNotifications s : subscribers) {
                s.onDoorPositionChange(elevatorIdx, floorIdx, currentPosition);
            }
        }
    }

    @Override
    public void startClosing() {
        this.direction = -1;
    }

    @Override
    public void startOpening() {
        this.direction = 1;
    }

    @Override
    public void subscribe(DoorNotifications subscriber) {
        subscribers.add(subscriber);
    }

    public double getCurrentPosition() {
        return currentPosition;
    }
}
