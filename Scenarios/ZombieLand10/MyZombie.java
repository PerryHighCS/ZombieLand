import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Plan the MyZombie's actions. 
 * 
 * Zombies aren't that smart, in order to reach their goal, they must follow a plan to the 
 * letter.  If the zombie runs into an obstacle that isn't accounted for in the plan, the 
 * zombie's plan will fail.  If the zombie's plan runs out of steps, the zombie will give
 * up and die.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class MyZombie extends Zombie
{
    /**
     * Plan the MyZombie's actions. 
     * 
     * Use the move() and turnRight() methods to help your MyZombie reach the goal.
     * 
     * This method is called whenever the 'Act' or 'Run' button gets pressed in the Greenfoot
     * environment.
     */
    public void plan() 
    {
        turnRight();
        forward();
        nextHall();
        forward();
        takeBrain();
        turnAround();
        forward();
        nextHall();
        forward();
    }
}
