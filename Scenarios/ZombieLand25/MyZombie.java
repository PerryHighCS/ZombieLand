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
     * Karl's planned out actions to sort the piles of brains in front of him.
     */
    public void plan() 
    {       
       // Loop once for each cell the world is wide
       for (int i = 0; i < getWorld().getWidth(); i++) {
           
           move();// Your code here
           
       }
       
       win();
    }
}
