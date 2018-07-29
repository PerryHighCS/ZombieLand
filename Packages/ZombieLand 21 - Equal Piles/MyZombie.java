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
     * Karl's planned out actions to duplicate the pile of brains in front of him.
     */
    public void plan() 
    {
       move();//sets up for the first action
       if(isBrainHere()){//move the brains two spaces over until there arent any in the original pile
         moveBrain();  
       }
       move();//sets up for the new action
       move();
       turnAround();
       if(isBrainHere()){//for every brain in the new pile, delete it and place a copy on both goal piles
       copyBrain();
       }
       win();//u r done
    }
    public void copyBrain()
    {//copies a brain from your new stock pile and places it twice
    takeBrain();
       move();//moves to the new space
       putBrain();//places the brain
       move();//moves to the new space
       putBrain();//places the brain
       turnAround();//moves back the first spot - restes
       move();
       move();
       turnAround();
       if(isBrainHere()){//if there is still at least a brain in the pile, calls itself
       copyBrain();
       }
       
    }
    public void moveBrain()
    {//moves all the brains for the first pile into one two spaces over
        takeBrain();//takes teh brain
       move();//moves to the new space
       move();
       putBrain();//places the brain
       turnAround();//moves back the first spot - restes
       move();
       move();
       turnAround();
       if(isBrainHere()){//if there is still at least a brain in the pile, calls itself
       moveBrain();
       }
       
    }
}
