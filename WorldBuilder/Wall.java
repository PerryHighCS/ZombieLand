import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Write a description of class Wall here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class Wall extends Actor
{
    /**
     * Create a block wall that fits in one tile.
     */
    public Wall()
    {
        GreenfootImage img = this.getImage();
        img.scale(64, 64);
        
    }
    
    /**
     *  It's a wall.  It doesn't do anything.
     */
    public void act() 
    {
        // Add your action code here.
    }    
}
