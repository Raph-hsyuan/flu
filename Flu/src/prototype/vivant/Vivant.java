package prototype.vivant;

import prototype.affair.State;

/**
 * @author HUANG Shenyuan
 * @date 2018-02-23 08:45
 * @email shenyuan.huang@etu.unice.fr
 */
public interface Vivant {
    State getState();
    void setState(State state);
}
