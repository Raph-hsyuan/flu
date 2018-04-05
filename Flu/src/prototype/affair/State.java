package prototype.affair;

/**
 * @author HUANG Shenyuan
 * @date 2018-02-23 08:44
 * @email shenyuan.huang@etu.unice.fr
 */
public enum State {
    CONTAGIOUS("_CON"), HEALTHY("_HEA"), INFECTED("_INF"), CONTAGIOUS_AND_SICK("_C&S"), CONTAGIOUS_NOT_SICK("_C!S"), RECOVERING("_RNG"), DEAD("_DEA"), RECOVERED("_RED");
    
    private String name;
    
    State(String name){
        this.name=name;
    }
    
    @Override
    public String toString() {
        return this.name;
    }
}
