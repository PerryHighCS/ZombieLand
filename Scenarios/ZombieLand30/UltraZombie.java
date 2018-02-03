import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.List;

/**
 * Write a description of class UltraZombie here.
 * 
 * @author bdahlem 
 * @version 7/26/2017
 */
public abstract class UltraZombie extends Zombie
{
    public static final int EAST = 0;
    public static final int SOUTH = 90;
    public static final int WEST = 180;
    public static final int NORTH = 270;
    
    public UltraZombie() {
        super();
    }
    
    /**
     * Determine which direction the UltraZombie is facing.
     * 
     * @return a value of EAST, SOUTH, WEST, or NORTH
     */
    public final int facing()
    {
       return (getRotation() / 90) * 90;
    }
    
    /**
     * Determine if the UltraZombie is facing a particular direction.
     * @param direction The direction to check (NORTH, SOUTH, EAST, or WEST)
     * @return true if the UltraZombie is facing that direction.
     */
    public final boolean isFacing(int direction) 
    {
        return facing() == direction;
    }
    
    /**
     * Turn 90 degrees to the left
     */
    public final void turnLeft()
    {
        synchronized (Zombie.class) {
            try {
                Zombie.class.wait();
                turn(-1);
            }
            catch (InterruptedException e) {
            }
        }
    }
    
    /**
     * Turn 180 degrees
     */
    public final void turnAround()
    {
        synchronized (Zombie.class) {
            try {
                Zombie.class.wait();
                turn(2);
            }
            catch (InterruptedException e) {
            }
        }
    }
    
    /**
     * Turn the zombie to face a particular direction (NORTH, SOUTH, EAST, or WEST);
     */
    public final void turnTo(int direction) 
    {
        synchronized (Zombie.class) {
            try {
                Zombie.class.wait();
                turn((direction - getRotation()) / 90);
            }
            catch (InterruptedException e) {
            }
        }
    }
    
    /**
     * Check if there is a wall or the edge of the world to the right of the zombie.
     */
    public final boolean isRightClear() {
        synchronized (Zombie.class) {
            try {
                Zombie.class.wait();
                
                int dir = facing();
                int dx = 0;
                int dy = 0;
        
                if (dir == EAST) {
                    dy = 1;
                }
                else if (dir == SOUTH) {
                    dx = -1;
                }
                else if (dir == WEST) {
                    dy = -1;
                }
                else {
                    dx = 1;
                }
        
                return checkDelta("Wall", dx, dy) == null &&
                        checkDelta(null, dx, dy) != this;
            }
            catch (InterruptedException e) {
            }
            return false;
        }
    }
    
    /**
     * Check if there is a wall or the edge of the world to the left of the zombie.
     */
    public final boolean isLeftClear() {
        synchronized (Zombie.class) {
            try {
                Zombie.class.wait();
                
                int dir = facing();
                int dx = 0;
                int dy = 0;
        
                if (dir == EAST) {
                    dy = -1;
                }
                else if (dir == SOUTH) {
                    dx = 1;
                }
                else if (dir == WEST) {
                    dy = 1;
                }
                else {
                    dx = -1;
                }
        
                return checkDelta("Wall", dx, dy) == null &&
                        checkDelta(null, dx, dy) != this;
            }
            catch (InterruptedException e) {
            }
            return false;
        }
    }
    
    /**
     * Check if there is a wall or the edge of the world to the back of the zombie.
     */
    public final boolean isBackClear() {
        synchronized (Zombie.class) {
            try {
                Zombie.class.wait();
                
                int dir = facing();
                int dx = 0;
                int dy = 0;
        
                if (dir == EAST) {
                    dx = -1;
                }
                else if (dir == SOUTH) {
                    dy = -1;
                }
                else if (dir == WEST) {
                    dx = 1;
                }
                else {
                    dy = 1;
                }
        
                return checkDelta("Wall", dx, dy) == null &&
                        checkDelta(null, dx, dy) != this;
            }
            catch (InterruptedException e) {
            }
            return false;
        }
    }
    
    /**
     * Check if there is a wall or the edge of the world in a certain direction from the Zombie.
     * @param direction The direction to look for a wall (NORTH, SOUTH, EAST, or WEST)
     */
    public final boolean isDirectionClear(int direction) {
        synchronized (Zombie.class) {
            try {
                Zombie.class.wait();
                
                int dx = 0;
                int dy = 0;
                
                switch (direction) {
                    case EAST:
                        dx = 1;
                        break;
                    case SOUTH:
                        dy = 1;
                        break;
                    case WEST:
                        dx = -1;
                        break;
                    case NORTH:
                        dy = -1;
                        break;
                }
                
                return checkDelta("Wall", dx, dy) == null &&
                        checkDelta(null, dx, dy) != this;
            }
            catch (InterruptedException e) {
            }
            return false;
        }
    }
    
    /**
     * Check for an object of a particular class at an offset from the zombie or if that distance is beyond the
     * edge of the world
     *
     * @param classname The class to check for.  If null, look for the edge of the world
     * @param dx The distance(in cells) along the x-axis to look for the object/edge
     * @param dy The distance(in cells) along the y-axis to look for the object/edge
     * @return The object at the offset, or a reference to this zombie if the offset is off the edge of
     *         the world, null if no object of the given class is at that distance or the world does not
     *         end within that distance.
     */
    private Actor checkDelta(String classname, int dx, int dy){        
        if (classname != null) {
            List<Actor> objects = getObjectsAtOffset(dx, dy, null);
            
            for (Actor a : objects) {
                if (a.getClass().getName().equals(classname)){
                    return a;
                }
            }
            
            return null;
        }
        else {
            int nextX = getX() + dx;
            int nextY = getY() + dy;
            if ((nextX >= 0 && nextX < getWorld().getWidth()) &&
                (nextY >= 0 && nextY < getWorld().getHeight())) {
                return getOneObjectAtOffset(dx, dy, null);
            }
            else {
                return this;
            }
        }
    }
}
