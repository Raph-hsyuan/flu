package prototype.virus;

/**
 * @author HUANG Shenyuan
 * @date 2018-04-06 10:59
 * @email shenyuan.huang@etu.unice.fr
 */
public class Virus {
    private final double infect;
    private final double recover;
    private final double die;
    private final double sick;
    private final double animalDie;
    private final String name;
    
    public Virus(String name,double infect,double recover,double die, double sick, double animalDie){
        this.infect=infect;
        this.recover=recover;
        this.die=die;
        this.sick=sick;
        this.animalDie=animalDie;
        this.name=name;
    }
    
    public double getDeadrate() {
        return die;
    }

    public double getInfectrate() {
        return infect;
    }

    public double getRecoverrate() {
        return recover;
    }

    public double getADeadrate() {
        return animalDie;
    }

    public double getSickrate() {
        return sick;
    }
    
    @Override
    public String toString() {
        return name;
    }

}
