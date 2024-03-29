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
        
        while(stillTrying()){
            nextBrain();
            followBrains();
            turnTo(EAST);
            move();
            while(!isBrainHere()){
                move();
                if(!isFrontClear()){
                    turnTo(WEST);
                    while(!isBrainHere()){
                        move();
                    }
                }
            }
            
        }
    }

    public void nextBrain()
    {
        while(!isBrainHere() && isFrontClear()){
            move();
        }
        turnAround();
    }

    public void followBrains()
    {
        turnTo(NORTH);
        while(isBrainHere()){
            move();
        }
        turnTo(SOUTH);
        move();
    }
}
