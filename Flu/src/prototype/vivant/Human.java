package prototype.vivant;

import prototype.affair.State;
import prototype.virus.Virus;

/**
 * @author HUANG Shenyuan
 * @date 2018-02-23 09:06
 * @email shenyuan.huang@etu.unice.fr
 */
public class Human implements Vivant {
    private State state;
    private Virus virus;
    public Human(){
        state=State.HEALTHY;
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
