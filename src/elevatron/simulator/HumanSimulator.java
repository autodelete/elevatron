package elevatron.simulator;

import elevatron.interfaces.Cabin;
import elevatron.interfaces.Elevator;
import elevatron.interfaces.ElevatorSystem;
import elevatron.interfaces.Floor;

import java.util.ArrayList;
import java.util.Random;
import static java.lang.Math.abs;

public class HumanSimulator {
    private final ClockSimulator clock;
    private final ElevatorSystem system;
    private final Random random;
    private final int numFloors;
    private final int numElevators;
    private final double[] floorYs;
    private final double callButtonsX;
    private final double[] elevatorXs;
    double subsecond = 0.0;

    private Floor targetFloor = null;
    private Floor currentFloor = null;
    private Cabin cabin = null;
    private State state;
    private double x;
    private double prevReportedX = Double.MAX_VALUE;
    private double y;
    private double prevReportedY = Double.MAX_VALUE;
    ArrayList<HumanNotifications> subscribers = new ArrayList<>();
    boolean first = true;


    public HumanSimulator(ClockSimulator clock, ElevatorSystemSimulator system, Random random,
                          double minX, double maxX, double[] floorYs, double callButtonsX, double[] elevatorXs) {
        this.clock = clock;
        this.system = system;
        this.random = random;
        numFloors = system.getFloors().length;
        numElevators = system.getElevators().length;
        this.floorYs = floorYs;
        this.callButtonsX = callButtonsX;
        this.elevatorXs = elevatorXs;
        this.currentFloor = choseRandomFloor();
        this.y = floorYs[currentFloor.getIdx()];
        this.x = random.nextDouble()*(maxX-minX)+minX;
        this.targetFloor = choseRandomFloor();
        clock.subscribe(this::tick);
        state = new Idle();
        state.onEnter();
    }

    public void subscribe(HumanNotifications subscriber) {
        subscribers.add(subscriber);
    }

    private Floor choseRandomFloor() {
        return system.getFloors()[random.nextInt(numFloors)];
    }

    private void tick(double dt) {
        subsecond += dt;
        state.tick(dt);
        if (subsecond >= 1.0) {
            // every sec:
            subsecond = 0.0;
            state.everySecond();
            // on average each 100 sec change decision
            if (random.nextDouble() < 0.01) {
                this.targetFloor = choseRandomFloor();
            }
        }
        if (first || x != prevReportedX || y != prevReportedY) {
            prevReportedX = x;
            prevReportedY = y;
            for (HumanNotifications s : subscribers) {
                s.onPositionChanged(x, y);
            }
        }
        first = false;
    }

    void changeState(State newState) {
        state = newState;
        state.onEnter();
    }

    public boolean isUpCallButtonPressed() {
        return ((ButtonSimulator)currentFloor.getUpCallButton())
                .getLightState();
    }

    public boolean isDownCallButtonPressed() {
        return ((ButtonSimulator)currentFloor.getDownCallButton())
                .getLightState();
    }
    abstract class State {
        public void tick(double dt) {}
        public void everySecond() {}
        public void onEnter() {}

    }

    abstract class Walking extends State {
        private double targetX;
        private final double speed;


        public Walking(double targetX) {
            this.targetX = targetX;
            speed = random.nextInt(20)+20;
        }

        public Walking() {
            this(x);
        }

        @Override
        public void tick(double dt) {
            if (x < targetX) {
                x += speed * dt;
                if (x > targetX) {
                    x = targetX;
                    doneWalking();
                }
            } else {
                x -= speed * dt;
                if (x < targetX) {
                    x = targetX;
                    doneWalking();
                }
            }
        }

        protected abstract void doneWalking();
        protected void setTarget(double x) {
            targetX = x;
        }

    }
    class Idle extends WanderingState {
        @Override
        public void tick(double dt) {
            if (currentFloor != targetFloor) {
                changeState(new WalkingToCallButtons());
                return;
            }
            super.tick(dt);
        }

    }
    class WalkingToCallButtons extends Walking {


        public WalkingToCallButtons() {
            super(callButtonsX);
        }

        @Override
        public void tick(double dt) {
            if (targetFloor.getIdx() == currentFloor.getIdx()) {
                changeState(new GoingToIdle());
                return;
            }
            if (isUpCallButtonPressed() && targetFloor.getIdx() > currentFloor.getIdx()
                    || isDownCallButtonPressed() && targetFloor.getIdx() < currentFloor.getIdx()
            ) {
                changeState(new WaitingForCabinToArrive());
                return;
            }
            super.tick(dt);
        }
        @Override
        protected void doneWalking() {
            ((ButtonSimulator)currentFloor.getUpCallButton()).press();
            changeState(new WaitingForCabinToArrive());
        }

    }
    private class GoingToIdle extends State {

    }
    private class WaitingForCabinToArrive extends WanderingState {

        @Override
        public void tick(double dt) {
            for (int i=0; i<numElevators; i++) {
                DoorSimulator door =
                        (DoorSimulator) system.getElevators()[i].getShaft().getDoors()[currentFloor.getIdx()];
                if (door.getCurrentPosition() >= 0.9) {
                    changeState(new WalkingToElevator(system.getElevators()[i]));
                    return;
                }
            }
            super.tick(dt);
        }
    }

    private class WanderingState extends Walking {
        double wanderingDelta = random.nextDouble() * 50.0 + 25.0;
        double origX = x;

        @Override
        protected void doneWalking() {
            if (x < origX) {
                super.setTarget(origX + wanderingDelta);
            } else {
                super.setTarget(origX - wanderingDelta);
            }
        }
    }

    private class WalkingToElevator extends Walking {
        private final Elevator targetElevator;
        public WalkingToElevator(Elevator targetElevator) {
            super(elevatorXs[targetElevator.getIdx()]);
            this.targetElevator = targetElevator;
        }

        @Override
        protected void doneWalking() {
            if (((DoorSimulator)targetElevator.getShaft().getDoors()[currentFloor.getIdx()]).getCurrentPosition() < 0.1
              || ((DoorSimulator)targetElevator.getCabin().getDoor()).getCurrentPosition() < 0.1) {
                changeState(new WalkingToCallButtons());
            } else {
                changeState(new EnteringCabin(targetElevator));
            }
        }
    }

    private class EnteringCabin extends Walking {
        private final Elevator targetElevator;
        double targetY = y - 5;
        double speed = 4;
        public EnteringCabin(Elevator targetElevator) {
            super(x + random.nextDouble() * 30 - 15);
            this.targetElevator = targetElevator;
        }

        @Override
        public void tick(double dt) {
            super.tick(dt);
            y-=speed*dt;
            if (y<targetY) {
                y = targetY;
                if (abs(((ShaftSimulator)targetElevator.getShaft()).getVerticalPosition()-currentFloor.getIdx()) < 0.15) {
                    changeState(new RidingCabin(targetElevator));
                } else {
                    changeState(new Falling());
                }
            }
        }

        @Override
        protected void doneWalking() {}
    }

    private class RidingCabin extends State {
        private final Elevator elevator;
        private double origElevatorPos;
        private double origY = y;

        public RidingCabin(Elevator targetElevator) {
            this.elevator = targetElevator;
            this.origElevatorPos = ((ShaftSimulator)elevator.getShaft()).getVerticalPosition();
        }

        @Override
        public void tick(double dt) {
            double pos = ((ShaftSimulator)elevator.getShaft()).getVerticalPosition();
            y = origY + (pos - origElevatorPos)*(floorYs[1]-floorYs[0]);
        }

        @Override
        public void everySecond() {
            ButtonSimulator floorButton = (ButtonSimulator) elevator.getCabin().getFloorButtons()[targetFloor.getIdx()];
            if (!floorButton.getLightState()) {
                floorButton.press();
            }
            if (
                    ((DoorSimulator)elevator.getShaft().getDoors()[targetFloor.getIdx()]).getCurrentPosition() > 0.5 &&
                    ((DoorSimulator)elevator.getCabin().getDoor()).getCurrentPosition() > 0.5 &&
                    abs(((ShaftSimulator)elevator.getShaft()).getVerticalPosition()-targetFloor.getIdx()) < 0.15) {
                changeState(new LeavingCabin());
            }
        }
    }

    private class LeavingCabin extends State {
        double targetY = floorYs[targetFloor.getIdx()];
        double speed = 4;


        @Override
        public void tick(double dt) {
            super.tick(dt);
            if (y < targetY) {
                y += speed * dt;
                if (y > targetY) {
                    done();
                }
            } else {
                y -= speed * dt;
                if (y < targetY) {
                    done();
                }
            }
        }

        void done() {
            changeState(new Idle());
        }
    }

    private class Falling extends State {
        double v = 0.0;
        @Override
        public void tick(double dt) {
            super.tick(dt);
            y += v*dt;
            v += 100*dt;
        }

    }
}
