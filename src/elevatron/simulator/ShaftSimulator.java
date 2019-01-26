package elevatron.simulator;

import elevatron.interfaces.Door;
import elevatron.interfaces.Shaft;
import elevatron.interfaces.ShaftNotifications;

import java.util.ArrayList;
import java.util.stream.IntStream;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class ShaftSimulator implements Shaft {
    private final int idx;
    private final int numFloors;
    double currentVerticalPosition = 0.0;
    double lastReportedVerticalPosition = 0.0;
    double verticalVelocity = 0.0;
    double verticalAcceleration = 0.0;

    public final DoorSimulator[] doors;

    private final ArrayList<ShaftNotifications> subscribers = new ArrayList<>();

    public ShaftSimulator(int idx, int numFloors, ClockSimulator clock) {
        this.idx = idx;
        this.numFloors = numFloors;
        doors =
                IntStream.range(0, numFloors)
                        .mapToObj(i -> new DoorSimulator(idx, i, clock))
                        .toArray(DoorSimulator[]::new);
        clock.subscribe(this::tick);
    }

    @Override
    public void controlShaftMotor(double acceleration) {
        assert acceleration >= -4.0f && acceleration <= 4.0;
        this.verticalAcceleration = acceleration;
    }

    @Override
    public Door[] getDoors() {
        return doors;
    }

    @Override
    public void subscribe(ShaftNotifications subscriber) {
        subscribers.add(subscriber);
    }

    private void tick(double dt) {
        verticalVelocity = max(-4.0, min(4.0,
                verticalVelocity + verticalAcceleration * dt));
        currentVerticalPosition = max(-0.5, min(numFloors - 0.5,
                currentVerticalPosition + verticalVelocity * dt));

        if (currentVerticalPosition != lastReportedVerticalPosition) {
            lastReportedVerticalPosition = currentVerticalPosition;
            for (ShaftNotifications s : subscribers) {
                s.onCabinVerticalPositionChange(idx, currentVerticalPosition);
            }
        }
    }

    public double getVerticalPosition() {
        return currentVerticalPosition;
    }
}
