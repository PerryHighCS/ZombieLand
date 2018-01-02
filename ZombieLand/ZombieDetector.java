import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Detects each time a Zombie enters the cell with the detector
 * 
 * @author bdahlem 
 * @version 1.0
 */
public abstract class ZombieDetector extends Actor
{
    private boolean isOn;
    
    /**
     * Create an invisible ZombieDetector
     */
    public ZombieDetector()
    {
        isOn = false;
    }
    
    /**
     * Check if a Zombie has stepped on the detector.  Increment the count only when the zombie
     * moves onto the detector.
     */
    public void act() 
    {
        if (isTouching(Zombie.class)) {
            if (isOn == false){     
                detected();
                isOn = true;
            }
        }
        else {
            isOn = false;
        }
    }
    
    public abstract void detected();
}
