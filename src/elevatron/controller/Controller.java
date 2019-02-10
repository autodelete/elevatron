package elevatron.controller;

import elevatron.interfaces.*;
import java.util.Random;
import java.util.stream.IntStream;

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

    private int[] prevFloorIdx;
    private int[] currFloorIdx;
    private int[] nextFloorIdx;

    private Door[][] shaftDoors;
    private boolean[][] shaftDoorOpened;

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

        shaftDoors = new Door[numFloors][numElevators];
        shaftDoorOpened = new boolean[numFloors][numElevators];
        for (int i = 0; i < numFloors; i++) {
            for (int j = 0; j < numElevators; j++) {
                shaftDoorOpened[i][j] = false;
            }
        }

        this.system.getClock().setAlarm(dt);
        this.system.getClock().subscribe(this);
    }

    @Override
    public void onAlarmTriggered() {
        for (int eId = 0; eId < numElevators; eId++) {
            Elevator elevator = system.getElevators()[eId];

            Shaft shaft = elevator.getShaft();
            Cabin cabin = elevator.getCabin();
            Door cabinDoor = cabin.getDoor();

            int fId = currFloorIdx[eId];

            shaftDoors[fId][eId] = shaft.getDoors()[fId];
            if (firstIteration[eId]) {
                shaftDoors[fId][eId].startOpening();
                firstIteration[eId] = false;
            }

            shaftDoors[fId][eId].subscribe(new DoorNotifications() {
                @Override
                public void onDoorPositionChange(int elevatorIdx, int floorIdx, double position) {
                    if (floorIdx == -1) {
                        return;
                    }

                    if (position == 1.0 && !cabinDoorOpened[elevatorIdx]) {
                        shaftDoorOpened[floorIdx][elevatorIdx] = true;
                        cabinDoor.startOpening();
                    } else if (position == 0.0) {
                        shaftDoorOpened[floorIdx][elevatorIdx] = false;

                        if (nextFloorIdx[elevatorIdx] > floorIdx) {
                            shaft.controlShaftMotor(1.0); // todo: compute acceleration
                            cabin.setDirectionIndicator(1);
                        } else if (nextFloorIdx[elevatorIdx] < floorIdx) {
                            shaft.controlShaftMotor(-1.0);
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
                    } else if (position == 0.0 && shaftDoorOpened[fId][elevatorIdx]) {
                        cabinDoorOpened[elevatorIdx] = false;
                        shaftDoors[fId][elevatorIdx].startClosing();
                    }
                }
            });

            cabin.subscribe(new CabinNotifications() {
                @Override
                public void onNumericIndicatorChange(int idx, int numericValue) {
                    if (numericValue != currFloorIdx[idx] && cabinDoorOpened[idx]) {
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

            if (subSecond[eId] >= randomWait()) {
                cabin.setNumericIndicator(nextFloorIdx[eId]);

                IntStream.range(0, cabin.getFloorButtons().length)
                        .forEach(i -> cabin.getFloorButtons()[i].setLightState(false));

                cabin.getFloorButtons()[nextFloorIdx[eId]].setLightState(true);

                subSecond[eId] = 0.0;
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

                    if (abs(prevFloorIdx[elevatorIdx] - currFloor) > 0) {
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

                        shaftDoors[currFloorIdx[elevatorIdx]][elevatorIdx] = shaft.getDoors()[currFloor];
                        shaftDoors[currFloorIdx[elevatorIdx]][elevatorIdx].startOpening();
                        cabin.setDirectionIndicator(0);

                        prevFloorIdx[elevatorIdx] = currFloorIdx[elevatorIdx];
                        currFloorIdx[elevatorIdx] = currFloor;

                        nextFloorIdx[elevatorIdx] = randomFloorIdx();

                        subSecond[elevatorIdx] = 0.0;
                    }
                }
            });

            subSecond[eId] += dt;
        }

        system.getClock().setAlarm(dt);
    }

    private int randomFloorIdx() {
        return random.nextInt(numFloors);
    }

    private double randomWait() {
        return 1.0 + 11.0 * random.nextDouble();
    }
}
