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
     * Karl's planned out actions to multiply the piles of brains in front of him.
     */
    public void plan() 
    {
        //makes sure no zero in multiplying and ends on 1st tile
       checkZeroCase();
       //takes the brains and puts the multiplied brain in the 2nd tile
       multiply();
       //gg ez
       win();
    }
    public void multiply()
    {
        move();
        //the while loop should end the zombie on the 1st brain in the row facing right
       while(isBrainHere())
       {
           //takes a brain to start a new cycle(takes a ticket)
           takeBrain();
           move();
           //will make two copies of the same brain in the last two tiles
           duplicate();
           move();
           turnAround();
           while(isBrainHere())
           {
               takeBrain();
               move();
               putBrain();
               turnAround();
               move();
               turnAround();
            }
           move();
           move();
           turnAround();
        }
        move();
        while(isBrainHere())
        {
            takeBrain();
        }
        move();
        move();
        turnAround();
        //moves all the brains to the correct tile
        while(isBrainHere())
        {
            takeBrain();
            move();
            move();
            move();
            putBrain();
            turnAround();
            move();
            move();
            move();
            turnAround();
        }
        move();
        move();
        move();
        move();
    }
    public void duplicate()
    {
        while(isBrainHere())
        {
            takeBrain();
            move();
            putBrain();
            move();
            putBrain();
            turnAround();
            move();
            move();
            turnAround();
        }
    }
    //starts on 1st tile ends on 1st tile
    public void checkZeroCase()
    {
        //purely to check the two tiles for any missing brains and assume that is a zero and pickup the other brain and terminate the program.
        move();
        if(!isBrainHere())
        {
            move();
            if(isBrainHere())
            {
                while(isBrainHere())
                {
                    takeBrain();
                }
                turnAround();
                move();
                win();
            }
        }
        //if brain is there
        else
        {
            move();
            if(!isBrainHere())
            {
                turnAround();
                move();
                while(isBrainHere())
                {
                    takeBrain();
                }
                move();
                win();
            }
        }
        turnAround();
        while(isFrontClear())
        {
            move();
        }
        turnAround();
    }
}
