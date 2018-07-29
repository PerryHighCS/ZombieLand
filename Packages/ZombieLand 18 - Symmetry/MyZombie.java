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
public class MyZombie extends UltraZombie
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
        if(!isBrainHere()) //Checks if the zombie starts on a brain, if not move forward.
        {
            if(isFrontClear())
            {
                move();
            }
        }
        if(!isBrainHere()) //Checks if the second tile has a brain, if not move forward.
        {
            if(isFrontClear())
            {
                move();
            }
        }
        moveTake();
        while(!isBrainHere())
        {
            while(isFrontClear())
            {
                move();
            }
            putBrain();
        }
        takeBrain();
        checkLeft();
        checkRight();
        putBrain();
        forwardCheckWhile();
        putBrain();
        turnAround();
        move();
        centerWinCheckCombo();
        centerWinCheckCombo();
        centerWinCheckCombo();
        centerWinCheckCombo();
        centerWinCheckCombo();
        centerWinCheckCombo();
        centerWinCheckCombo();
        centerWinCheckCombo();
        centerWinCheckCombo();
        centerWinCheckCombo();
        centerWinCheckCombo();
        centerWinCheckCombo();
        centerWinCheckCombo();
        centerWinCheckCombo();
    }
    public void checkLeft() //Checks if the zombie can turn left and does it if possible.
    {
        if(isLeftClear())
        {
            turnLeft();
        }
    }
    public void checkRight() //Checks if the zombie can turn right and does it if possible.
    {
        if(isRightClear())
        {
            turnRight();
        }
    }
    public void moveTake() //Checks if there is a brain, if there is a brain take all the brains. Then move forward taking all brains until a wall is reached.
    {
        while(isBrainHere())
        {
            while(isBrainHere())
            {
                takeBrain();
            }
            forwardCheck();
        }
    }
    public void forwardCheck() //Allows the zombie to keep moving forward if a gap in a line of brains exists.
    {
        if(isFrontClear())
            {
                move();
            }
        if(!isBrainHere())
        {
            if(isFrontClear())
            {
                move();
            }
        }
    }
    public void forwardCheckWhile() //Allows the zombie to keep moving forward if a gap in a line of brains exists.
    {
        while(isFrontClear())
            {
                move();
            }
    }
    public void forwardCheckBrain() //Allows the zombie to keep moving forward if a gap in a line of brains exists.
    {
        while(!isBrainHere())
            {
                move();
            }
        takeBrain();
        turnAround();
        move();
        putBrain();
    }
    public void placeBrainMarker() //Places a brain where the zombie should end up.
    {
        while(isFrontClear())
            {
                move();
            }
        turnAround();
        while(haveBrains())
        {
            putBrain();
            if(haveBrains())
            {
                putBrain();
            }
            move();
        }
    }
    public void winCheck() //Checks if the zombie is in the proper location.
    {
        takeBrain();
        if(isBrainHere())
        {
            takeBrain();
            win();
        } else {
            putBrain();
        }
   }
   public void centerWinCheckCombo() //Centers the brain and checks if the zombie is in the proper location.
   {
       forwardCheckBrain();
       winCheck();
       move();
   }
}