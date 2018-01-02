import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

import java.awt.image.BufferedImage;
import java.awt.Graphics2D;

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
public class MyZombie extends Actor
{
    private int numBrains = 0;
    private GreenfootImage img;
    
    public MyZombie() {
        img = this.getImage();
    }
    
    public MyZombie(int numBrains) {
        this();
        this.numBrains = numBrains;
        showImage();
    }
    
    public void act() {}
    
    public void setNumBrains(int numBrains) {
        this.numBrains = numBrains;
        showImage();
    }
    
    public int getNumBrains() {
        return numBrains;
    }
    
    public void showImage() {
        GreenfootImage newimg = new GreenfootImage(img);
        
        if (numBrains > 0) {
                GreenfootImage brainsLabel = new GreenfootImage("" + numBrains, 14, Color.WHITE, Color.BLACK);
                BufferedImage blImg = brainsLabel.getAwtImage();
                BufferedImage frmImg = img.getAwtImage();
                
                BufferedImage bi = newimg.getAwtImage();
                Graphics2D graphics = (Graphics2D)bi.getGraphics();
                graphics.drawImage(frmImg, null, 0, 0);
                graphics.drawImage(blImg, null, 0, 0);
        }
        
        setImage(newimg);
    }
    
}
