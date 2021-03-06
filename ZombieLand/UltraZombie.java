import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * An evolved zombie with POWERS!
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
    
    protected UltraZombie(int numBrains) {
        super(numBrains);
    }
    
    /**
     * Determine which direction the UltraZombie is facing.
     * 
     * @return a value of EAST, SOUTH, WEST, or NORTH
     */
    public final int facing()
    {
        CompletableFuture<Integer> result = new CompletableFuture<>();
            
        nextAction(()->
            {
                
                result.complete(dirFacing());
            },
            debugLog()
        );
            
        try {
            return result.get();
        }
        catch (InterruptedException | ExecutionException e) {
        }
        return 0;
    }
    
    private int dirFacing()
    {
        return (getRotation() / 90) * 90;
    }
    
    /**
     * Determine if the UltraZombie is facing a particular direction.
     * <p>
     * Example:
     * <pre>
     * {@code
     *    if (isFacing(EAST)) // If the zombie is facing the right
     *     {
     *         turnLeft();     // turn to face NORTH
     *     }
     * }
     * </pre> 
     * </p>
     * @param direction The direction to check (NORTH, SOUTH, EAST, or WEST)
     * @return true if the UltraZombie is facing that direction.
     */
    public final boolean isFacing(int direction) 
    {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
            
        nextAction(()->
            {
                result.complete(dirFacing() == direction);
            },
            debugLog()
        );
            
        try {
            return result.get();
        }
        catch (InterruptedException | ExecutionException e) {
        }
        return false;
    }
    
    /**
     * Turn 90 degrees to the left
     */
    public final void turnLeft()
    {           
       nextAction(()->
            {
                turn(-1);
            },
            debugLog()
        );
    }
    
    /**
     * Turn 180 degrees
     */
    public final void turnAround()
    {
        nextAction(()->
            {
                turn(2);
            },
            debugLog()
        );
    }
    
    /**
     * Turn the zombie to face a particular direction (NORTH, SOUTH, EAST, or WEST);
     * <p>
     * Example:
     * <pre>
     * {@code
     *     turnTo(SOUTH);
     * }
     * </pre> 
     * </p>
     */
    public final void turnTo(int direction) 
    {
        nextAction(()->
            {
                turn((direction - getRotation()) / 90);
            },
            debugLog()
        );
    }
    
    /**
     * Check if there is a wall or the edge of the world to the right of the zombie.
     * <p>
     * Example:
     * <pre>
     * {@code
     *    if (isRightClear()) // If the zombie can move to the right
     *     {
     *         turnRight();    // face that way
     *     }
     * }
     * </pre> 
     * </p>
     */
    public final boolean isRightClear() {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
            
        nextAction(()->
            {
                int dir = dirFacing();
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
                result.complete(checkDelta("Wall", dx, dy) == null &&
                                checkDelta(null, dx, dy) != this);
            },
            debugLog()
        );
            
        try {
            return result.get();
        }
        catch (InterruptedException | ExecutionException e) {
        }
        return false;
    }
    
    /**
     * Check if there is a wall or the edge of the world to the left of the zombie.
     * <p>
     * Example:
     * <pre>
     * {@code
     *    if (isLeftClear()) // If the zombie can move to the left
     *     {
     *         turnLeft();    // face that way
     *     }
     * }
     * </pre> 
     * </p>
     */
    public final boolean isLeftClear() {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
            
        nextAction(()->
            {
                int dir = dirFacing();
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
                
                result.complete(checkDelta("Wall", dx, dy) == null &&
                                checkDelta(null, dx, dy) != this);
            },
            debugLog()
        );
            
        try {
            return result.get();
        }
        catch (InterruptedException | ExecutionException e) {
        }
        return false;
    }
    
    /**
     * Check if there is a wall or the edge of the world to the back of the zombie.
     * <p>
     * Example:
     * <pre>
     * {@code
     *    if (isBackClear()) // If the zombie can move to backwards
     *     {
     *         turnAround();    // face that way
     *     }
     * }
     * </pre> 
     * </p>
     */
    public final boolean isBackClear() {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
            
        nextAction(()->
            {
                int dir = dirFacing();
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
                result.complete(checkDelta("Wall", dx, dy) == null &&
                                checkDelta(null, dx, dy) != this);
            },
            debugLog()
        );
            
        try {
            return result.get();
        }
        catch (InterruptedException | ExecutionException e) {
        }
        return false;
    }
    
    /**
     * Check if there is a wall or the edge of the world in a certain direction from the Zombie.
     * <p>
     * Example:
     * <pre>
     * {@code
     *    if (isDirectionClear(NORTH)) // If the zombie can move to the north
     *     {
     *         turnTo(NORTH);    // face that way
     *     }
     * }
     * </pre> 
     * </p>
     * @param direction The direction to look for a wall (NORTH, SOUTH, EAST, or WEST)
     */
    public final boolean isDirectionClear(int direction) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
            
        nextAction(()->
            {
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
                result.complete(checkDelta("Wall", dx, dy) == null &&
                                checkDelta(null, dx, dy) != this);
            },
            debugLog()
        );
            
        try {
            return result.get();
        }
        catch (InterruptedException | ExecutionException e) {
        }
        return false;
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
