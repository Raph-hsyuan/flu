package prototype.core;

import static prototype.affair.Event.*;
import static prototype.affair.State.*;
import static prototype.core.Sandbox.SIZE;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import prototype.affair.Event;
import prototype.affair.State;
import prototype.vivant.Chicken;
import prototype.vivant.Human;
import prototype.vivant.Pig;
import prototype.vivant.Vivant;

/**
 * @author HUANG Shenyuan
 * @date 2018-03-01 23:15
 * @email shenyuan.huang@etu.unice.fr
 */
public class Simulator {
    private static final double INFECTED_CHANCE = 0.1;
    private static final double RECOVER_CHANCE = 1;
    private static final double DIE_CHANCE = 0.1;
    private static final double SICK_CHANCE = 0.5;
    private static final double SICK_ANIMAL_CHANCE = 0.5;
    private static final double ACCIDENT_CHANCE = 0;
    private static final double DIE_ANIMAL_CHANCE = 0.2;
    private static final int NOMBER_HUMAN = 40;
    private static final int NOMBER_CHICKEN = 15;
    private static final int NOMBER_PIG = 10;
    private Sandbox sandbox = new Sandbox();
    private final Map<State, Map<Event, Supplier<State>>> dict = new HashMap<>();

    Simulator() {
        buildDict();
        initial();
    }

    void run(int days) {
        int dead = 0;
        for (int i = 0; i < days; i++) {
            move();
            int sick = 0;
            int health = 0;
            int recovered = 0;
            int contagious = 0;
            for (int x = 0; x < SIZE; x++)
                for (int y = 0; y < SIZE; y++) {
                    Location location = sandbox.getLocation(x, y);
                    Vivant vivant = location.getVivant();
                    if (!location.isVide()) {
                        if (location.getVivant().toString().equals("H")) {
                            if (vivant.getState().equals(DEAD)) {
                                location.removeVivant();
                                continue;
                            }
                            Event event = dectEvent(location);
                            State state1 = vivant.getState();
                            State state2 = dict.get(state1).get(event).get();
                            vivant.setState(state2);
                            switch (vivant.getState()) {
                            case HEALTHY:
                                health++;
                                break;
                            case INFECTED:
                                health++;
                                break;
                            case CONTAGIOUS_AND_SICK:
                                sick++;
                                contagious++;
                                break;
                            case CONTAGIOUS_NOT_SICK:
                                contagious++;
                                break;
                            case CONTAGIOUS:
                                contagious++;
                                break;
                            case RECOVERED:
                                recovered++;
                                break;
                            case DEAD:
                                dead++;
                                break;
                            default:
                                break;
                            }
                        } else {
                            if(vivant.getState().equals(CONTAGIOUS)&&Math.random() < DIE_ANIMAL_CHANCE)
                                vivant.setState(DEAD);
                            
                        }
                    }
                }
            System.out.println("\n*******************");
            System.out.println("\nDAY: " + i);
            System.out.println(this);
            System.out.println("REPORT:\nsick : " + sick);
            System.out.println("healthy : " + health);
            System.out.println("contagious : " + contagious);
            System.out.println("recovered : " + recovered);
            System.out.println("dead : " + dead);
        }
    }

    Event dectEvent(Location loc) {
        Event event = NOTHING;
        Vivant centre = loc.getVivant();
        if (centre.getState().equals(CONTAGIOUS))
            return CONTAGIOUS_TIME;
        if (centre.getState().equals(CONTAGIOUS_NOT_SICK))
            return CONTAGIOUS_TIME;
        if (centre.getState().equals(CONTAGIOUS_AND_SICK))
            return CONTAGIOUS_TIME;
        if (centre.getState().equals(RECOVERING))
            return RECOVERING_TIME;
        if (centre.getState().equals(RECOVERED))
            return NOTHING;
        if (centre.getState().equals(INFECTED))
            return INCUBATION_TIME;
        if (centre.getState().equals(DEAD))
            return NOTHING;
        if (centre.getState().equals(HEALTHY)) {
            for (Location location : sandbox.getNeighbor(loc)) {
                if (!location.isVide()) {
                    Vivant vivant = location.getVivant();
                    switch (vivant.getState()) {
                    case CONTAGIOUS:
                        return CONTACT;
                    case CONTAGIOUS_AND_SICK:
                        return CONTACT;
                    case CONTAGIOUS_NOT_SICK:
                        return CONTACT;
                    default:
                        break;
                    }
                }

            }
        }
        return event;
    }

    void buildDict() {
        put(HEALTHY, CONTACT, () -> Math.random() > INFECTED_CHANCE ? HEALTHY : INFECTED);
        put(INFECTED, INCUBATION_TIME, () -> Math.random() < SICK_CHANCE ? CONTAGIOUS : INFECTED);
        put(CONTAGIOUS, CONTAGIOUS_TIME, () -> Math.random() < SICK_CHANCE ? CONTAGIOUS_AND_SICK : CONTAGIOUS_NOT_SICK);
        put(CONTAGIOUS_NOT_SICK, CONTAGIOUS_TIME, () -> Math.random() < SICK_CHANCE ? CONTAGIOUS_AND_SICK : RECOVERING);
        put(RECOVERING, RECOVERING_TIME, () -> Math.random() < RECOVER_CHANCE ? RECOVERED : RECOVERING);
        put(CONTAGIOUS_AND_SICK, CONTAGIOUS_TIME, () -> Math.random() < DIE_CHANCE ? DEAD : RECOVERING);
        put(HEALTHY, NOTHING, () -> Math.random() > ACCIDENT_CHANCE ? HEALTHY : DEAD);
        put(INFECTED, NOTHING, () -> Math.random() > ACCIDENT_CHANCE ? HEALTHY : DEAD);
        put(CONTAGIOUS, NOTHING, () -> Math.random() > ACCIDENT_CHANCE ? HEALTHY : DEAD);
        put(CONTAGIOUS_NOT_SICK, NOTHING, () -> Math.random() > ACCIDENT_CHANCE ? CONTAGIOUS_NOT_SICK : DEAD);
        put(CONTAGIOUS_AND_SICK, NOTHING, () -> Math.random() > ACCIDENT_CHANCE ? CONTAGIOUS_AND_SICK : DEAD);
        put(RECOVERED, NOTHING, () -> Math.random() > ACCIDENT_CHANCE ? RECOVERED : DEAD);
    }

    void initial() {
        for (int i = 0; i < NOMBER_HUMAN; i++) {
            sandbox.addVivant(new Human());
        }
        for (int i = 0; i < NOMBER_CHICKEN; i++) {
            Vivant aChicken = new Chicken();
            if (Math.random() < SICK_ANIMAL_CHANCE)
                aChicken.setState(CONTAGIOUS);
            sandbox.addVivant(aChicken);
        }
        for (int i = 0; i < NOMBER_PIG; i++) {
            Vivant aPig = new Pig();
            if (Math.random() < SICK_ANIMAL_CHANCE)
                aPig.setState(CONTAGIOUS);
            sandbox.addVivant(aPig);
        }
    }

    void put(State state, Event event, Supplier<State> todo) {
        if (dict.get(state) == null)
            dict.put(state, new HashMap<Event, Supplier<State>>());
        dict.get(state).put(event, todo);
    }

    void move() {
        sandbox.move();
    }

    @Override
    public String toString() {
        return sandbox.toString();
    }

}
