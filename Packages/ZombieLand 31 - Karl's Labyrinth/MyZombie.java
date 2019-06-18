import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Help Karl collect all of the brains.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class MyZombie extends UltraZombie
{   
    public MyZombie() {
        super();
    }
    
    public MyZombie(int brains) {
        super(brains);
    }
    
    /**
     * Karl's planned out actions to find his way to the ZombieGoal.
     */
    public void plan() 
    {              
        if (haveBrains()) {putBrain();}
        solveMaze();
    }
    
    private void solveMaze() {
        if (hasWon()) {
            return;
        }
        
        if (isFrontClear()) {
            move();
            if (haveBrains()) {putBrain();}
            solveMaze();
        }
        else {
            turnAround();
        }
        
        if (isRightClear()) {
            turnRight();
            move();
            if (haveBrains()) {putBrain();}
            solveMaze();
        }
        else {
            turnLeft();
        }
        
        if (isFrontClear()) {
            move();
            if (haveBrains()) {putBrain();}
            solveMaze();
            turnLeft();
        }
        else {
            turnRight();
        }
        
        if (isBrainHere()) {takeBrain();}
        if (isFrontClear()) {
            move();
        }
    }
    
}