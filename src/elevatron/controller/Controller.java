package elevatron.controller;

import elevatron.interfaces.*;
import java.util.Random;

import static java.lang.Math.*;

public class Controller implements ClockNotifications {
    private final ElevatorSystem system;
    private Random random = new Random();

    private int numFloors;
    private int numElevators;

    private double[] subSecond;
    private double dt = 0.1;

    private boolean[] firstIteration;
    private boolean[] cabinDoorOpened;
    private boolean[] shaftDoorOpened;

    private int[] prevFloorIdx;
    private int[] currFloorIdx;
    private int[] nextFloorIdx;

    //private Cabin cabin;
    //private Door cabinDoor;
    //private Shaft shaft;
    //private Door shaftDoorAtFloor;
    private Door[] shaftDoorAtFloor;

    public Controller(ElevatorSystem system) {
        this.system = system;

        numFloors = system.getFloors().length;
        numElevators = system.getElevators().length;

        subSecond = new double[numElevators];
        firstIteration = new boolean[numElevators];
        cabinDoorOpened = new boolean[numElevators];

        prevFloorIdx = new int[numElevators];
        currFloorIdx = new int[numElevators];
        nextFloorIdx = new int[numElevators];

        for (int i = 0; i < numElevators; i++) {
            subSecond[i] = 0.0;

            firstIteration[i] = true;
            cabinDoorOpened[i] = false;

            prevFloorIdx[i] = -1;
            currFloorIdx[i] = 0;
            nextFloorIdx[i] = 1;
        }

        shaftDoorAtFloor = new Door[numFloors];
        shaftDoorOpened = new boolean[numFloors];
        for (int i = 0; i < numFloors; i++) {
            shaftDoorOpened[i] = false;
        }

        this.system.getClock().setAlarm(1.0); // invoke onAlarmTriggered after 1 second
        this.system.getClock().subscribe(this);
    }

    @Override
    public void onAlarmTriggered() {
        //int elevatorIdx = randomElevatorIdx();
        //int elevatorIdx = 0;

        //for (int elevatorIdx = 0; elevatorIdx < numElevators; elevatorIdx++) {
        for (int elevatorIdx = 0; elevatorIdx < 1; elevatorIdx++) {
            Elevator elevator = system.getElevators()[elevatorIdx];

            Cabin cabin = elevator.getCabin();
            Door cabinDoor = cabin.getDoor();
            Shaft shaft = elevator.getShaft();
            shaftDoorAtFloor[currFloorIdx[elevatorIdx]] = shaft.getDoors()[currFloorIdx[elevatorIdx]];

            if (firstIteration[elevatorIdx]) {
                shaftDoorAtFloor[currFloorIdx[elevatorIdx]].startOpening();
                firstIteration[elevatorIdx] = false;
            }

            shaftDoorAtFloor[currFloorIdx[elevatorIdx]].subscribe(new DoorNotifications() {
                @Override
                public void onDoorPositionChange(int elevatorIdx, int floorIdx, double position) {
                    if (floorIdx == -1) {
                        return;
                    }

                    if (position == 1.0 && !cabinDoorOpened[elevatorIdx]) {
                        shaftDoorOpened[elevatorIdx] = true;
                        cabinDoor.startOpening();
                    } else if (position == 0.0) {
                        shaftDoorOpened[elevatorIdx] = false;

                        if (nextFloorIdx[elevatorIdx] > floorIdx) {
                            shaft.controlShaftMotor(0.5); // todo: compute acceleration
                            cabin.setDirectionIndicator(1);
                        } else if (nextFloorIdx[elevatorIdx] < floorIdx) {
                            shaft.controlShaftMotor(-0.5);
                            cabin.setDirectionIndicator(-1);
                        } else {
                            shaft.stopShaftMotor();
                            cabin.setDirectionIndicator(0);
                        }
                    }
                }
            });

            cabinDoor.subscribe(new DoorNotifications() {
                @Override
                public void onDoorPositionChange(int elevatorIdx, int floorIdx, double position) {
                    if (floorIdx != -1) {
                        return;
                    }

                    if (position == 1.0) {
                        cabinDoorOpened[elevatorIdx] = true;
                    } else if (position == 0.0 && shaftDoorOpened[elevatorIdx]) {
                        cabinDoorOpened[elevatorIdx] = false;
                        shaftDoorAtFloor[currFloorIdx[elevatorIdx]].startClosing();
                    }
                }
            });

            int finalElevatorIdx = elevatorIdx;
            cabin.subscribe(new CabinNotifications() {
                @Override
                public void onNumericIndicatorChange(int idx, int numericValue) {
                    if (numericValue != currFloorIdx[finalElevatorIdx] && cabinDoorOpened[finalElevatorIdx]) {
                        cabinDoor.startClosing();
                    }
                }

                @Override
                public void onDirectionIndicatorChange(int idx, int direction) {
                }

                @Override
                public void onButtonPressed(int elevatorIdx, int floorIdx) {
                }

                @Override
                public void onDoorSensorStatusChange(int elevatorIdx, boolean obstacleDetected) {
                }
            });

            if (subSecond[elevatorIdx] >= randomWait()) {
                cabin.setNumericIndicator(nextFloorIdx[elevatorIdx]);

                cabin.getFloorButtons()[currFloorIdx[elevatorIdx]].setLightState(false);
                cabin.getFloorButtons()[nextFloorIdx[elevatorIdx]].setLightState(true);

                subSecond[elevatorIdx] = 0.0;
            }

            shaft.subscribe(new ShaftNotifications() {
                @Override
                public void onCabinVerticalPositionChange(int elevatorIdx, double position) {
                    int currFloor;
                    if (nextFloorIdx[elevatorIdx] >= currFloorIdx[elevatorIdx]) {
                        currFloor = (int) floor(position);
                    } else {
                        currFloor = (int) ceil(position);
                    }

                    if (abs(prevFloorIdx[elevatorIdx] - currFloor) > 0.1) {
                        Floor floor = system.getFloors()[currFloorIdx[elevatorIdx]];
                        if (prevFloorIdx[elevatorIdx] != -1) {
                            Floor prevFloor = system.getFloors()[prevFloorIdx[elevatorIdx]];
                            prevFloor.getDownCallButton().setLightState(false);
                            prevFloor.getUpCallButton().setLightState(false);
                        }

                        if (nextFloorIdx[elevatorIdx] > currFloorIdx[elevatorIdx]) {
                            floor.getDownCallButton().setLightState(false);
                            floor.getUpCallButton().setLightState(true);
                        } else if (nextFloorIdx[elevatorIdx] < currFloorIdx[elevatorIdx]) {
                            floor.getUpCallButton().setLightState(false);
                            floor.getDownCallButton().setLightState(true);
                        }
                    }

                    if (currFloor == nextFloorIdx[elevatorIdx]) {
                        shaft.stopShaftMotor();

                        shaftDoorAtFloor[currFloorIdx[elevatorIdx]] = shaft.getDoors()[currFloor];
                        shaftDoorAtFloor[currFloorIdx[elevatorIdx]].startOpening();
                        cabin.setDirectionIndicator(0);

                        prevFloorIdx[elevatorIdx] = currFloorIdx[elevatorIdx];
                        currFloorIdx[elevatorIdx] = currFloor;

                        nextFloorIdx[elevatorIdx] = randomFloorIdx();

                        subSecond[elevatorIdx] = 0.0;
                    }
                }
            });

            subSecond[elevatorIdx] += dt;
        }

        system.getClock().setAlarm(dt);
    }

    private int randomFloorIdx() {
        return random.nextInt(numFloors);
    }

    private int randomElevatorIdx() {
        return random.nextInt(numElevators);
    }

    private double randomWait() {
        return 5.0 + 15.0 * random.nextDouble();
    }
}
