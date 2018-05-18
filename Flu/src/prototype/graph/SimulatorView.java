package prototype.graph;

import javafx.scene.paint.Color;
import prototype.core.Sandbox;
import prototype.vivant.Vivant;

/**
 * A graphical view of the simulation grid. This interface defines all possible
 * different views.
 * 
 * @author Michael Kölling and David J. Barnes
 * @author Peter Sander
 * @version 2016.02.29
 */
public interface SimulatorView {
    /**
     * Define a color to be used for a given class of animal.
     * 
     * @param animalClass
     *            The animal's Class object.
     * @param color
     *            The color to be used for the given class.
     */
    void setColor(Class<? extends Vivant> animalClass, Color color);

    /**
     * Determine whether the simulation should continue to run.
     * 
     * @return true If there is more than one species alive.
     */
    boolean isViable(Sandbox field);

    /**
     * Show the current status of the field.
     * 
     * @param step
     *            Which iteration step it is.
     * @param field
     *            The field whose status is to be displayed.
     */
    void showStatus(int step, Sandbox field);

    /**
     * Prepare for a new run.
     */
    void reset();

    /**
     * Sets up GUI.
     */
    void start();
}