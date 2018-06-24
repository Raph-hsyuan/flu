package prototype.core;

import static prototype.affair.Event.*;
import static prototype.affair.State.*;
import static prototype.core.Sandbox.SIZE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import prototype.graph.SimulatorView;
import prototype.affair.Event;
import prototype.affair.State;
import prototype.vivant.Chicken;
import prototype.vivant.Human;
import prototype.vivant.Pig;
import prototype.vivant.Vivant;
import prototype.virus.Virus;

/**
 * @author HUANG Shenyuan
 * @author Wang Chengzhi
 * @date 2018-03-01 23:15
 * @email shenyuan.huang@etu.unice.fr
 */
public class Simulator {
    private double infectRate;
    private double recoverRate;
    private double dieRate;
    private double sickRate;
    private double dieAnimalRate;
    private static int step = 0;
    private static final double ACCIDENT_RATE = 0.000001;
    private static final double SICK_ANIMAL_RATE = 0.4;
    private static final int NOMBER_HUMAN = 13288;
    private static final int NOMBER_CHICKEN = 40;
    private static final int NOMBER_PIG = 20;
    private Sandbox sandbox = new Sandbox();
    private final Map<State, Map<Event, Supplier<State>>> dict = new HashMap<>();
    private List<SimulatorView> views = new ArrayList<>();
    private static int sick = 0;
    private static int healthy = 0;
    private static int recovered = 0;
    private static int contagious = 0;
    private static int dead = 0;
    private int nothing = 0;
    private static Virus chickVirus;
    private static Virus pigVirus;
    private boolean firstTime = true;
    
    public Simulator(SimulatorView... views) {
        buildDict();
        Arrays.asList(views).forEach(v -> this.views.add(v));
    }

    public static void setChickenVirus(Virus virus) {
        chickVirus = virus;
    }
    
    public static void setPigVirus(Virus virus) {
        pigVirus = virus;
    }
    
    public static int getNumber(State state) {
        switch(state) {
        case CONTAGIOUS : return contagious;
        case HEALTHY : return healthy;
        case DEAD : return dead;
        case RECOVERED : return recovered;
        case SICK : return sick;
        default : return 0;
        }
    }

    private void setProperty(Virus virus) {
        infectRate = virus.getInfectrate();
        recoverRate = virus.getRecoverrate();
        dieRate = virus.getDeadrate();
        sickRate = virus.getSickrate();
        dieAnimalRate = virus.getADeadrate();
    }

    private void initialVivant() {
        if(firstTime) {
        setHuman(NOMBER_HUMAN);
        setChicken(NOMBER_CHICKEN);
        setPig(NOMBER_PIG);
        firstTime = false;
        }
    }
    
    public int simulateOneStep() {
        initialVivant();
        step++;
        sandbox.removeDead();
        move();
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
                        if (state1.equals(CONTAGIOUS) 
                                || state1.equals(SICK)
                                || state1.equals(CONTAGIOUS_NOT_SICK)
                                || state1.equals(RECOVERING)||event.equals(CONTACT))
                            setProperty(vivant.getVirus());
                        State state2 = dict.get(state1).get(event).get();
                        vivant.setState(state2);
                    } else {
                        if (vivant.getState().equals(CONTAGIOUS) && Math.random() < dieAnimalRate)
                            vivant.setState(DEAD);
                    }
                }
            }
        countState();
        return step;
    }

    void countState() {
        sick = 0;
        healthy = 0;
        recovered = 0;
        contagious = 0;
        for(int x = 0; x < SIZE; x++)
            for(int y = 0; y < SIZE; y++) {
                Location location = sandbox.getLocation(x, y);
                if(location.isVide()) continue;
                Vivant vivant = location.getVivant();
              switch (vivant.getState()) {
              case HEALTHY:
                  healthy++;
                  break;
              case INFECTED:
                  healthy++;
                  break;
              case SICK:
                  sick++;
                  contagious++;
                  break;
              case CONTAGIOUS_NOT_SICK:
                  contagious++;
                  break;
              case RECOVERING:
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
            }
    }
    void run(int days) {
        for (int i = 0; i < 100; i++) {
            simulateOneStep();
            System.out.println(this);
        }
    }

    Event dectEvent(Location loc) {
        Event event = NOTHING;
        Vivant centre = loc.getVivant();
        if (centre.getState().equals(CONTAGIOUS))
            return CONTAGIOUS_TIME;
        if (centre.getState().equals(CONTAGIOUS_NOT_SICK))
            return CONTAGIOUS_TIME;
        if (centre.getState().equals(SICK))
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
                    case SICK:
                        centre.setVirus(vivant.getVirus());
                        return CONTACT;
                    case CONTAGIOUS_NOT_SICK:
                        centre.setVirus(vivant.getVirus());
                        return CONTACT;
                    case RECOVERING:
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
        put(HEALTHY, CONTACT, () -> Math.random() < infectRate ?  INFECTED : HEALTHY);
        put(INFECTED, INCUBATION_TIME, () -> Math.random() < sickRate ? CONTAGIOUS : INFECTED);
        put(CONTAGIOUS, CONTAGIOUS_TIME, () -> Math.random() < sickRate ? SICK : CONTAGIOUS_NOT_SICK);
        put(CONTAGIOUS_NOT_SICK, CONTAGIOUS_TIME, () -> Math.random() < sickRate ? SICK : RECOVERING);
        put(RECOVERING, RECOVERING_TIME, () -> Math.random() < recoverRate ? RECOVERED : RECOVERING);
        put(SICK, CONTAGIOUS_TIME, () -> Math.random() < dieRate ? DEAD : RECOVERING);
        put(HEALTHY, NOTHING, () -> Math.random() > ACCIDENT_RATE ? HEALTHY : DEAD);
        put(INFECTED, NOTHING, () -> Math.random() > ACCIDENT_RATE ? HEALTHY : DEAD);
        put(CONTAGIOUS, NOTHING, () -> Math.random() > ACCIDENT_RATE ? HEALTHY : DEAD);
        put(CONTAGIOUS_NOT_SICK, NOTHING, () -> Math.random() > ACCIDENT_RATE ? CONTAGIOUS_NOT_SICK : DEAD);
        put(SICK, NOTHING, () -> Math.random() > ACCIDENT_RATE ? SICK : DEAD);
        put(RECOVERED, NOTHING, () -> Math.random() > ACCIDENT_RATE ? RECOVERED : DEAD);
    }

    void setHuman(int number) {
        for (int i = 0; i < number; i++) {
            sandbox.addVivant(new Human());
        }
    }

    void setPig(int number) {
        for (int i = 0; i < number; i++) {
            Vivant aPig = new Pig();
            if (Math.random() < SICK_ANIMAL_RATE) {
                aPig.setState(CONTAGIOUS);
                aPig.setVirus(pigVirus);
            }
            sandbox.addVivant(aPig);
        }
    }

    void setChicken(int number) {
        for (int i = 0; i < NOMBER_CHICKEN; i++) {
            Vivant aChicken = new Chicken();
            if (Math.random() < SICK_ANIMAL_RATE) {
                aChicken.setVirus(chickVirus);
                aChicken.setState(CONTAGIOUS);
            }
            sandbox.addVivant(aChicken);
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

    /**
     * @param step2
     * @return
     */
    public boolean isViable() {
        if (getNumber(CONTAGIOUS) + getNumber(SICK) == 0)
            nothing++;
        return nothing <= 40;
    }
    
    public void updateViews() {
        views.forEach(v -> v.showStatus(step, this.sandbox));
    }
    
    public void reset() {
        sandbox.removeAll();
        setHuman(NOMBER_HUMAN);
        setChicken(NOMBER_CHICKEN);
        setPig(NOMBER_PIG);
    }

}
