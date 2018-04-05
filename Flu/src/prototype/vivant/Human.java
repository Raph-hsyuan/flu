package prototype.vivant;

import prototype.affair.State;

/**
 * @author HUANG Shenyuan
 * @date 2018-02-23 09:06
 * @email shenyuan.huang@etu.unice.fr
 */
public class Human implements Vivant {
    State state;
    public Human(){
        state=State.HEALTHY;
    }

    
    @Override
    public String toString() {
        return "H";
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
