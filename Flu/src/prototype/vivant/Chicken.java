package prototype.vivant;

import prototype.affair.State;
import prototype.virus.Virus;

/**
 * @author HUANG Shenyuan
 * @date 2018-03-30 01:20
 * @email shenyuan.huang@etu.unice.fr
 */
public class Chicken implements Animal {
    private State state;
    private Virus virus;
    public Chicken() {
        state = State.HEALTHY;
    }

    @Override
    public void setVirus(Virus virus) {
        this.virus=virus;
    }
    
    @Override
    public Virus getVirus() {
        return virus;
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
