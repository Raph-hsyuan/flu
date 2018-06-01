package prototype.graph;

import java.util.HashMap;
import java.util.Map;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Dimension2D;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import prototype.core.Sandbox;
import prototype.core.Simulator;
import prototype.vivant.*;
import static prototype.affair.State.*;

/**
 * A graphical view of the simulation grid. The view displays a colored
 * rectangle for each location representing its contents. Colors for each type
 * of species can be defined using the setColor method.
 * 
 * @author Michael Kölling and David J. Barnes
 * @author Peter Sander
 * @version 2017.03.24
 */
@SuppressWarnings("serial")
public class GridView implements SimulatorView {
    private static GridView instance;
    private static final int LABEL_HEIGHT = 50;
    // Colors used for empty locations.
    private static final Color EMPTY_COLOR = Color.WHITE;
    private final String STEP_PREFIX = "Day: ";
    private final String POPULATION_PREFIX = "Population: ";
    private final String DETAIL_PREFIX = "Detail: ";
    private Label stepLabel;
    private Label populationLbl;
    private Label detailLable;
    private FieldView fieldView;

    // A map for storing colors for participants in the simulation
    private final Map<Class<? extends Vivant>, Color> colors
            = new HashMap<Class<? extends Vivant>, Color>() {{
                put(Human.class, Color.ORANGE);
                put(Pig.class, Color.PINK);
                put(Chicken.class, Color.YELLOW);
            }};
    // A statistics object computing and storing simulation information
    private FieldStats stats;
    private int width;
    private int height;
    private BorderPane root;
    private int step = 0;

    public GridView() {
        this(Sandbox.SIZE, Sandbox.SIZE);
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
        this.width = width+20;
        this.height = height+20;
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

        Stage stage = new Stage();
        stage.setTitle("Flu");
        root = new BorderPane();

        stepLabel = new Label(STEP_PREFIX);
        stepLabel.setAlignment(Pos.CENTER);
        stepLabel.setMinHeight(LABEL_HEIGHT);
        root.setTop(stepLabel);
        BorderPane.setAlignment(root.getTop(), Pos.BOTTOM_CENTER);

        populationLbl = new Label(POPULATION_PREFIX);
        populationLbl.setAlignment(Pos.CENTER);
        populationLbl.setMinHeight(LABEL_HEIGHT);
        root.setBottom(populationLbl);
        BorderPane.setAlignment(root.getTop(), Pos.BOTTOM_RIGHT);

        detailLable = new Label(DETAIL_PREFIX);
        detailLable.setAlignment(Pos.CENTER);
        detailLable.setMinHeight(LABEL_HEIGHT);
        root.setLeft(detailLable);
        BorderPane.setAlignment(root.getTop(), Pos.BOTTOM_CENTER);
        
        root.setCenter(fieldView);
        stage.setScene(new Scene(root,
                width * FieldView.GRID_VIEW_SCALING_FACTOR,
                height * FieldView.GRID_VIEW_SCALING_FACTOR + 2 * LABEL_HEIGHT));
        stage.show();

    }

    public void stop() {
        System.out.println("Well, that was fun.");
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
        if(vivant.getState().equals(DEAD))
            return Color.BLACK;
        if(vivant.getState().equals(RECOVERED))
            return Color.GREEN;
        if(!vivant.getState().equals(HEALTHY))
            return Color.RED;
        return colors.get(vivant.getClass());
    }

    /**
     * Show the current status of the field.
     * Incidentally draws protagonists in place.
     * 
     * @param step
     *            Which iteration step it is.
     * @param field
     *            The field whose status is to be displayed.
     */
    @Override
    public void showStatus(int step, Sandbox field) {
        stepLabel.setText(STEP_PREFIX + step);
        StringBuilder detail = new StringBuilder();
        detail.append("\nhealther: " + Simulator.getHealther()+"\n");
        detail.append("sicker: " + Simulator.getSicker()+"\n");
        detail.append("contagious: " + Simulator.getContagious()+"\n");
        detail.append("recovered: " + Simulator.getRecovered()+"\n");
        detail.append("dead: " + Simulator.getDead()+"\n");
        
        detailLable.setText(DETAIL_PREFIX + detail);
        stats.reset();
        fieldView.preparePaint();
        for (int row = 0; row < Sandbox.SIZE; row++) {
            for (int col = 0; col < Sandbox.SIZE; col++) {
                boolean isVide = field.getLocation(row, col).isVide();
                if (!isVide) {
                    Vivant animal = field.getLocation(row, col).getVivant();
                    stats.incrementCount(animal.getClass());
                    fieldView.drawMark(col,
                            row, getColor(animal));
                } else {
                    fieldView.drawMark(col, row, EMPTY_COLOR);
                }
            }
        }
        stats.countFinished();
        populationLbl.setText(stats.getPopulationDetails(field));
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
     * Provide a graphical view of a rectangular field. This is a nested class
     * (a class defined inside a class) which defines a custom component for the
     * user interface. This component displays the field. This is rather
     * advanced GUI stuff - you can ignore this for your project if you like.
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
            super(width * GRID_VIEW_SCALING_FACTOR,
                    height * GRID_VIEW_SCALING_FACTOR);
            gridHeight = height;
            gridWidth = width;
            size = new Dimension2D(width * GRID_VIEW_SCALING_FACTOR,
                    height * GRID_VIEW_SCALING_FACTOR);
            g = getGraphicsContext2D();
        }

        /**
         * Prepare for a new round of painting. Since the component may be
         * resized, compute the scaling factor again.
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
}
