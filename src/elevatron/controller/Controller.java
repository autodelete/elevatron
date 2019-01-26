package elevatron.controller;

import elevatron.interfaces.ClockNotifications;
import elevatron.interfaces.ElevatorSystem;

import java.util.Random;

public class Controller implements ClockNotifications {
    private final ElevatorSystem system;
    Random random = new Random();
    public Controller(ElevatorSystem system) {
        this.system = system;
        this.system.getClock().setAlarm(1.0); // invoke onAlarmTriggered after 1 second

        this.system.getClock().subscribe(this);
    }

    @Override
    public void onAlarmTriggered() {
        // Sending random commands to motors
        system.getElevators()[randomElevatorIdx()].getShaft().controlShaftMotor(random.nextDouble()-0.5);
        // Closing and opening random shaft doors
        system.getElevators()[randomElevatorIdx()].getShaft().getDoors()[randomFloorIdx()].startOpening();
        system.getElevators()[randomElevatorIdx()].getShaft().getDoors()[randomFloorIdx()].startClosing();
        // Closing and opening cabin doors
        system.getElevators()[randomElevatorIdx()].getCabin().getDoor().startOpening();
        system.getElevators()[randomElevatorIdx()].getCabin().getDoor().startClosing();
        // Making some light to blink
        for (int i=0; i<10; i++) {
            // Lights on buttons inside a cabin
            system.getElevators()[randomElevatorIdx()].getCabin().getFloorButtons()[randomFloorIdx()]
                    .setLightState(random.nextBoolean());
            // UP/DOWN call buttons on the floors:
            system.getFloors()[randomFloorIdx()].getUpCallButton()
                    .setLightState(random.nextBoolean());
            system.getFloors()[randomFloorIdx()].getDownCallButton()
                    .setLightState(random.nextBoolean());
        }
        // Floor # indicators
        system.getElevators()[randomElevatorIdx()].getCabin().setNumericIndicator(randomFloorIdx());
        // Direction indicators
        system.getElevators()[randomElevatorIdx()].getCabin().setDirectionIndicator(random.nextInt(3)-2);

        // Set alarm to trigger again in 1/10 second to repeat all this nonsense again
        system.getClock().setAlarm(0.1);
    }

    private int randomFloorIdx() {
        return random.nextInt(system.getFloors().length);
    }

    private int randomElevatorIdx() {
        return random.nextInt(system.getElevators().length);
    }
}
