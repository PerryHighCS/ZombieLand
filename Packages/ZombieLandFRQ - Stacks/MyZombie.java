import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Help Karl collect all of the brains.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class MyZombie extends UltraZombie
{
    /**
     * Karl's planned out actions to find the longest line of brains in front of him.
     */
    public void plan()
    {
        while(true) {
            nextBrain();
            followBrains();
        }
    }
    
    public void nextBrain() {
        turnTo(EAST);
        while (isFrontClear() && ! isBrainHere()) {
            move();
        }
        
        if (!isFrontClear()) {
            turnTo(WEST);
            while (!isBrainHere()) {
                move();
            }
            win();
        }
    }
    
    public void followBrains() {
        turnLeft();
        while (isBrainHere() && isFrontClear()) {
            move();
        }
        
        if (!isBrainHere()) {
            turnTo(SOUTH);
            move();
        }
    }
}

