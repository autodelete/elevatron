package elevatron.ui;

import elevatron.interfaces.*;
import elevatron.simulator.ButtonSimulator;
import elevatron.simulator.ClockSimulator;
import elevatron.simulator.ElevatorSystemSimulator;
import elevatron.simulator.HumanSimulator;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Random;
import java.util.stream.IntStream;

public class Main extends Application {
    private static final boolean USE_ORIGINAL_RANDOM_CONTROLLER = false;

    private static final double HUMAN_WIDTH = 20;
    private static final double HUMAN_HEIGHT = 60;
    public static final Color HUMAN_STROKE = Color.CYAN;
    public static final Color HUMAN_FILL = Color.TRANSPARENT;
    private static final double HUMAN_STROKE_WIDTH = 2;
    private static int NUM_FLOORS=7;
    private static int NUM_ELEVATORS=2;
    private static int NUM_HUMANS=15;


    public static final int FPS = 20;

    private static final double FLOOR_STROKE_WIDTH = 3;
    private static final Paint FLOOR_FILL = Color.TRANSPARENT;
    private static final Paint FLOOR_STROKE = Color.WHITE;
    private static int WINDOW_WIDTH = 1200;
    private static int WINDOW_HEIGHT = 800;
    private static int FLOOR_WIDTH=1100;
    private static final int FLOOR_START_X = (WINDOW_WIDTH - FLOOR_WIDTH) / 2;
    private static final int FLOOR_START_Y = 10;
    private static int FLOOR_HEIGHT=90;

    private static int ELEVATOR_AREA_WIDTH=300;
    private static int SHAFT_WIDTH=150;
    private static final double SHAFT_STROKE_WIDTH = 3;
    private static final Paint SHAFT_FILL = Color.rgb(64,64,64);
    private static final Paint SHAFT_STROKE = Color.TRANSPARENT;

    private static int CABIN_WIDTH = SHAFT_WIDTH*7/ 10;
    private static final int CABIN_HEIGHT = FLOOR_HEIGHT*8/ 10;
    private static final double CABIN_STROKE_WIDTH = 3;
    private static final Paint CABIN_FILL = Color.TRANSPARENT;
    private static final Paint CABIN_STROKE = Color.YELLOW;
    private static int DOOR_WIDTH = CABIN_WIDTH*2/5;
    private static final Paint BUTTON_OFF_STROKE = Color.DARKRED;
    private static final Paint BUTTON_OFF_FILL = Color.TRANSPARENT;
    private static final Paint BUTTON_ON_STROKE = Color.LIMEGREEN;
    private static final Paint BUTTON_ON_FILL = Color.LIMEGREEN;

    private static final Paint INDICATOR_STROKE = Color.YELLOW;

    Timeline timeline = new Timeline();
    private long startTimeMillis = System.currentTimeMillis();

    @Override
    public void start(Stage primaryStage) throws Exception{
        ElevatorSystemSimulator simulator
                = new ElevatorSystemSimulator(NUM_FLOORS, NUM_ELEVATORS);
        Random random = new Random();
        HumanSimulator[] humans = IntStream.range(0, NUM_HUMANS)
                .mapToObj(i -> new HumanSimulator((ClockSimulator) simulator.getClock(),
                        simulator, random, 0.0, FLOOR_WIDTH,
                            IntStream.range(0, NUM_FLOORS).mapToDouble(f -> floorY(f)).toArray(),
                            callButtonsX(),
                            IntStream.range(0, NUM_FLOORS).mapToDouble(e -> elevatorX(e)).toArray()
                        ))
                .toArray(HumanSimulator[]::new);

        Pane root = FXMLLoader.load(getClass().getResource("elevatron.fxml"));
        primaryStage.setTitle("Elevatron v0.1.0");
        primaryStage.setScene(new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT));
        primaryStage.show();
        scheduleTimer(simulator);
        drawEverything(root, simulator);
        drawHumans(root, humans);
        primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> {
            onResize(root, newVal.doubleValue(), primaryStage.getHeight());
        });
        primaryStage.heightProperty().addListener((obs, oldVal, newVal) -> {
            onResize(root, primaryStage.getWidth(), newVal.doubleValue());
        });

        if (USE_ORIGINAL_RANDOM_CONTROLLER) {
            new elevatron.controller.OriginalRandomController(simulator);
        } else {
            new elevatron.controller.Controller(simulator);
        }
    }

    private void onResize(Pane root, double width, double height) {
        Scale scale = new Scale(width/WINDOW_WIDTH, height/WINDOW_HEIGHT);
        scale.setPivotX(0);
        scale.setPivotY(0);
        root.getTransforms().setAll(scale);
    }

    private void drawHumans(Pane root, HumanSimulator[] humans) {
        for (HumanSimulator human : humans) {
            Pane humanPane = new Pane();
            Circle head
                    = new Circle(HUMAN_WIDTH/2.0,
                    HUMAN_WIDTH/2.0,
                    HUMAN_WIDTH/2.0);
            head.setStroke(HUMAN_STROKE);
            head.setFill(HUMAN_FILL);
            head.setStrokeWidth(HUMAN_STROKE_WIDTH);
            Polygon body = new Polygon();
            body.getPoints().addAll(
                    0.0, HUMAN_HEIGHT,
                    HUMAN_WIDTH, HUMAN_HEIGHT,
                    HUMAN_WIDTH/2.0, HUMAN_WIDTH+1);
            body.setStroke(HUMAN_STROKE);
            body.setFill(HUMAN_FILL);
            body.setStrokeWidth(HUMAN_STROKE_WIDTH);
            humanPane.getChildren().addAll(head,body);
            root.getChildren().addAll(humanPane);
            human.subscribe((x, y) -> {
                humanPane.setLayoutX(FLOOR_START_X + x - HUMAN_WIDTH / 2);
                humanPane.setLayoutY(y - HUMAN_HEIGHT - 4);
            });
        }
    }

    private void drawEverything(Pane root, ElevatorSystemSimulator simulator) {
        for (int elevIdx=0; elevIdx<NUM_ELEVATORS; elevIdx++) {
            drawElevator(root, simulator.getElevators()[elevIdx], FLOOR_START_X + elevIdx*ELEVATOR_AREA_WIDTH,
                    FLOOR_START_Y-5, ELEVATOR_AREA_WIDTH, 10 +FLOOR_HEIGHT*NUM_FLOORS);
        }
        for (int i=0; i<NUM_FLOORS; i++) {
            int florIdx = NUM_FLOORS-i-1;
            drawFloor(root,simulator,simulator.getFloors()[florIdx], FLOOR_START_X,
                    FLOOR_START_Y+i*FLOOR_HEIGHT,FLOOR_WIDTH, FLOOR_HEIGHT-5);
        }
    }

    private int floorY(int idx) {
        return FLOOR_START_Y+(NUM_FLOORS-idx)*FLOOR_HEIGHT;
    }

    private double elevatorX(int idx) {
        return (SHAFT_WIDTH - CABIN_WIDTH) / 2 +  ELEVATOR_AREA_WIDTH*idx + CABIN_WIDTH/2 + 3;
    }

    private void drawFloor(Pane root, ElevatorSystemSimulator simulator, Floor floor, int x, int y, int w, int h) {
        Rectangle rect = new Rectangle(x, y, w, h);
        rect.setFill(FLOOR_FILL);
        rect.setStroke(FLOOR_STROKE);
        rect.setStrokeWidth(FLOOR_STROKE_WIDTH);
        for (int i = 0; i< NUM_ELEVATORS; i++) {
            Cabin cabin = simulator.getElevators()[i].getCabin();
            Door door = simulator.getElevators()[i].getShaft().getDoors()[floor.getIdx()];
            int midX = x + (int)elevatorX(i);
            int doorY = y + FLOOR_HEIGHT - CABIN_HEIGHT - 4;
            drawDoor(root, door, midX - DOOR_WIDTH, doorY,
                    DOOR_WIDTH, CABIN_HEIGHT, -1, FLOOR_STROKE, FLOOR_FILL);
            drawDoor(root, door, midX, doorY,
                    DOOR_WIDTH, CABIN_HEIGHT, 1, FLOOR_STROKE, FLOOR_FILL);
            drawIndicator(root, cabin,
                    x + SHAFT_WIDTH + ELEVATOR_AREA_WIDTH*i -20,
                    doorY,30,15);
        }

        int yMid = y + FLOOR_HEIGHT-CABIN_HEIGHT/2;
        Polygon upButton = triangle(x + callButtonsX(), yMid-5,   5, -10);
        Polygon downButton = triangle(x + callButtonsX(),yMid+5, 5, 10);
        bindButtonEvents(floor.getUpCallButton(), upButton);
        bindButtonEvents(floor.getDownCallButton(), downButton);
        root.getChildren().addAll(rect, upButton, downButton);
    }

    private int callButtonsX() {
        return SHAFT_WIDTH + (ELEVATOR_AREA_WIDTH - SHAFT_WIDTH) / 2;
    }

    private void drawIndicator(Pane root, Cabin cabin, int x, int y, int w, int h) {
        Polyline up = arrow(x+w/2,y,w/2,h);
        Polyline down = arrow(x+w,y+h,-w/2,-h);
        Text number = new Text();
        number.setX(x);
        number.setY(y+14);
        number.setFont(Font.font(h*1.3));
        number.setStroke(INDICATOR_STROKE);
        number.setFill(INDICATOR_STROKE);
        root.getChildren().addAll(up,down,number);
        cabin.subscribe(new CabinNotifications() {
            @Override
            public void onButtonPressed(int elevatorIdx, int floorIdx) {

            }

            @Override
            public void onDoorSensorStatusChange(int elevatorIdx, boolean obstacleDetected) {

            }

            @Override
            public void onNumericIndicatorChange(int idx, int numericValue) {
                number.setText(numericValue >= 0 ? Integer.toString(numericValue) : "");
            }

            @Override
            public void onDirectionIndicatorChange(int idx, int direction) {
                up.setVisible(direction == 1);
                down.setVisible(direction == -1);
            }
        });
    }

    private Polyline arrow(double x, double y, double w, double h) {
        Polyline polyline = new Polyline();
        polyline.getPoints().addAll(x+w/2,y+h, x+w/2,y, x, y+w/2, x+w/2, y, x+w,y+w/2);
        polyline.setStroke(INDICATOR_STROKE);
        return polyline;
    }

    private void bindButtonEvents(Button button, Polygon polygon) {
        button.subscribe(new ButtonNotifications() {
            @Override
            public void onLightStateChange(boolean isOn) {
                polygon.setFill(isOn ? BUTTON_ON_FILL : BUTTON_OFF_FILL);
                polygon.setStroke(isOn ? BUTTON_ON_STROKE : BUTTON_OFF_STROKE);
            }

            @Override
            public void onPressed() {
            }
        });
        polygon.setOnMouseClicked(mouseEvent -> ((ButtonSimulator)button).press());
    }

    private Polygon triangle(double x, double y, double dx, double dy) {
        Polygon polygon = new Polygon();
        polygon.getPoints().addAll(x-dx,y, x,y+dy, x+dx, y);
        polygon.setStroke(BUTTON_OFF_STROKE);
        polygon.setFill(BUTTON_OFF_FILL);
        return polygon;
    }

    private void drawElevator(Pane root, Elevator elevator, int x, int y, int w, int h) {
        drawShaft(root, elevator.getShaft(), x, y, SHAFT_WIDTH, h);
        drawCabin(root, elevator.getShaft(), elevator.getCabin(),
                x + (SHAFT_WIDTH - CABIN_WIDTH) / 2,
                y + FLOOR_HEIGHT*NUM_FLOORS-CABIN_HEIGHT,
                CABIN_WIDTH, CABIN_HEIGHT);


        // Button panel
        int cols = 2;
        for (int i=0; i<NUM_FLOORS; i++) {
            Button floorButton = elevator.getCabin().getFloorButtons()[i];
            int bx = x + CABIN_WIDTH - 16 + (i % cols) * 25;
            int by = y + h + 20 + i / cols * 25;
            Rectangle floorButtonRect = new Rectangle(bx, by, 23, 23);
            floorButtonRect.setStrokeWidth(CABIN_STROKE_WIDTH);
            Text number = new Text();
            number.setFill(INDICATOR_STROKE);
            number.setText(Integer.toString(i));
            number.setFont(Font.font(20));
            number.setX(bx);
            number.setY(by+20);
            root.getChildren().addAll(floorButtonRect, number);
            floorButtonRect.setOnMouseClicked(mouseEvent -> ((ButtonSimulator)floorButton).press());
            number.setOnMouseClicked(mouseEvent -> ((ButtonSimulator)floorButton).press());

            floorButton.subscribe(new ButtonNotifications() {
                @Override
                public void onLightStateChange(boolean isOn) {
                    floorButtonRect.setStroke(isOn ? BUTTON_ON_STROKE : BUTTON_OFF_STROKE);
                    floorButtonRect.setFill(isOn ? BUTTON_ON_FILL : BUTTON_OFF_FILL);
                }

                @Override
                public void onPressed() {
                }
            });
        }
    }

    private void drawShaft(Pane root, Shaft shaft, int x, int y, int w, int h) {
        Rectangle rect = new Rectangle(x, y, w, h);
        rect.setFill(SHAFT_FILL);
        rect.setStroke(SHAFT_STROKE);
        rect.setStrokeWidth(SHAFT_STROKE_WIDTH);
        root.getChildren().addAll(rect);
    }

    private void drawCabin(Pane root, Shaft shaft, Cabin cabin, int x, int y, int w, int h) {
        Pane cabinPane = new Pane();
        cabinPane.setLayoutX(x);
        cabinPane.setLayoutY(y);
        shaft.subscribe(new ShaftNotifications() {
            @Override
            public void onCabinVerticalPositionChange(int elevatorIdx, double position) {
                cabinPane.setLayoutY(y - position*FLOOR_HEIGHT);
            }
        });
        root.getChildren().addAll(cabinPane);
        Rectangle cabinRect = new Rectangle(0, 0, w, h);
        cabinRect.setFill(CABIN_FILL);
        cabinRect.setStroke(CABIN_STROKE);
        cabinRect.setStrokeWidth(CABIN_STROKE_WIDTH);
        cabinPane.getChildren().addAll(cabinRect);
        for (int i=0; i<NUM_FLOORS; i++) {
            Rectangle floorButtonRect
                    = new Rectangle(CABIN_WIDTH - 16 + (i%2)*8,
                        CABIN_HEIGHT - 8 - NUM_FLOORS/2*8 + i/2*8, 4, 4);
            floorButtonRect.setStrokeWidth(CABIN_STROKE_WIDTH);
            cabinPane.getChildren().addAll(floorButtonRect);
            cabin.getFloorButtons()[i].subscribe(new ButtonNotifications() {
                @Override
                public void onLightStateChange(boolean isOn) {
                    floorButtonRect.setStroke(isOn ? BUTTON_ON_STROKE : BUTTON_OFF_STROKE);
                    floorButtonRect.setFill(isOn ? BUTTON_ON_FILL : BUTTON_OFF_FILL);
                }

                @Override
                public void onPressed() {

                }
            });
        }

        int midX = CABIN_WIDTH/2;
        drawDoor(cabinPane, cabin.getDoor(), midX - DOOR_WIDTH, 0,
                DOOR_WIDTH, h, -1, CABIN_STROKE, CABIN_FILL);
        drawDoor(cabinPane, cabin.getDoor(), midX, 0,
                DOOR_WIDTH, h, 1, CABIN_STROKE, CABIN_FILL);
    }

    private void drawDoor(Pane root, Door door, int x, int y, int w, int h, int openDir,
                          Paint stroke, Paint fill) {
        Rectangle rect = new Rectangle(x, y, w, h);
        rect.setFill(fill);
        rect.setStroke(stroke);
        rect.setStrokeWidth(CABIN_STROKE_WIDTH);
        root.getChildren().addAll(rect);
        door.subscribe(new DoorNotifications() {
            @Override
            public void onDoorPositionChange(int elevatorIdx, int floorIdx, double position) {
                rect.setX(x + position * w * openDir);
            }
        });
    }

    private void scheduleTimer(ElevatorSystemSimulator simulator) {
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis((long)(1000.0/FPS)), actionEvent -> onTimer(simulator)));
        timeline.playFromStart();
    }

    void onTimer(ElevatorSystemSimulator simulator) {
        simulator.advanceTime((System.currentTimeMillis()-startTimeMillis)/1000.0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
