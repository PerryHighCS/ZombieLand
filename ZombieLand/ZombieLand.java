import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.List;

import java.io.File;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.net.URL;
import java.net.URLClassLoader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.ArrayList;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

/**
 * Write a description of class MyWorld here.
 * 
 * @author bdahlem
 * @version 1.0
 */
public class ZombieLand extends World
{
    Actor message = null;
    private boolean done = false;
    private List<GoalObject> goal;
    private boolean checked = false;

    /**
     * Load the world description file and initialize the world
     */    
    public ZombieLand()
    {  
    
        // Create a temporary world;
        super(1,1,64);
    
        try {
            // Create a Classloader to load the actors for the world
            URL url = (new File(".")).toURL();
            URL[] urls = new URL[]{url};
            ClassLoader cl = new URLClassLoader(urls, this.getClass().getClassLoader());
        
            // Open and parse the world description XML File
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(this.getClass().getName() + ".xml"));
            doc.getDocumentElement().normalize();
        
            // Get a handle to the root of the world description
            Element document = doc.getDocumentElement();
            Element root = (Element)document.getElementsByTagName("world").item(0);
        
            ZombieLand firstWorld = loadWorld(root, cl);
            Greenfoot.setWorld(firstWorld);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
   
    /**
     * Create a ZombieLand with a given size
     */
    protected ZombieLand(int width, int height)
    {
        super(width, height, 64);
    }

    /**
     * Create a world based on an XML DOM description of the world and its actors
     * @param root The DOM element representing the root of the world
     * @param cl A classloader that can load the actor classes into the java runtime
     * @return the ZombieLand described by the XML DOM element and its children
     */
    private ZombieLand loadWorld(Element root, ClassLoader cl) 
    throws ClassNotFoundException, NoSuchMethodException, InstantiationException, 
    IllegalAccessException, 
    InvocationTargetException
    {
        // Set the world width and height
        int width = Integer.parseInt(root.getAttribute("width"));
        int height = Integer.parseInt(root.getAttribute("height"));
        ZombieLand realWorld = new ZombieLand(width, height);

        // Get handles to the initial and objective description nodes
        Node initial = root.getElementsByTagName("initial").item(0);
        Node objective = root.getElementsByTagName("objective").item(0);

        // Load and place initial objects
        NodeList initialObjects = ((Element)initial).getElementsByTagName("object");
        for (int i = 0; i < initialObjects.getLength(); i++) {
            // Get the object description
            Element obj = (Element)initialObjects.item(i);

            // Determine the classname that describes the object
            String className = obj.getAttribute("classname");

            // Make sure the class is loaded into the Java runtime
            Class objClass = cl.loadClass(className);

            // Determine where the class instances are located
            NodeList locations = obj.getElementsByTagName("location");
            for (int j = 0; j < locations.getLength(); j++) {
                Element pos = (Element)locations.item(j);

                // Find out the coordinates of the location
                int x = Integer.parseInt(pos.getAttribute("x"));
                int y = Integer.parseInt(pos.getAttribute("y"));

                int dir = 0;
                if (pos.hasAttribute("dir")) {
                    dir = Integer.parseInt(pos.getAttribute("dir"));
                }
                
                // Find out how many instances are present in this location
                int count = 1;                
                if (pos.hasAttribute("count")) {
                    count = Integer.parseInt(pos.getAttribute("count"));
                }

                NodeList callList = pos.getElementsByTagName("call");
                List<String[]> calls = null;
                if (callList.getLength() > 0) {
                    calls = new ArrayList<>();

                    for (int k = 0; k < callList.getLength(); k++) {
                        Element method = ((Element)callList.item(k));
                        String[] callSignature = new String[2];
                        callSignature[0] = method.getAttribute("name");
                        callSignature[1] = method.getAttribute("value");

                        calls.add(callSignature);
                    }
                }
                
                if (className.equals("Brain")) {
                    // Create instances at this location
                    Constructor constructor = objClass.getConstructor(int.class);
                    
                    Actor a = (Actor)constructor.newInstance(count);
                    realWorld.addObject(a, x, y);
                    a.setRotation(dir);
                    
                    if (calls != null) {
                        for (String[] call : calls) {
                            Class[] params = {int.class};
                            Method m = objClass.getMethod(call[0], params);
                            m.invoke(a, Integer.parseInt(call[1]));
                        }
                    }
                }
                else if (className.equals("MyZombie")) {
                    // Determine how many brains this zombie is carrying
                    int numBrains = 0;
                    if (pos.hasAttribute("brains")) {
                        numBrains = Integer.parseInt(pos.getAttribute("brains"));
                    }
                    
                    // Create instances at this location
                    Constructor constructor = objClass.getConstructor();
                    for (; count > 0; count--) {
                        Zombie z = (Zombie)constructor.newInstance();
                        
                        // Give the zombie some brains
                        if (numBrains > 0) {
                            try {
                                Field nb = Zombie.class.getDeclaredField("numBrains");
                                nb.setAccessible(true);
                                nb.set(z, numBrains);
                                nb.setAccessible(false);
                            }
                            catch (Exception e) {}
                        }
                        
                        // Add the zombie and face it the correct direction
                        realWorld.addObject(z, x, y);
                        z.setRotation(dir);
                        z.showAnimationFrame();
                        
                        // Make any setup calls
                        if (calls != null) {
                            for (String[] call : calls) {
                                Class[] params = {int.class};
                                Method m = objClass.getMethod(call[0], params);
                                m.invoke(z, Integer.parseInt(call[1]));
                            }
                        }
                    }
                }
                else {
                    // Create instances at this location
                    Constructor constructor = objClass.getConstructor();
                    for (; count > 0; count--) {
                        Actor a = (Actor)constructor.newInstance();
                        realWorld.addObject(a, x, y);
                        a.setRotation(dir);
    
                        if (calls != null) {
                            for (String[] call : calls) {
                                Class[] params = {int.class};
                                Method m = objClass.getMethod(call[0], params);
                                m.invoke(a, Integer.parseInt(call[1]));
                            }
                        }
                    }
                }
            }
        }

        // Load the solution for this world
        NodeList goalObjects = ((Element)objective).getElementsByTagName("object");
        realWorld.goal = loadGoals(goalObjects);

        // Determine the order to paint the actor classes
        Element paintOrderEl = ((Element)root.getElementsByTagName("paintOrder").item(0));

        if (paintOrderEl != null) {
            NodeList paintOrder = paintOrderEl.getElementsByTagName("class");

            Class[] classes = new Class[paintOrder.getLength()];
            for (int i = 0; i < classes.length; i++) {
                String className = ((Element)paintOrder.item(i)).getAttribute("name");
                classes[i] = Class.forName(className);
            }
            realWorld.setPaintOrder(classes);
        }

        // Return the loaded world
        return realWorld;
    }

    /**
     * Load the intended solution for this world.
     * 
     * @param goalNodes a list of XML DOM Elements describing the solution state of the actors in the world
     * @return a list of objects that can be compared to the actors in the world to determine if the
     *         solution was reached
     */
    private List<GoalObject> loadGoals(NodeList goalNodes)
    {
        List<GoalObject> goalList = new ArrayList<>();

        // Create a list of goal objects based on all of the nodes in the solution tree
        for (int i = 0; i < goalNodes.getLength(); i++) {
            // Determine what type of object to look for
            Element gEl = (Element)goalNodes.item(i);
            String classname = gEl.getAttribute("classname");

            // Determine all of the instances of the goal object
            NodeList locations = gEl.getElementsByTagName("location");
            for (int j = 0; j < locations.getLength(); j++) {
                Element pos = (Element)locations.item(j);

                // Create a goal object representing the instance at a particular location
                GoalObject gObj = new GoalObject();
                gObj.name = classname;                    
                gObj.x = Integer.parseInt(pos.getAttribute("x"));
                gObj.y = Integer.parseInt(pos.getAttribute("y"));
                
                // Determine how many instances should be at the location
                if (pos.hasAttribute("count")) {
                    gObj.count = Integer.parseInt(pos.getAttribute("count"));
                }
                
                // Determine what direction the instance(s) should be facing
                if (pos.hasAttribute("dir")) {
                    gObj.dir = Integer.parseInt(pos.getAttribute("dir"));
                }
                
                // Determine if any method calls need to be made to determine if goal has been reached
                NodeList callList = pos.getElementsByTagName("call");
                if (callList.getLength() > 0) {
                    gObj.calls = new ArrayList<String[]>();

                    for (int k = 0; k < callList.getLength(); k++) {
                        Element method = ((Element)callList.item(k));
                        String[] callSignature = new String[2];
                        callSignature[0] = method.getAttribute("name");  // method call name
                        callSignature[1] = method.getAttribute("value"); // expected return value

                        gObj.calls.add(callSignature);
                    }
                }    

                goalList.add(gObj);
            }
        }

        return goalList;
    }

    /**
     * Check the status of the Zombies every frame
     */
    public void act()
    {
        // Make sure the MyZombie plan does not include illegal features
        if (!checked) {
            checkForAssignment();
            checked = true;
        }
        
        // If the goal hasn't yet been reached and the zombies are still trying
        if (!done) {
            synchronized (Zombie.class) {

                // Make sure that the zombies are still trying
                if (checkZombies() ) {
                    // And check if the goal has now been reached
                    if (checkGoal()) {
                    }
                }                    
            }
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

        // Create an actor to display the message
        message = new Actor(){public void act(){}};

        int xOffset = 0;
        int yOffset = 0;

        if (getWidth() % 2 == 0) {
            xOffset = getCellSize() / 2;
        }
        if (getHeight() % 2 == 0) {
            yOffset = getCellSize() / 2;
        }

        // Create a temporary image to measure the text
        GreenfootImage img = new GreenfootImage(1,1);

        // Set up the font for the message
        java.awt.Font f = new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.BOLD, 30);
        java.awt.Image image = img.getAwtImage();
        java.awt.Graphics g = img.getAwtImage().createGraphics();
        g.setFont(f);
        java.awt.FontMetrics fm = g.getFontMetrics(f);

        // Determine the size of the text
        int textWidth = fm.stringWidth(msg);
        int textHeight = fm.getHeight() + fm.getMaxDescent();
        int textBottom = textHeight - fm.getMaxDescent();

        // Create an image that will fit the actual dimensions of the text (and center it)
        img = new GreenfootImage((textWidth  + xOffset)* 2, textHeight + yOffset * 2);
        g = img.getAwtImage().createGraphics();
        g.setColor(java.awt.Color.BLACK);
        g.setFont(f); 

        int x = textWidth / 2 ;
        int y = textBottom;

        // Draw the text onto the image, offsetting in all directions to create an outline
        g.drawString(msg, x-1, y-1);
        g.drawString(msg, x, y-1);
        g.drawString(msg, x+1, y-1);
        g.drawString(msg, x-1, y);
        g.drawString(msg, x+1, y);
        g.drawString(msg, x-1, y+1);
        g.drawString(msg, x, y+1);
        g.drawString(msg, x+1, y+1);

        // Draw the text in white above the outline
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
     * End the world if there aren't any zombies left.
     */
    public boolean checkZombies()
    {   
        // if the scenario hasn't already finished
        if (!done) {
            List<Zombie> zombies = getObjects(Zombie.class);

            // Check if there are any zombies left in the world
            if (zombies.size() == 0) {
                finish("Zombie no more.", false);
                return false;
            }
            else {
                boolean allDead = true;
                boolean allDone = true;
                
                // If there are still zombies, determine if they are all dead or if any are still trying
                for (Zombie z : zombies) {
                    if (!z.isDead()) {
                        allDead = false;    // Someone is still undead
                    }
                    if (z.stillTrying()) {
                        allDone = false;    // Someone is still struggling
                    }
                }

                if (allDead) {
                    finish("Zombie dead.", false);
                    return false;
                }
                
                done = allDone; // The scenario is over if all of the zombies are
            }
        }
        return true;
    }

    /**
     * Determine if the goal has been reached
     */
    public boolean checkGoal()
    {
        try {
            // Create a Classloader to load the actors for the world
            URL url = (new File(".")).toURL();
            URL[] urls = new URL[]{url};
            ClassLoader cl = new URLClassLoader(urls, this.getClass().getClassLoader());
            
            List<Actor> actors = getObjects(null);
            List<GoalObject> state = new ArrayList<GoalObject>();
            synchronized (Zombie.class) {
                // Look at all of the actors in the world
                for (Actor a : actors) {
                    // If the actor is an anonymous class, skip it
                    if (a.getClass().getName().contains("$")) {
                        continue;
                    }
                    
                    // Create a goal object describing this actor
                    GoalObject gObj = new GoalObject();
                    gObj.a = a;
                    gObj.name = a.getClass().getName();
    
                    gObj.x = a.getX();
                    gObj.y = a.getY();
                    gObj.dir = a.getRotation();
                    gObj.count = 1;
                    
                    // Handle counting brains on the pile using reflection
                    if (gObj.name.equals("Brain")) {
                        try {
                            Class objClass = cl.loadClass("Brain");
                            
                            Method m = objClass.getMethod("getNum", null);
                            Integer rval = (Integer)m.invoke(a, null);
                            gObj.count = rval;
                        }
                        catch (Exception e) {}
                    }
    
                    boolean duplicate = false;

                    /*
                    for (int i = 0; i < state.size(); i++) {
                        GoalObject o = state.get(i);

                        if (o.name.equals(gObj.name) &&
                            o.x == gObj.x &&
                            o.y == gObj.y) {
                            duplicate = true;
                            o.count = o.count + 1;
                            break;
                        }
                    }
                    */
              
                    // Add all objects to the current state
                    if (!duplicate) {
                        state.add(gObj);
                    }
                }
    
                // If the state is the same as the goal condition, the scenario succeeded
                if (goal != null && state.size() == goal.size()) {
                    if (state.containsAll(goal)) {
                        finish("Zombie do good.", true);
                        return true;
                    }
                }
                
                // If the goal hasn't been reached
                if (goal != null) {
                    //Check all of the zombies
                    for (GoalObject o : state) {
                        if (o.a instanceof Zombie) {
                            Zombie z = (Zombie)o.a;
                            
                            // If the zombie has not reached its goal, and is no longer trying to, mark it as red
                            if (!z.stillTrying() && !goal.contains(o)) {                                
                                z.drawRed();
                            }
                        }
                    }
                }
                
                // If the scenario is finished, but the goal hasn't been reached
                if (goal != null && done) {
                    // Check all the actors that aren't zombies
                    for (GoalObject o : state) {
                        // If the actor isn't supposed to be there, mark it yellow.
                        if (!(o.a instanceof Zombie) && !goal.contains(o)) {
                            Actor a = o.a;
                            GreenfootImage ai = a.getImage();
                            
                            GreenfootImage img = new GreenfootImage(getCellSize(), getCellSize());
                            img.setColor(new Color(255, 255, 0, 96));
                            img.fillRect(1, 1, getCellSize() - 2, getCellSize() - 2);
                            
                            int x = (getCellSize() - ai.getWidth()) / 2;
                            int y = (getCellSize() - ai.getHeight()) / 2;
                            img.drawImage(ai, x, y);
                            
                            a.setImage(img);
                        }
                    }
                    
                    // Check all the objects in the goal condition
                    for (GoalObject o : goal) {
                        // If the actor isn't present, draw an empty yellow box
                        if (!(o.name.equals("MyZombie")) && !state.contains(o)) {
                            Actor a = new Actor(){public void act(){}};
                            
                            GreenfootImage img = new GreenfootImage(getCellSize(), getCellSize());
                            img.setColor(new Color(255, 255, 0, 96));
                            img.fillRect(1, 1, getCellSize() - 2, getCellSize() - 2);
                            
                            a.setImage(img);
                            addObject(a, o.x, o.y);
                        }
                    }
                }
            }
        }
        catch (Exception e) {}
        
        return false;
    }
    
    /**
     * Look at the source code for MyZombie.java, ensuring there are no assignment statements.
     */
    private void checkForAssignment() {
        try {
            String sourceCode = readFile("MyZombie.java", StandardCharsets.UTF_8);
            
            // Strip out comments
            sourceCode = sourceCode.replaceAll("//(?:.|)*?[\\n\\r]", "")        // singleline comments
                                   .replaceAll("/\\*(?:.|[\\n\\r])*?\\*/", ""); // multiline comments
            
            // If there is an =
            if (sourceCode.matches("[\\S\\s]*(?<![=!])=(?!=)[\\S\\s]*")) {
                // Quit because there's an assignment statement
                finish("Zombie not know =", false);
            }
            else if (sourceCode.matches("[\\S\\s]*\\d[\\S\\s]*")) {
                // Quit because there's a scary number
                finish("Not know numbers", false);
            }
            else if (sourceCode.contains("<")) {
                // Quit because there's a comparison statement
                finish("Zombie not know <", false);
            }
            else if (sourceCode.contains(">")) {
                // Quit because there's a comparison statement
                finish("Zombie not know >", false);
            }
            else if (sourceCode.contains("+")) {
                // Quit because there's a math expression
                finish("Zombie not know +", false);
            }
            else if (sourceCode.contains("-")) {
                // Quit because there's a math expression
                finish("Zombie not know -", false);
            }
        }
        catch (IOException e) {
        }
    }

    static String readFile(String path, Charset encoding) 
      throws IOException 
    {
      byte[] encoded = Files.readAllBytes(Paths.get(path));
      return new String(encoded, encoding);
    }
    
    private class GoalObject {
        public String name;
        public int count = 1;
        public int dir = Integer.MIN_VALUE;
        public int x;
        public int y;
        public List<String[]> calls;
        public Actor a;
        
        public boolean equals(Object o) {
            if (o instanceof GoalObject) {
                GoalObject other = (GoalObject)o;

                boolean calls = true;
                
                GoalObject goal;
                GoalObject cur;
                
                if (this.a != null) {
                    goal = other;
                    cur = this;
                }
                else {
                    goal = this;
                    cur = other;
                }

                if (this.name.equals(other.name)) {
                    if (goal.calls != null && cur.a != null) {
                        calls = checkCalls(goal, cur);
                    }
                }
                
                if (goal.dir != Integer.MIN_VALUE) {
                    if (goal.dir != cur.dir) {
                        calls = false;
                    }
                }
                
                return this.name.equals(other.name) &&
                    this.x == other.x &&
                    this.y == other.y &&
                    this.count == other.count &&
                    calls == true;
            }
            return false;
        }
        
        
        private boolean checkCalls(GoalObject goal, GoalObject cur) {
            for (String[] methodCall : goal.calls) {
                String methodName = methodCall[0];
                try {
                    Class c = Class.forName(this.name);
                    Method m = c.getMethod(methodName, null);

                    String rval = m.invoke(cur.a, null).toString();

                    if (!rval.equals(methodCall[1])){
                        return false;
                    }
                }
                catch (Exception e) {
                    //e.printStackTrace();
                }
            }
            
            return true;
        }
    }
    
}