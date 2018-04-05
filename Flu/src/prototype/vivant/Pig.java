package prototype.vivant;

import prototype.affair.State;

/**
 * @author HUANG Shenyuan
 * @date 2018-03-30 01:18
 * @email shenyuan.huang@etu.unice.fr
 */
public class Pig implements Animal {
    State state;
    public Pig(){
        state=State.HEALTHY;
    }

    @Override
    public String toString() {
        return "P";
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
