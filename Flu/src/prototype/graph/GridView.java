package prototype.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Dimension2D;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import prototype.core.Sandbox;
import prototype.core.Simulator;
import prototype.virus.Virus;
import prototype.vivant.*;
import static prototype.affair.State.*;

/**
 * A graphical view of the simulation grid. The view displays a colored
 * rectangle for each location representing its contents. Colors for each type
 * of species can be defined using the setColor method.
 * 
 * @author Michael Kölling and David J. Barnes
 * @author Peter Sander
 * @author HUANG Shenyuan
 * @version 2017.03.24
 */

public class GridView extends Application implements SimulatorView {
    private static GridView instance;
    private static final int LABEL_HEIGHT = 50;
    private static final Color EMPTY_COLOR = Color.WHITE;
    private static final String STEP_PREFIX = "Day: ";
    private Label stepLabel;
    private TextFlow population = new TextFlow();
    private FieldView fieldView;
    private Simulator simulator;
    private List<SimulatorView> views = new ArrayList<>();
    private static AnimationTimer timer;
    private static Slider speedSlider;
    private Slider infectRateSlider = new Slider(0, 1, 0.1);
    private Slider recoverRateSlider = new Slider(0, 1, 0.1);
    private Slider dieRateSlider = new Slider(0, 1, 0.1);
    private Slider sickRateSlider = new Slider(0, 1, 0.1);
    private Slider dieAnimalSlider = new Slider(0, 1, 0.1);
    private Thread threadOneStep = new Thread();
    private boolean isFirstTime = true;
    // A map for storing colors for participants in the simulation
    private final Map<Class<? extends Vivant>, Color> colors = new HashMap<Class<? extends Vivant>, Color>();
    // A statistics object computing and storing simulation information
    private FieldStats stats;
    private int width;
    private int height;

    public GridView() {
        this(Sandbox.SIZE, Sandbox.SIZE);
        colors.put(Human.class, Color.ORANGE);
        colors.put(Pig.class, Color.PINK);
        colors.put(Chicken.class, Color.YELLOW);
    }

    /**
     * Create a view of the given width and height.
     * 
     * @param height
     *            The simulation's height.
     * @param width
     *            The simulation's width.
     */
    public GridView(int height, int width) {
        this.width = width + 60;
        this.height = height + 20;
        stats = new FieldStats();
        fieldView = new FieldView(height, width);
        instance = this;
    }

    /**
     * @return reference to the current object.
     */
    public static GridView getInstance() {
        return instance;
    }

    /**
     * FX application method to run the GUI.
     */
    @Override
    public void start() {
        BorderPane root;
        Stage stage = new Stage();
        stage.setTitle("Flu");
        root = new BorderPane();
        stepLabel = new Label(STEP_PREFIX);
        stepLabel.setAlignment(Pos.CENTER);
        stepLabel.setMinHeight(LABEL_HEIGHT);
        root.setTop(stepLabel);
        BorderPane.setAlignment(root.getTop(), Pos.BOTTOM_CENTER);

        root.setBottom(population);

        root.setCenter(fieldView);
        stage.setScene(new Scene(root, width * FieldView.GRID_VIEW_SCALING_FACTOR,
                height * FieldView.GRID_VIEW_SCALING_FACTOR + 2 * LABEL_HEIGHT));
        root.setRight(createContent());

        stage.show();
    }

    /**
     * Define a color to be used for a given class of animal.
     * 
     * @param animalClass
     *            The animal's Class<Animal> object.
     * @param color
     *            The color to be used for the given class.
     */
    @Override
    public void setColor(Class<? extends Vivant> animalClass, Color color) {
        colors.put(animalClass, color);
    }

    /**
     * @return The color to be used for a given class of animal.
     */
    private Color getColor(Vivant vivant) {
        if (vivant.getState().equals(DEAD))
            return Color.BLACK;
        if (vivant.getState().equals(RECOVERED))
            return Color.GREEN;
        if (!vivant.getState().equals(HEALTHY) && !vivant.getState().equals(INFECTED))
            return Color.RED;
        return colors.get(vivant.getClass());
    }

    /**
     * Show the current status of the field. Incidentally draws protagonists in
     * place.
     * 
     * @param step
     *            Which iteration step it is.
     * @param field
     *            The field whose status is to be displayed.
     */
    @Override
    public void showStatus(int step, Sandbox field) {
        stepLabel.setText(STEP_PREFIX + step);
        stepLabel.setFont(Font.font(30));
        stats.reset();
        fieldView.preparePaint();
        for (int row = 0; row < Sandbox.SIZE; row++) {
            for (int col = 0; col < Sandbox.SIZE; col++) {
                boolean isVide = field.getLocation(row, col).isVide();
                if (!isVide) {
                    Vivant animal = field.getLocation(row, col).getVivant();
                    stats.incrementCount(animal.getClass());
                    fieldView.drawMark(col, row, getColor(animal));
                } else {
                    fieldView.drawMark(col, row, EMPTY_COLOR);
                }
            }
        }

        buildStatistic(field);
        buildStateStatistic(field);
    }

    void buildStateStatistic(Sandbox field) {
        Text cC = new Text("\n█");
        cC.setFill(Color.RED);
        Text c = new Text("Contagious: " + Simulator.getNumber(CONTAGIOUS));

        Text hC = new Text("\n█");
        hC.setFill(Color.ORANGE);
        Text h = new Text("Healthy: " + Simulator.getNumber(HEALTHY));

        Text dC = new Text("\n█");
        dC.setFill(Color.BLACK);
        Text d = new Text("Dead: " + Simulator.getNumber(DEAD));

        Text rC = new Text("\n█");
        rC.setFill(Color.GREEN);
        Text r = new Text("Recovered: " + Simulator.getNumber(RECOVERED));

        Text sC = new Text("\n█");
        sC.setFill(Color.RED);
        Text s = new Text("Sick: " + Simulator.getNumber(SICK));
        population.getChildren().addAll(cC, c, hC, h, dC, d, sC, s, rC, r);

    }

    void buildStatistic(Sandbox field) {
        population.getChildren().clear();
        stats.countFinished();

        Text humanC = new Text("\n█");
        humanC.setFill(Color.ORANGE);
        Text human = new Text(stats.getPopulationDetails(field, new Human()));

        Text pigC = new Text("\n█");
        pigC.setFill(Color.PINK);
        Text pig = new Text(stats.getPopulationDetails(field, new Pig()));

        Text chickenC = new Text("\n█");
        chickenC.setFill(Color.YELLOW);
        Text chicken = new Text(stats.getPopulationDetails(field, new Chicken()));

        population.getChildren().addAll(humanC, human, pigC, pig, chickenC, chicken);
    }

    /**
     * Determine whether the simulation should continue to run.
     * 
     * @return true If there is more than one species alive.
     */
    public boolean isViable(Sandbox field) {
        return stats.isViable(field);
    }

    /**
     * Prepare for a new run.
     */
    @Override
    public void reset() {
        // always a pleasure
    }

    /**
     * Provide a graphical view of a rectangular field. This is a nested class (a
     * class defined inside a class) which defines a custom component for the user
     * interface. This component displays the field. This is rather advanced GUI
     * stuff - you can ignore this for your project if you like.
     */
    private class FieldView extends Canvas {
        private static final int GRID_VIEW_SCALING_FACTOR = 6;

        private int gridWidth, gridHeight;
        private int xScale, yScale;
        private Dimension2D size;
        private GraphicsContext g;

        /**
         * Create a new FieldView component.
         */
        public FieldView(int height, int width) {
            super(width * GRID_VIEW_SCALING_FACTOR, height * GRID_VIEW_SCALING_FACTOR);
            gridHeight = height;
            gridWidth = width;
            size = new Dimension2D(width * GRID_VIEW_SCALING_FACTOR, height * GRID_VIEW_SCALING_FACTOR);
            g = getGraphicsContext2D();
        }

        /**
         * Prepare for a new round of painting. Since the component may be resized,
         * compute the scaling factor again.
         */
        public void preparePaint() {
            g.clearRect(0, 0, size.getWidth(), size.getHeight());
            xScale = (int) size.getWidth() / gridWidth;
            yScale = (int) size.getHeight() / gridHeight;
        }

        /**
         * Paint on grid location on this field in a given color.
         */
        public void drawMark(int x, int y, Color color) {
            g.setFill(color);
            g.fillRect(x * xScale, y * yScale, xScale - 1, yScale - 1);
        }
    }

    /**
     * Instantiates simulator view objects from simulator view class names. Bit of a
     * hack to get around the checked exceptions.
     */
    @Override
    public void init() {
        getParameters().getUnnamed().forEach(name -> {
            try {
                views.add((SimulatorView) Class.forName(name).newInstance());
            } catch (Exception e) {
                System.out.println("WHOOPSIE! Careful with that cast, Eugene");
            }
        });
    }

    /**
     * Sets up and runs animation timer. Calls one simulation step at each time
     * event.
     */
    @Override
    public void start(Stage primaryStage) {
        setVirus();
        views.forEach(v -> v.start());
        simulator = new Simulator(views.toArray(new SimulatorView[0]));
        if (timer == null) {
            timer = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    // trying to slow down the display
                    if (isFirstTime || (!threadOneStep.isAlive())) {
                        threadOneStep = new Thread() {
                            @Override
                            public void run() {
                                simulator.nothingCount();
                                try {
                                    Thread.sleep(getSpeed());
                                } catch (InterruptedException e) {
                                }
                                simulator.simulateOneStep();
                                Platform.runLater(() -> {
                                    simulator.updateViews();
                                });
                            }
                        };
                        isFirstTime = false;
                        threadOneStep.start();
                    }
                    if (!simulator.isViable()) {
                        stop();
                        System.out.println("Animation stopped");
                    }

                }

            };
            setTimer(timer);
        }
    }

    private void setVirus() {
        Virus virus = new Virus("UserVirus",infectRateSlider.getValue(),
                recoverRateSlider.getValue()*0.3,
                dieRateSlider.getValue()*0.05,
                sickRateSlider.getValue(),
                dieAnimalSlider.getValue());
        Simulator.setChickenVirus(virus);
        Simulator.setPigVirus(virus);
    }
    
    private Parent createContent() {
        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        buildRateSliders();
        Button pauseBtn = new Button("Pause");
        pauseBtn.setOnAction(evt -> timer.stop());
        Button startBtn = new Button("Start");
        startBtn.setOnAction(evt -> {timer.start();setVirus();});
        HBox speedBox = new HBox();
        speedBox.setAlignment(Pos.CENTER);
        HBox infectRateBox = new HBox();
        infectRateBox.setAlignment(Pos.CENTER);
        infectRateBox.getChildren().addAll(new Label("infectRate"), infectRateSlider);
        HBox recoverRateBox = new HBox();
        recoverRateBox.setAlignment(Pos.CENTER);
        recoverRateBox.getChildren().addAll(new Label("recoverRate"), recoverRateSlider);
        HBox dieRateBox = new HBox();
        dieRateBox.setAlignment(Pos.CENTER);
        dieRateBox.getChildren().addAll(new Label("dieRate"), dieRateSlider);
        HBox sickRateBox = new HBox();
        sickRateBox.setAlignment(Pos.CENTER);
        sickRateBox.getChildren().addAll(new Label("sickRate"), sickRateSlider);
        HBox dieAnimalBox = new HBox();
        dieAnimalBox.setAlignment(Pos.CENTER);
        dieAnimalBox.getChildren().addAll(new Label("dieAnimalRate"), dieAnimalSlider);
        speedSlider = new Slider(0, 1, 0.5); // in secs
        speedSlider.setShowTickMarks(true);
        speedSlider.setShowTickLabels(true);
        speedSlider.setMajorTickUnit(0.25f);
        speedSlider.setBlockIncrement(0.1f);
        speedBox.getChildren().addAll(new Label("Slow"), speedSlider, new Label("Fast"));
        Button quitBtn = new Button("Quit");
        quitBtn.setOnAction(evt -> Platform.exit());
        root.getChildren().addAll(infectRateBox,recoverRateBox,dieRateBox,sickRateBox,dieAnimalBox,
                startBtn, pauseBtn, quitBtn, speedBox);
        return root;
    }

    void buildRateSliders() {
        infectRateSlider = new Slider(0, 1, 0.1);
        recoverRateSlider = new Slider(0, 1, 0.1);
        dieRateSlider = new Slider(0, 1, 0.1);
        sickRateSlider = new Slider(0, 1, 0.1);
        dieAnimalSlider = new Slider(0, 1, 0.1);
        
        List<Slider> rateSliders = new ArrayList<>();
        rateSliders.add(infectRateSlider);
        rateSliders.add(recoverRateSlider);
        rateSliders.add(dieRateSlider);
        rateSliders.add(sickRateSlider);
        rateSliders.add(dieAnimalSlider);
        for(Slider s : rateSliders) {
            s.setShowTickMarks(true);
            s.setShowTickLabels(true);
            s.setMajorTickUnit(0.1f);
            s.setBlockIncrement(0.1f);
        }
    }
    void setTimer(AnimationTimer timers) {
        timer = timers;
    }

    /**
     * Converts speed reading from secs to msecs.
     */
    long getSpeed() {
        return 1000 - (long) (1000 * speedSlider.getValue());
    }

    public static void launch() {
        Application.launch("prototype.graph.GridView");
    }
}
