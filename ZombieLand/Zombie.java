import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.awt.Graphics;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.Rectangle;
import java.util.List;
import java.net.URLClassLoader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * A programmable zombie character.
 *
 * @author bdahlem
 * @version 1.0
 */
public abstract class Zombie extends Actor
{
    private int frame = 0;
    private int deadFrame = 0;

    private static final int NUM_FRAMES = 4;
    private static final GreenfootImage[][] images = preloadImages();

    public static final int ZOMBIE_GROAN = 0;
    public static final int ZOMBIE_MED_GROAN = 1;
    public static final int ZOMBIE_LONG_GROAN = 2;
    public static final int ZOMBIE_LOUD_GROAN = 3;
    public static final int ZOMBIE_GRUNT = 4;
    public static final int ZOMBIE_SCREAM = 5;
    public static final int NUM_SOUNDS = 6;

    private static GreenfootSound[] sounds = preloadSounds();

    private Thread thinker;

    private volatile int numBrains = 0;

    private volatile boolean undead = true;
    private volatile boolean won = false;

    private volatile boolean redBox = false;

    private boolean debugMode = false;

    private ArrayBlockingQueue<Runnable> actions;

    /**
     * Make a new zombie, you evil person.
     */
    public Zombie() {   
        actions = new ArrayBlockingQueue<>(1);

        thinker = new Thread(() ->
            {
                try {   
                    pass();

                    plan();    // Follow the plan

                    if (actions.size() > 0)
                        pass(); // Wait for the plan to come to an end

                    pass();    // Wait for another act cycle after the plan ends for everything to settle down

                    nextAction(()->
                        {    // If the Zombie hasn't solved its problems,
                            die();              // Kill it
                        },
                        null
                    );
                }
                catch (java.lang.IllegalStateException e) {
                    // If the plan is interrupted, or the zombie was removed, causing an illegal state,
                    // end the zombie
                    //if (stillTrying())
                    die();
                }
            }
        );
    }

    protected Zombie(int numBrains) {
        this();
        this.numBrains = numBrains;
    }

    /**
     * Perform one animation step.
     */
    public final void act()
    {
        frame = (frame + 1) % NUM_FRAMES; // Show the next animation frame

        if (thinker.getState() == Thread.State.NEW) {
            thinker.start();
        }
        else if(frame % 2 == 0) {
            // Every other animation frame, perform one action
            Runnable nextAction = actions.poll();

            if (nextAction != null && stillTrying()) {
                nextAction.run();
            }
        }
            
        ZombieLand w = (ZombieLand)getWorld();
        if (w == null) {
            return;
        }
        
        if ((won || (undead && !w.isFinished())) && Math.random() < 0.0001) {            // Play a random sound randomly if still running
            sounds[(int)(Math.random() * 2)].play();
        }
        
        showAnimationFrame();

        if (!undead || won) {  // If the zombie is no more, stop doing things.                
            if (!thinker.isInterrupted())
                thinker.interrupt();
        }

        
    }

    /**
     * The special thing about this zombie is that has a plan.
     * The zombie's plan is run in a separate thread.  Commands, such as move()
     * and turnRight() wait their turn so that they can happen asynchronously with
     * animations, etc.
     */
    public abstract void plan();

    /**
     * Determine if this zombie is still struggling to make it in this world.
     * <p>
     * Example:
     * <pre>
     * {@code
     *    if (stillTrying()) // If the zombie hasn't passed on, and hasn't reached the goal
     *     {
     *         move();
     *     }
     * }
     * </pre> 
     * </p>
     */
    public final boolean stillTrying()
    {
        boolean worldFinished = false;
        if (getWorld() != null) {
            worldFinished = ((ZombieLand)getWorld()).isFinished();
        }

        return (undead && !won && !worldFinished);
    }

    /**
     * Don't do anything for a cycle.
     */
    public void pass()
    {
        nextAction(()->{;}, debugLog());
    }

    /**
     * Move forward one step.
     */
    public final void move()
    {
        debugLog();
        nextAction(()->
            {
                boolean success = handleWall(); 
                success = success && handleBucket();
                if (success) {
                    super.move(1);
                }
                else {
                    undead = false;
                    die();
                }
            },
            debugLog()
        );
    }

    /**
     * Move forward x steps (this may look useful, but zombies don't understand numbers).
     */
    public final void move(int x)
    {
        while (x > 0 && !isDead() && !hasWon()) {
            this.move();
            x--;
        }
    }

    /**
     * Turn 90 degrees to the right.
     */
    public final void turnRight()
    {
        nextAction(()->{turn(1);}, debugLog());
    }

    /**
     * Turn to the right a given number of times (this may look useful, but zombies don't understand numbers).
     * @param turns the number of times to turn 90 degrees to the right
     */
    public final void turn(int turns)
    {        
        int degrees = turns * 90;

        if (stillTrying()) {
            //getImage().setTransparency(0);
            super.turn(degrees);
            showAnimationFrame();
            //getImage().setTransparency(255);
        }
    }   

    /**
     * Pick up brains if they exist. End if not.
     */
    public final void takeBrain()
    {        
        ClassLoader cl = this.getClass().getClassLoader();

        nextAction(()->
            {
                try {
                    if (stillTrying()) {
                        Class brainClass = cl.loadClass("Brain");
                        Actor a = getOneIntersectingObject(brainClass);

                        if (a != null) {
                            numBrains++;
                            Method remove = brainClass.getMethod("removeBrain", new Class[0]);
                            remove.invoke((Object)a, null);
                        }
                        else {
                            drawRed();
                            ((ZombieLand)getWorld()).finish("Zombie no get brain.", false);
                        }
                    }
                }
                catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                }
            },
            debugLog()
        );
    }

    /**
     * Put down a brain if the Zombie has one, if the zombie is not holding a brain, it will put down its own brain, ending its afterlife.
     */
    public final void putBrain()
    {
        ClassLoader cl = this.getClass().getClassLoader();

        nextAction(()->
            {
                try {
                    debugLog();
                    if (stillTrying()) {
                        if (numBrains > 0) {
                            numBrains--;

                            Class brainClass = cl.loadClass("Brain");
                            Actor a = getOneIntersectingObject(brainClass);

                            if (a == null) {
                                Constructor constructor = brainClass.getConstructor();
                                a = (Actor)constructor.newInstance();

                                getWorld().addObject(a, getX(), getY());
                            }
                            else {
                                Method add = brainClass.getMethod("addBrain", new Class[0]);
                                add.invoke((Object)a, null);
                            }
                        }
                        else {
                            drawRed();
                            ((ZombieLand)getWorld()).finish("Zombie no have brain.", false);
                        }
                    }
                }
                catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    //                ((ZombieLand)getWorld()).finish("Zombie no have brain.", false);
                    //                e.printStackTrace();
                }
            },
            debugLog()
        );
    }

    /**
     * Check if this actor is touching an object with the given classname (this won't help out your plan)
     * @param classname The name of the object type to check for
     */
    public final boolean isTouching(String classname) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();

        nextAction(()->
            {
                List<Actor> objects = getObjectsAtOffset(0, 0, null);

                for (Actor a : objects) {
                    if (a.getClass().getName().equals(classname)){
                        result.complete(true);
                        return;
                    }
                }
                result.complete(false);
            },
            debugLog()
        );

        try {
            return result.get();
        }
        catch (InterruptedException | ExecutionException e) {
        }
        return false;
    }

    /**
     * Remove one object that the zombie is touching (this may look useful, but it really won't work in your plan)
     * @param classname the name of the type of object to remove
     */
    public final void removeTouching(String classname) {        
        List<Actor> objects = getObjectsAtOffset(0, 0, null);

        for (Actor a : objects) {
            if (a.getClass().getName().equals(classname)){
                getWorld().removeObject(a);
                return;
            }
        }
    }

    /**
     * Check if this Zombie is carrying a brain.
     * <p>
     * Example:
     * <pre>
     * {@code
     *    if (haveBrains()) // If the zombie is carrying at least one brain
     *     {
     *         putBrain();   // put one down
     *     }
     * }
     * </pre> 
     * </p>
     */
    public final boolean haveBrains()
    {
        CompletableFuture<Boolean> result = new CompletableFuture<>();

        nextAction(()->
            {
                result.complete(numBrains > 0);
            },
            debugLog()
        );

        try {
            return result.get();
        }
        catch (InterruptedException | ExecutionException e) {
        }
        return false;
    }

    /**
     * Check if there is a brain where the zombie is standing.
     * <p>
     * Example:
     * <pre>
     * {@code
     *    if (isBrainHere()) // If the zombie is standing on a brain
     *     {
     *         takeBrain();   // pick it up
     *     }
     * }
     * </pre>
     * </p>
     */
    public final boolean isBrainHere() {
        String db = debugLog();
        if (db != null) {
            System.out.println(db);
        }
        
        return isTouching("Brain");
    }

    /**
     * Check if there is a wall or the edge of the world in front of the zombie.
     * <p>
     * Example:
     * <pre>
     * {@code
     *    if (isFrontClear()) // If the zombie is not facing a wall
     *     {
     *         move();         // take a step forward
     *     }
     * }
     * </pre> 
     * </p>
     */
    public final boolean isFrontClear() {
        CompletableFuture<Boolean> result = new CompletableFuture<>();

        nextAction(()->
            {

                result.complete(checkFront("Wall", 1) == null &&
                                checkFront(null, 1) != this);
            },
            debugLog()
        );

        try {
            return result.get();
        }
        catch (InterruptedException | ExecutionException e) {
        }
        return false;
    }

    /**
     * Die, for reals this time.
     */
    public final void die()
    {
        if (undead && !hasWon()) {
            drawRed();
            undead = false;
            sounds[ZOMBIE_SCREAM].play();
        }
    }

    /**
     * Die, for reals this time.
     */
    public final void die(boolean fast)
    {
        if (!fast) {        
            die();
        }
        else {
            undead = false;
            thinker.interrupt();

            World w = getWorld();
            if (w != null) {
                getWorld().removeObject(this);
            }
        }    
    }

    /**
     * Play a sound
     */
    public void playSound(int index) 
    {
        sounds[index].play();
    }

    /**
     * Check if this zombie is dead, or just undead
     */
    public final boolean isDead()
    {
        return undead == false;
    }

    /**
     * Tell this Zombie it has reached its goal in afterlife!
     */
    public final void win()
    {
        String db = debugLog();
        if (db != null) {
            System.out.println(db);
        }
        
        if (!won && undead) {
            won = true;
            sounds[ZOMBIE_GRUNT].play();
        }
    }

    /**
     * Check if this zombie has accomplished everything it could hope for.
     * <p>
     * Example:
     * <pre>
     * {@code
     *    if (!hasWon()) // If the zombie hasn't reached the goal
     *     {
     *         move();    // step towards it
     *     }
     * }
     * </pre> 
     * </p>
     */
    public boolean hasWon()
    {
        return won;
    }

    /**
     * Give this zombie a red background to indicate an error.
     */
    public void drawRed()
    {
        redBox = true;
    }

    /**
     * Show the next animation frame based on the direction the zombie is facing.
     */
    public void showAnimationFrame()
    {
        int dir = getRotation() / 90;

        GreenfootImage img = null;

        if (stillTrying()) {
            // If the zombie is carrying brains, add the number to the current frame.
            if (numBrains > 0) {
                GreenfootImage brainsLabel = new GreenfootImage("" + numBrains, 14, Color.WHITE, Color.BLACK);
                BufferedImage blImg = brainsLabel.getAwtImage();
                GreenfootImage frm = new GreenfootImage(images[dir][frame]);
                BufferedImage frmImg = frm.getAwtImage();

                img = new GreenfootImage(frm.getWidth(), frm.getHeight());
                BufferedImage bi = img.getAwtImage();
                Graphics2D graphics = (Graphics2D)bi.getGraphics();
                graphics.drawImage(frmImg, null, 0, 0);
                graphics.rotate(Math.toRadians(-getRotation()), frm.getWidth() / 2, frm.getHeight() / 2);
                graphics.drawImage(blImg, null, 0, 0);
                graphics.rotate(Math.toRadians(getRotation()), frm.getWidth() / 2, frm.getHeight() / 2);
            }
            else {
                img = images[dir][frame];
            }
        }
        else if (won) {
            setRotation(90);
            img = images[1][1];
        }
        else /* if (!undead) */ {
            setRotation(0);
            if (deadFrame == 0) {
                deadFrame = dir;
            }
            img = images[4][deadFrame];
        }

        if (redBox) {
            GreenfootImage sprite = img;
            img = new GreenfootImage(sprite.getWidth(), sprite.getHeight());
            img.setColor(Color.RED);
            img.fillRect(1, 1, img.getWidth() - 2, img.getHeight() - 2);
            img.drawImage(sprite, 0, 0);
        }

        setImage(img);
    }

    /**
     * Handle a wall in front of the zombie.  Everything ends if we crash into a wall.
     */
    private boolean handleWall()
    {
        if (checkFront("Wall", 1) != null || checkFront(null, 1) == this) {
            drawRed();
            die();
            ((ZombieLand)getWorld()).finish("Zombie hit wall.", false);
            return false;
        }
        return true;
    }

    /**
     * Handle a bucket in front of the zombie.  Running into a bucket tries to push it.  If it can't be pushed,
     * everything ends.
     */
    private boolean handleBucket()
    {
        Actor bucket = checkFront("Bucket", 1);
        if (bucket != null)
        {
            if (tryPush(bucket, getRotation()) == false) {
                drawRed();
                ((ZombieLand)getWorld()).finish("Bucket no move.", false);
                return false;
            }
        }
        return true;
    }

    /**
     * Attempt to push an object in a given direction
     */
    private boolean tryPush(Actor item, int dir)
    {
        dir = dir / 90;
        int dx = 0;
        int dy = 0;

        if (dir == 0) {
            dx = 1;
        }
        else if (dir == 1) {
            dy = 1;
        }
        else if (dir == 2) {
            dx = -1;
        }
        else {
            dy = -1;
        }

        if (checkFront("Wall", 2) == null && checkFront(null, 2) != this) {
            item.setLocation(item.getX()+dx, item.getY()+dy);
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Check for an object of a particular class in front of the zombie or if that distance is beyond the
     * edge of the world
     *
     * @param classname The class to check for.  If null, look for the edge of the world
     * @param distance The distance (in cells) to the front to look for the object/edge
     * @return The object at the distance, or a reference to this zombie if the front is off the edge of
     *         the world, null if no object of the given class is at that distance or the world does not
     *         end within that distance.
     */
    private Actor checkFront(String classname, int distance)
    {
        int dir = getRotation() / 90;
        int dx = 0;
        int dy = 0;

        if (dir == 0) {
            dx = 1;
        }
        else if (dir == 1) {
            dy = 1;
        }
        else if (dir == 2) {
            dx = -1;
        }
        else {
            dy = -1;
        }

dx *= distance;
dy *= distance;

        if (classname != null) {
            List<Actor> objects = getObjectsAtOffset(dx, dy, null);

            for (Actor a : objects) {
                if (a.getClass().getName().equals(classname)){
                    return a;
                }
            }

            return null;
        }
        else {
            int nextX = getX() + dx;
            int nextY = getY() + dy;
            if ((nextX >= 0 && nextX < getWorld().getWidth()) &&
            (nextY >= 0 && nextY < getWorld().getHeight())) {
                return getOneObjectAtOffset(dx, dy, null);
            }
            else {
                return this;
            }
        }
    }

    /**
     * Execute some action on the next act cycle. This won't help your plan.
     */
    protected final void nextAction(Runnable action, String debug) {
        try {
            CompletableFuture<Object> cf = new CompletableFuture<>();
            
            actions.put(()->
                {
                    if (debug != null) {
                        System.out.println(debug);
                    }
                    action.run();
                    cf.complete(null);
                }
            );
            
            cf.get();
        }
        catch (InterruptedException | ExecutionException e) {
        }        
    }

    /**
     * Load the zombie walking images for faster shambling animations.
     */
    private static GreenfootImage[][] preloadImages()
    {
        GreenfootImage[][] images = new GreenfootImage[5][];

        images[0] = loadImages("zombie-right");
        images[1] = loadImages("zombie-down");
        images[2] = loadImages("zombie-left");
        images[3] = loadImages("zombie-up");

        images[4] = loadImages("zombie-dead");

        return images;
    }

    /**
     * Create and fill an array of Greenfoot images by loading the files with
     * a given name followed by frame numbers
     */
    private static GreenfootImage[] loadImages(String name)
    {
        GreenfootImage[] imageArr = new GreenfootImage[NUM_FRAMES];

        for (int i = 0; i < NUM_FRAMES; i++) {
            imageArr[i] =  new GreenfootImage(name + "-" + i + ".png");
        }

        return imageArr;
    }

    /**
     * Load the zombie sounds for faster playback.
     */
    private static GreenfootSound[] preloadSounds()
    {
        GreenfootSound[] sounds;

        sounds = new GreenfootSound[NUM_SOUNDS];
        sounds[ZOMBIE_GROAN] = new GreenfootSound("ZombieGroan.wav");
        sounds[ZOMBIE_MED_GROAN] = new GreenfootSound("ZombieMedGroan.wav");
        sounds[ZOMBIE_LONG_GROAN] = new GreenfootSound("ZombieLongGroan.wav");
        sounds[ZOMBIE_LOUD_GROAN] = new GreenfootSound("ZombieLoudGroan.wav");
        sounds[ZOMBIE_GRUNT] = new GreenfootSound("ZombieGrunt.wav");
        sounds[ZOMBIE_SCREAM] = new GreenfootSound("ZombieScream.wav");

        return sounds;
    }

    /**
     * Make the poor zombie write out a log of every action it takes.
     */
    public void debug() 
    {
        System.out.print("\u000c");
        debugMode = true;
    }

    /**
     * Retrieve a line of debugging information
     */
    public String debugLog()
    {
        String s = null;

        if (debugMode) {
            StackTraceElement[] trace = (new Throwable()).getStackTrace();

            int methodPos = 1;
            int callerPos = 2;

            StackTraceElement method = trace[methodPos];
            StackTraceElement caller = trace[callerPos];

            if (!method.getClassName().equals(caller.getClassName()) &&
            caller.getClassName().equals("MyZombie")) {
                s = "(" + caller.getFileName() + ":" + caller.getLineNumber() + ")" +
                caller.getClassName() + "." + caller.getMethodName() +"()" +
                " " + method.getMethodName();
            }
        }

        return s;
    }    
}
