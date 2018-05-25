package prototype.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import prototype.vivant.Vivant;
import static prototype.affair.State.*;
/**
 * @author HUANG Shenyuan
 * @date 2018-02-23 09:23
 * @email shenyuan.huang@etu.unice.fr
 */
public class Sandbox {
    public static final int SIZE = 100;
    final Location[][] locations = new Location[SIZE][SIZE];
    private List<Location> videSpace = new ArrayList<>();
    private Random rand = new Random();

    /**
     * Build the sandbox which size is SIZEOFBOX^2
     */
    Sandbox() {
        for (int x = 0; x < SIZE; x++)
            for (int y = 0; y < SIZE; y++) {
                locations[x][y] = new Location(x,y);
                videSpace.add(locations[x][y]);
            }
    }

    void addVivant(Vivant vivant) {
        if (videSpace.isEmpty())
            throw new IllegalStateException("no space");
        int target = rand.nextInt(videSpace.size());
        videSpace.get(target).addVivant(vivant);
        videSpace.get(target).setOccupy();
        videSpace.remove(target);
    }

    void move() {
        for (int x = 0; x < SIZE; x++)
            for (int y = 0; y < SIZE; y++) {
                int target = rand.nextInt(7);
                Location[] neighbors = getNeighbor(x, y);
                locations[x][y].exchangeVivant(neighbors[target]);
            }
    }

    /**
     * @param x
     * @param y
     * @return the neighors it is a borderless map
     */
    Location[] getNeighbor(Location location) {
        int x = location.getX();
        int y = location.getY();
        int ym1 = y - 1;
        int yp1 = y + 1;
        int xm1 = x - 1;
        int xp1 = x + 1;
        if (y + 1 > SIZE - 1)
            yp1 = 0;
        if (y - 1 < 0)
            ym1 = SIZE - 1;
        if (x + 1 > SIZE - 1)
            xp1 = 0;
        if (x - 1 < 0)
            xm1 = SIZE - 1;
        Location[] neighbor = new Location[8];
        neighbor[0] = locations[x][yp1];
        neighbor[1] = locations[xm1][yp1];
        neighbor[2] = locations[xp1][yp1];
        neighbor[3] = locations[x][ym1];
        neighbor[4] = locations[xp1][yp1];
        neighbor[5] = locations[xm1][yp1];
        neighbor[6] = locations[xp1][y];
        neighbor[7] = locations[xm1][y];
        return neighbor;
    }
    
    Location[] getNeighbor(int x, int y) {
        return getNeighbor(new Location(x,y));
    }

    public Location getLocation(int x, int y) {
        return locations[x][y];
    }
    
    @Override
    public String toString() {
        StringBuilder table = new StringBuilder();
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                table.append(locations[x][y].toString());
            }
            table.append("\n");
        }
        return table.toString();
    }

    void removeDead() {
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                Vivant find = locations[x][y].getVivant();
                if(!locations[x][y].isVide()&&find.getState().equals(DEAD))
                    locations[x][y].removeVivant();
            }
        }
    }
}
