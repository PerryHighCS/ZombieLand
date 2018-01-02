import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.List;

/**
 * A special ZombieLand that builds a random labyrinth for Karl to wander
 */
public class ZombieLand extends World
{
    private Actor[][] worldArray;
    private boolean[][] labyrinth;
    private boolean building = false;
    
    Actor message = null;
    private boolean done = false;
    
    private Thread builder;
    
    /**
     * Create the labyrinth
     */
    public ZombieLand()
    {   
        // Create a new world with 19x30 cells with a cell size of 64x64 pixels.
        super(19, 13, 64);
        
        // Get the width and height of the world in cells.
        // This is backwards, but that's how Greenfoot works
        int width = getWidth();
        int height = getHeight();
        
        
        // Create an array to track the visited locations in the labyrinth
        labyrinth = new boolean[width / 2][height / 2];
        for (int i = 0; i < width / 2; i++) {
            for (int j = 0; j < height / 2; j ++) {
                labyrinth[i][j] = false;
            }
        }

        // Create an array to hold all of the objects in the world
        worldArray = new Actor[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                worldArray[i][j] = new Wall();
                addObject(worldArray[i][j], i, j);
            }
        }
        
        // Build the labyrinth in a new thread so it can be animated
        building = true;        
        builder = new Thread(new Runnable() 
        {
            public void run() {
                // Build the labyrinth starting in the bottom right corner
                build((width/2) - 1, (height/2) - 1);
                
                
                // Once the labyrinth is built, add a goal in the bottom right corner
                breakBlock(width - 1, height - 2);
                ZombieGoal g = new ZombieGoal();
                addObject(g, width - 1, height - 2);
                
                // Then make space and add Karl to the top left corner.
                breakBlock(0, 1);
                Zombie z = new MyZombie();
                addObject(z, 0, 1);
                
                // Building has now ended
                building = false;
            }
        });
        
        builder.start();
    }
    
    /**
     * Animate the building of the labyrinth by building one step per act cycle, then
     * watch the zombie's progress
     */
    public void act()
    {
        synchronized (ZombieLand.class) {
            // If the labyrinth is still being built
            if (building) {
                // Let it build one more step
                ZombieLand.class.notify();
            }
            else if (!done) {
                // If the labyrinth has been built
                synchronized (Zombie.class) {
                    // check the Zombie's progress.
                    if (checkZombies()) {}                    
                }
            }
        }
    }    
    
    /**
     * Recursively build a labyrinth
     * @param x The horizontal labyrinth coordinate to build from
     * @param y The vertical labyrinth coordinate to build from
     */
    private void build(int x, int y) {        
        synchronized (ZombieLand.class) {
            // Convert the current x,y cooridnate values to world map values
            int mapX = x * 2 + 1;
            int mapY = y * 2 + 1;
            
            // Create a list of the directions to move from this location, in a random order
            int dir[] = {0, 1, 2, 3};
            shuffle(dir);
            
            // Mark this location as visited
            labyrinth[x][y] = true;
            
            // Break the block in this location
            breakBlock(mapX, mapY);
            
            // Add a flam to this location
            Fire flame = new Fire();
            addObject(flame, mapX, mapY);

            try {            
                // Look in each direction from this cell, one at a time
                // If the cell in that direction has not yet been visited, break the block
                // separating this cell and that one, then continue building in that direction
                for (int direction : dir) {                    
                    switch(direction) {
                        case 0: // Up
                            if (y > 0 && labyrinth[x][y-1] == false) { 
                                // Wait for an act cycle before continuing
                                ZombieLand.class.wait();
                                
                                breakBlock(mapX, mapY - 1);
                                build(x, y-1);
                            }
                            break;
                        case 1: // Left
                            if (x > 0 && labyrinth[x-1][y] == false) {
                                // Wait for an act cycle before continuing
                                ZombieLand.class.wait();
                                
                                breakBlock(mapX - 1, mapY);
                                build(x-1, y);
                            }
                            break;
                        case 2: // Down
                            if (y < labyrinth[x].length - 1 && labyrinth[x][y+1] == false) {
                                 // Wait for an act cycle before continuing
                                 ZombieLand.class.wait();
                                 
                                 breakBlock(mapX, mapY + 1);
                                 build(x, y+1);
                            }
                            break;
                        case 3: // Right
                            if (x < labyrinth.length - 1&& labyrinth[x+1][y] == false) {
                                // Wait for an act cycle before continuing
                                ZombieLand.class.wait();
                                
                                breakBlock(mapX + 1, mapY);
                                build(x+1, y);
                            }
                            break;
                    }
                }
                
                // Wait for an act cycle before extinguishing the flame to show this cell is done
                ZombieLand.class.wait(); 
                flame.extinguish();
            }
            catch (InterruptedException | java.lang.IllegalStateException e) {}
        }        
    }
    
    /**
     * Display a message in the World
     */
    public void showMessage(String msg)
    {
        if (message != null) {
            removeObject(message);
        }

        message = new Actor(){public void act(){}};

        int xOffset = 0;
        int yOffset = 0;

        if (getWidth() % 2 == 0) {
            xOffset = getCellSize() / 2;
        }
        if (getHeight() % 2 == 0) {
            yOffset = getCellSize() / 2;
        }

        GreenfootImage img = new GreenfootImage(1,1);

        java.awt.Font f = new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.BOLD, 30);
        java.awt.Image image = img.getAwtImage();
        java.awt.Graphics g = img.getAwtImage().createGraphics();
        g.setFont(f);
        java.awt.FontMetrics fm = g.getFontMetrics(f);

        int textWidth = fm.stringWidth(msg);
        int textHeight = fm.getHeight() + fm.getMaxDescent();
        int textBottom = textHeight - fm.getMaxDescent();

        img = new GreenfootImage((textWidth  + xOffset)* 2, textHeight + yOffset * 2);
        g = img.getAwtImage().createGraphics();
        g.setColor(java.awt.Color.BLACK);
        g.setFont(f); 

        int x = textWidth / 2 ;
        int y = textBottom;

        g.drawString(msg, x-1, y-1);
        g.drawString(msg, x, y-1);
        g.drawString(msg, x+1, y-1);
        g.drawString(msg, x-1, y);
        g.drawString(msg, x+1, y);
        g.drawString(msg, x-1, y+1);
        g.drawString(msg, x, y+1);
        g.drawString(msg, x+1, y+1);

        g.setColor(java.awt.Color.WHITE);
        g.drawString(msg, x, y);

        message.setImage(img);
        addObject(message, getWidth() / 2, getHeight() / 2);
    }

    /**
     * When the mission is ended, stop the world.
     */
    public void finish(boolean success)
    {
        done = true;
    }

    /**
     * When the mission is ended, stop the world.
     */
    public void finish(String msg, boolean success)
    {
        showMessage(msg);
        done = true;
    }

    /**
     * Check whether the scenario is complete.
     */
    public boolean isFinished()
    {
        return done;
    }

    /**
     * End the world if there aren't any zombies left, or the Zombie reached the goal.
     */
    public boolean checkZombies()
    {    
        if (!done) {
            List<Zombie> zombies = getObjects(Zombie.class);

            if (zombies.size() == 0) {
                finish("Zombie no more.", false);
                return false;
            }
            else {
                boolean allDead = true;                
                boolean allWon = true;
                for (Zombie z : zombies) {
                    if (!z.isDead()) {
                        allDead = false;
                    }
                    if (!z.hasWon()) {
                        allWon = false;
                    }
                }

                if (allDead) {
                    finish("Zombie dead.", false);
                    return false;
                }
               
                if (allWon) {
                    finish("Zombie do good.", true);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Randomize an array of ints
     */
    private void shuffle(int[] arr) {
        // Move each element in the array to a random new location by swapping the two elements
        for (int i = 0; i < arr.length; i++) {
            int j = (int)(Math.random() * arr.length);
            int tmp = arr[i];
            arr[i] = arr[j];
            arr[j] = tmp;
        }
    }
    
    /**
     * Remove the object at a given x,y location in the map
     */
    private void breakBlock(int x, int y) {
        removeObject(worldArray[x][y]);
        worldArray[x][y] = null;
    }
}
