package prototype.graph;

import java.util.HashMap;

import prototype.core.Sandbox;
import prototype.core.Simulator;
import prototype.vivant.Vivant;
import static prototype.affair.State.*;
import prototype.affair.State;
/**
 * This class collects and provides some statistical data on the state of a
 * field. It is flexible: it will create and maintain a counter for any class of
 * object that is found within the field.
 * 
 * @author David J. Barnes and Michael Kölling
 * @version 2016.02.29
 */
public class FieldStats {
    // Counters for each type of entity (fox, rabbit, etc.) in the simulation.
    private HashMap<Class<? extends Vivant>, Counter> counters;
    // Whether the counters are currently up to date.
    private boolean countsValid;

    /**
     * Construct a FieldStats object.
     */
    public FieldStats() {
        // Set up a collection for counters for each type of animal that
        // we might find
        counters = new HashMap<>();
        countsValid = false;
    }

    /**
     * Get details of what is in the field.
     * 
     * @return A string describing what is in the field.
     */
    public String getPopulationDetails(Sandbox field, Vivant vivant) {
        StringBuilder builder = new StringBuilder();
        if (!countsValid) {
            generateCounts(field);
        }

        Counter info = counters.get(vivant.getClass());
        builder.append(info.getName());
        builder.append(": ");
        builder.append(info.getCount());
        return builder.toString();
    }

    public String getStatDetails(State state) {
        switch (state) {
        case CONTAGIOUS:
            return "Contagious : " + Simulator.getNumber(CONTAGIOUS);
        case HEALTHY:
            return "Healthy : " + Simulator.getNumber(HEALTHY);
        case DEAD:
            return "Dead : " + Simulator.getNumber(DEAD);
        case RECOVERED:
            return "Recovered : " + Simulator.getNumber(RECOVERED);
        case SICK:
            return "Sick : " + Simulator.getNumber(SICK);
        default:
            return "";
        }
    }

    /**
     * Get the number of individuals in the population of a given class.
     * 
     * @return An int with the number for this class.
     */
    public int getPopulationCount(Sandbox field, Class<? extends Vivant> key) {
        if (!countsValid) {
            generateCounts(field);
        }

        Counter counter = counters.get(key);
        return counter.getCount();
    }

    /**
     * Invalidate the current set of statistics; reset all counts to zero.
     */
    public void reset() {
        countsValid = false;
        for (Class<? extends Vivant> key : counters.keySet()) {
            Counter count = counters.get(key);
            count.reset();
        }
    }

    /**
     * Increment the count for one class of animal.
     * 
     * @param animalClass
     *            The class of animal to increment.
     */
    public void incrementCount(Class<? extends Vivant> animalClass) {
        Counter count = counters.get(animalClass);
        if (count == null) {
            // We do not have a counter for this species yet.
            // Create one.
            count = new Counter(animalClass.getSimpleName());
            counters.put(animalClass, count);
        }
        count.increment();
    }

    /**
     * Indicate that an animal count has been completed.
     */
    public void countFinished() {
        countsValid = true;
    }

    /**
     * Determine whether the simulation is still viable. I.e., should it continue to
     * run.
     * 
     * @return true If there is more than one species alive.
     */
    public boolean isViable(Sandbox field) {
        // How many counts are non-zero.
        int nonZero = 0;
        if (!countsValid) {
            generateCounts(field);
        }
        for (Class<? extends Vivant> key : counters.keySet()) {
            Counter info = counters.get(key);
            if (info.getCount() > 0) {
                nonZero++;
            }
        }
        return nonZero > 1;
    }

    /**
     * Generate counts of the number of foxes and rabbits. These are not kept up to
     * date as foxes and rabbits are placed in the field, but only when a request is
     * made for the information.
     * 
     * @param field
     *            The field to generate the stats for.
     */
    private void generateCounts(Sandbox field) {
        reset();
        for (int row = 0; row < Sandbox.SIZE; row++) {
            for (int col = 0; col < Sandbox.SIZE; col++) {
                Vivant animal = field.getLocation(row, col).getVivant();
                if (animal != null) {
                    incrementCount(animal.getClass());
                }
            }
        }
        countsValid = true;
    }
}
