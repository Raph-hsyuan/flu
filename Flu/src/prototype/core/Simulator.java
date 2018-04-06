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
import prototype.vivant.virus.Virus;

/**
 * @author HUANG Shenyuan
 * @date 2018-03-01 23:15
 * @email shenyuan.huang@etu.unice.fr
 */
public class Simulator {
    private double infect = 0.1;
    private double recover = 1;
    private double die = 0.1;
    private double sick = 0.5;
    private double dieAnimal = 0.2;

    private static final double ACCIDENT_RATE = 0;
    private static final double SICK_ANIMAL_RATE = 0.5;
    private static final int NOMBER_HUMAN = 40;
    private static final int NOMBER_CHICKEN = 15;
    private static final int NOMBER_PIG = 10;
    private Sandbox sandbox = new Sandbox();
    private final Map<State, Map<Event, Supplier<State>>> dict = new HashMap<>();

    Simulator() {
        buildDict();
        initial();
    }
    Virus H1N1= new Virus("H1N1",0.2,0.4,0.5,0.5,0.2);
    Virus HHHH= new Virus("HHHH",0.5,0.5,0.5,0.5,0.5);
    
    private void setProperty(Virus virus) {
        infect = virus.getInfectrate();
        recover = virus.getRecoverrate();
        die = virus.getDeadrate();
        sick = virus.getSickrate();
        dieAnimal = virus.getADeadrate();
    }

    void run(int days) {
        int dead = 0;
        for (int i = 0; i < days; i++) {
            move();
            int sicker = 0;
            int healther = 0;
            int recovered = 0;
            int contagious = 0;
            for (int x = 0; x < SIZE; x++)
                for (int y = 0; y < SIZE; y++) {
                    Location location = sandbox.getLocation(x, y);
                    Vivant vivant = location.getVivant();
                    if (!location.isVide()) {
                        if (vivant.toString().equals("H")) {
                            if (vivant.getState().equals(DEAD)) {
                                location.removeVivant();
                                continue;
                            }
                            Event event = dectEvent(location);
                            State state1 = vivant.getState();
                            if (state1.equals(CONTAGIOUS) || state1.equals(CONTAGIOUS_AND_SICK)
                                    || state1.equals(CONTAGIOUS_NOT_SICK))
                                setProperty(vivant.getVirus());
                            State state2 = dict.get(state1).get(event).get();
                            vivant.setState(state2);
                            switch (vivant.getState()) {
                            case HEALTHY:
                                healther++;
                                break;
                            case INFECTED:
                                healther++;
                                break;
                            case CONTAGIOUS_AND_SICK:
                                sicker++;
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
                            if (vivant.getState().equals(CONTAGIOUS) && Math.random() < dieAnimal)
                                vivant.setState(DEAD);

                        }
                    }
                }
            System.out.println("\n*******************");
            System.out.println("\nDAY: " + i);
            System.out.println(this);
            System.out.println("REPORT:\nsick : " + sicker);
            System.out.println("healthy : " + healther);
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
                        centre.setVirus(vivant.getVirus());
                        return CONTACT;
                    case CONTAGIOUS_AND_SICK:
                        centre.setVirus(vivant.getVirus());
                        return CONTACT;
                    case CONTAGIOUS_NOT_SICK:
                        centre.setVirus(vivant.getVirus());
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
        put(HEALTHY, CONTACT, () -> Math.random() > infect ? HEALTHY : INFECTED);
        put(INFECTED, INCUBATION_TIME, () -> Math.random() < sick ? CONTAGIOUS : INFECTED);
        put(CONTAGIOUS, CONTAGIOUS_TIME, () -> Math.random() < sick ? CONTAGIOUS_AND_SICK : CONTAGIOUS_NOT_SICK);
        put(CONTAGIOUS_NOT_SICK, CONTAGIOUS_TIME, () -> Math.random() < sick ? CONTAGIOUS_AND_SICK : RECOVERING);
        put(RECOVERING, RECOVERING_TIME, () -> Math.random() < recover ? RECOVERED : RECOVERING);
        put(CONTAGIOUS_AND_SICK, CONTAGIOUS_TIME, () -> Math.random() < die ? DEAD : RECOVERING);
        put(HEALTHY, NOTHING, () -> Math.random() > ACCIDENT_RATE ? HEALTHY : DEAD);
        put(INFECTED, NOTHING, () -> Math.random() > ACCIDENT_RATE ? HEALTHY : DEAD);
        put(CONTAGIOUS, NOTHING, () -> Math.random() > ACCIDENT_RATE ? HEALTHY : DEAD);
        put(CONTAGIOUS_NOT_SICK, NOTHING, () -> Math.random() > ACCIDENT_RATE ? CONTAGIOUS_NOT_SICK : DEAD);
        put(CONTAGIOUS_AND_SICK, NOTHING, () -> Math.random() > ACCIDENT_RATE ? CONTAGIOUS_AND_SICK : DEAD);
        put(RECOVERED, NOTHING, () -> Math.random() > ACCIDENT_RATE ? RECOVERED : DEAD);
    }

    void initial() {
        for (int i = 0; i < NOMBER_HUMAN; i++) {
            sandbox.addVivant(new Human());
        }
        for (int i = 0; i < NOMBER_CHICKEN; i++) {
            Vivant aChicken = new Chicken();
            if (Math.random() < SICK_ANIMAL_RATE) {
                aChicken.setVirus(H1N1);
                aChicken.setState(CONTAGIOUS);
            }
            sandbox.addVivant(aChicken);
        }
        for (int i = 0; i < NOMBER_PIG; i++) {
            Vivant aPig = new Pig();
            if (Math.random() < SICK_ANIMAL_RATE) {
                aPig.setState(CONTAGIOUS);
                aPig.setVirus(HHHH);
            }
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
