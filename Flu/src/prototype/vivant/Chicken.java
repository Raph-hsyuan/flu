package prototype.vivant;

import prototype.affair.State;

/**
 * @author HUANG Shenyuan
 * @date 2018-03-30 01:20
 * @email shenyuan.huang@etu.unice.fr
 */
public class Chicken implements Animal {
    State state;

    public Chicken() {
        state = State.HEALTHY;
    }

    @Override
    public String toString() {
        return "C";
    }

    @Override
    public State getState() {
        return state;
    }
    
    @Override
    public void setState(State state) {
        this.state=state;       
    }
}
