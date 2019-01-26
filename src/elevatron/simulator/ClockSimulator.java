package elevatron.simulator;

import elevatron.interfaces.Clock;
import elevatron.interfaces.ClockNotifications;

import java.util.ArrayList;

public class ClockSimulator implements Clock {

    private static final double DT = 0.01; // 10ms
    private final ArrayList<ClockNotifications> subscribers = new ArrayList<>();
    private final ArrayList<TickReceiver> tickReceivers = new ArrayList<>();
    private double now = 0.0;
    private double alarm = Double.MAX_VALUE;

    @Override
    public void setAlarm(double secondsFromNow) {
        alarm = now + secondsFromNow;
    }

    @Override
    public double now() {
        return now;
    }

    @Override
    public void subscribe(ClockNotifications subscriber) {
        subscribers.add(subscriber);
    }

    public void subscribe(TickReceiver subscriber) {
        tickReceivers.add(subscriber);
    }

    public void advanceTime(double newNow) {
        assert newNow >= now;
        while (now < newNow) {
            now += DT;
            tick(DT);
        }
    }

    private void tick(double dt) {
        if (now >= alarm) {
            alarm = Double.MAX_VALUE;
            for (ClockNotifications s : subscribers) {
                s.onAlarmTriggered();
            }
        }
        for (TickReceiver s : tickReceivers) {
            s.tick(dt);
        }
    }
}
