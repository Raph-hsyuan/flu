package prototype.core;

import prototype.vivant.Vivant;

/**
 * @author HUANG Shenyuan
 * @date 2018-02-23 10:20
 * @email shenyuan.huang@etu.unice.fr
 */
public class Location {
    private boolean isVide;
    private Vivant vivant;
    private int x;
    private int y;

    Location(int x, int y) {
        isVide = true;
        this.x = x;
        this.y = y;
    }

    int getX() {
        return x;
    }

    int getY() {
        return y;
    }

    private void setVivant(Vivant vivant) {
        this.vivant = vivant;
    }

    public Vivant getVivant() {
        return this.vivant;
    }

    boolean addVivant(Vivant vivant) {
        if (!isVide)
            return false;
        this.setVivant(vivant);
        return true;

    }

    public boolean isVide() {
        return isVide;
    }

    void setOccupy() {
        isVide = false;
    }
    
    void removeVivant() {
        isVide = true;
    }

    void exchangeVivant(Location location) {
        Vivant vivantA = location.getVivant();
        boolean stat = location.isVide;
        location.isVide = isVide;
        location.setVivant(this.vivant);
        this.setVivant(vivantA);
        isVide = stat;
    }

    @Override
    public String toString() {
        return isVide ? "[     ]" : "[" + vivant + vivant.getState() + "]";
    }

}
