package elevatron.controller;

import elevatron.interfaces.*;
import java.util.Random;
import java.util.stream.IntStream;

import static java.lang.Math.*;

public class Controller implements ClockNotifications {
    // region Private Class Fields

    // region Const

    private static final double RANDOM_WAIT_RANGE_START = 1.0;
    private static final double RANDOM_WAIT_RANGE_END = 12.0;
    private static final double TIME_DELTA = 0.1;

    // endregion

    // region Per System

    private final ElevatorSystem system;
    private Random random = new Random();

    private int numFloors;
    private int numElevators;

    // endregion

    // region Per Elevator

    private double[] timeline;
    private boolean[] firstIteration;
    private boolean[] cabinDoorOpened;

    private int[] prevFloorIdx;
    private int[] currFloorIdx;
    private int[] nextFloorIdx;

    // endregion

    // region Per Floor Per Elevator

    private Door[][] shaftDoors;
    private boolean[][] shaftDoorOpened;

    //endregion

    // endregion

    public Controller(ElevatorSystem system) {
        this.system = system;

        this.init(this.system);

        this.system.getClock().setAlarm(TIME_DELTA);
        this.system.getClock().subscribe(this);
    }

    @Override
    public void onAlarmTriggered() {
        for (int eId = 0; eId < this.numElevators; eId++) {
            Elevator elevator = this.system.getElevators()[eId];
            Shaft shaft = elevator.getShaft();
            Cabin cabin = elevator.getCabin();
            Door cabinDoor = cabin.getDoor();

            int fId = this.currFloorIdx[eId];

            /** Big remark - this controller doesn't subscribe for
             * onPressed event on the floor button.
             * Here we simply start with opening the door on the initial floor.
             * Next version should start with waiting for human to press button first.
             **/
            this.shaftDoors[fId][eId] = shaft.getDoors()[fId];
            if (this.firstIteration[eId]) {
                this.shaftDoors[fId][eId].startOpening();
                this.firstIteration[eId] = false;
            }

            this.shaftDoors[fId][eId].subscribe((elevatorIdx, floorIdx, position) -> {
                if (floorIdx == -1) {
                    return;
                }

                if (position == 1.0 && !cabinDoorOpened[elevatorIdx]) {
                    shaftDoorOpened[floorIdx][elevatorIdx] = true;
                    cabinDoor.startOpening();
                } else if (position == 0.0) {
                    shaftDoorOpened[floorIdx][elevatorIdx] = false;

                    if (nextFloorIdx[elevatorIdx] > floorIdx) {
                        shaft.controlShaftMotor(1.0);
                        cabin.setDirectionIndicator(1);
                    } else if (nextFloorIdx[elevatorIdx] < floorIdx) {
                        shaft.controlShaftMotor(-1.0);
                        cabin.setDirectionIndicator(-1);
                    } else {
                        shaft.stopShaftMotor();
                        cabin.setDirectionIndicator(0);
                    }
                }
            });

            cabinDoor.subscribe((elevatorIdx, floorIdx, position) -> {
                if (floorIdx != -1) {
                    return;
                }

                if (position == 1.0) {
                    cabinDoorOpened[elevatorIdx] = true;
                } else if (position == 0.0 && shaftDoorOpened[fId][elevatorIdx]) {
                    cabinDoorOpened[elevatorIdx] = false;
                    shaftDoors[fId][elevatorIdx].startClosing();
                }
            });

            cabin.subscribe(new CabinNotifications() {
                @Override
                public void onNumericIndicatorChange(int idx, int numericValue) {
                    if (numericValue != currFloorIdx[idx] && cabinDoorOpened[idx]) {
                        cabinDoor.startClosing();
                    }
                }

                @Override public void onDirectionIndicatorChange(int idx, int direction) { }
                @Override public void onButtonPressed(int elevatorIdx, int floorIdx) { }
                @Override public void onDoorSensorStatusChange(int elevatorIdx, boolean obstacleDetected) { }
            });

            if (this.timeline[eId] >= this.randomWait()) {
                cabin.setNumericIndicator(this.nextFloorIdx[eId]);

                IntStream.range(0, cabin.getFloorButtons().length)
                        .forEach(i -> cabin.getFloorButtons()[i].setLightState(false));

                cabin.getFloorButtons()[this.nextFloorIdx[eId]].setLightState(true);

                this.timeline[eId] = 0.0;
            }

            shaft.subscribe((elevatorIdx, position) -> {
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
                    nextFloorIdx[elevatorIdx] = this.randomFloorIdx();

                    timeline[elevatorIdx] = 0.0;
                }
            });

            this.timeline[eId] += TIME_DELTA;
        }

        this.system.getClock().setAlarm(TIME_DELTA);
    }

    // region Private Methods

    private void init(ElevatorSystem system) {
        this.numFloors = system.getFloors().length;
        this.numElevators = system.getElevators().length;

        this.timeline = new double[this.numElevators];
        this.firstIteration = new boolean[this.numElevators];
        this.cabinDoorOpened = new boolean[this.numElevators];

        this.prevFloorIdx = new int[this.numElevators];
        this.currFloorIdx = new int[this.numElevators];
        this.nextFloorIdx = new int[this.numElevators];

        for (int i = 0; i < this.numElevators; i++) {
            this.timeline[i] = 0.0;

            this.firstIteration[i] = true;
            this.cabinDoorOpened[i] = false;

            this.prevFloorIdx[i] = -1;
            this.currFloorIdx[i] = 0;
            this.nextFloorIdx[i] = this.randomFloorIdx();
        }

        this.shaftDoors = new Door[this.numFloors][this.numElevators];
        this.shaftDoorOpened = new boolean[this.numFloors][this.numElevators];
        for (int i = 0; i < this.numFloors; i++) {
            for (int j = 0; j < this.numElevators; j++) {
                this.shaftDoorOpened[i][j] = false;
            }
        }
    }

    private int randomFloorIdx() {
        return this.random.nextInt(this.numFloors);
    }

    private double randomWait() {
        return RANDOM_WAIT_RANGE_START +
                (RANDOM_WAIT_RANGE_END - RANDOM_WAIT_RANGE_START) * this.random.nextDouble();
    }

    // endregion
}
