import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.Random;

import javax.swing.JFrame;

/**  LOG: 
 	 0) The screen is drawing a blank. You may have to set each pixel to be black first or something. [FIXED BY METHOD 2 in renderNewField()]
	 1) Check why the code is running inefficiently. The FPS rate is close to tick speed. (UPDATE: Improved by moving renderNewField() from render to tick method.)
	 2) Add MouseListener [DONE]
	 	a) Use Mouse Adapter Since you will only need the initial click and hold click to add light bullets [DONE]
	 	b) See if this can be done using an anonymous class. [DONE]
	 3) Debug/Test [DONE]
	 4) Clean Up Methods in Class Bullet [DONE]
	 4.1) Clean Up DeleteMode [DONE]
	 5) Add directionality control to the bullets [DONE]
	 6) Bouncing Gravity Motion [DONE]
	 7) Circular Motion around the Center of the Window [DONE]
	 8) Allow a decrease/increase function to either decrease/increase the length of the trail [DONE]
	 9) Build Dialogue Header for Motions [DONE]
	 10) Build Help Box  [DONE]
	 11) Show to friends [DONE]
**/
public class StringLights extends Canvas implements Runnable{

	private static final long serialVersionUID = 1L;

	private class Bullet{
		private double x, y;					// Location on window
		private int lifespan = 7200;			// Bullet will remain on screen for a maximum of 600 frames
		private int bulletColor;				// The ARGB int value for color
		private byte bulletDirection;			// There are 8 directions the bullet can move in random/normal/circular/tech motion. 0 = N, 1 = NE, 2 = E... 7 = NW. This is a clockwise movement.
		private byte motionToTake;				// An byte that determines what motion pattern the bullet will take. 0 = Random/normal/circular/tech, 1 = orbital, 2 = gravitational.
		
		private int techCounter = 0;			// Counter for next Tech Motion
		
		private double theta;					// The angle from the center of the window; used for Orbital Motion
		private double radius;					// The distance from the center of the window; used for Orbital Motion
		
		private double x_vel;					// The x coordinate velocity of the bullet. This will be a value between 0 and 2 exclusively.
		private double y_vel;					// The y coordinate velocity of the bullet.
		
		public Bullet(int x, int y){
			this.x = x;
			this.y = y;
			bulletDirection = (byte) r.nextInt(8);
			
			// Adding the bullet with the correct motionToTake
			motionToTake = 0x0;
			if(keyPressed == KeyEvent.VK_O)
				prepareOrbitalMotion();
			if(keyPressed == KeyEvent.VK_G)
				prepareGravitationalMotion();
			
			// Determining Random Color
			int red = r.nextInt(255) << 16;
			int green = r.nextInt(255) << 8;
			int blue = r.nextInt(255);
			bulletColor = red + green + blue;
		}
		
		public void tick(){
			// If the bullet lifespan reaches zero, delete it.
			if(lifespan <= 0){
				bullets.remove(this);
			}
			
			determineMotion();		// Deciding what motion to take.
			
			// Update Movements
			if(motionToTake == 0x0){
				updateX();
				updateY();
			}
			else if(motionToTake == 0x1){
				updateRotation();
			}
			else if(motionToTake == 0x2){
				updateGravitationalMotion();
			}

			// Decrease Lifespan
			lifespan--;
		}
		
		private void determineMotion(){
			// Random Motion
			if(keyPressed == KeyEvent.VK_R){
				bulletDirection += (r.nextInt(3)-1);
				bulletDirection = (byte) ((bulletDirection+8)%8);
				motionToTake = 0x0;
			}
			
			// Circle Motion
			else if(keyPressed == KeyEvent.VK_C){
				bulletDirection = (byte) ((++bulletDirection)%8);
				motionToTake = 0x0;
			}
			
			// Tech Motion
			else if(keyPressed == KeyEvent.VK_T){
				// If the counter reaches zero, determine to turn left, turn left twice, turn right twice, or turn right.
				if(techCounter-- <= 0){
					int nextTechTurn = r.nextInt(4);
					if(nextTechTurn == 0)	  { counterClockwiseTurn();}
					else if(nextTechTurn == 1){ counterClockwiseTurn(); counterClockwiseTurn();}
					else if(nextTechTurn == 2){ clockwiseTurn(); clockwiseTurn();}
					else					  { clockwiseTurn();}
					
					techCounter = r.nextInt(60) + 15;
				}
				motionToTake = 0x0;
			}
			
			// Orbital Motion
			else if(keyPressed == KeyEvent.VK_O){
				motionToTake = 0x1;
			}
			
			// Gravitational Motion
			else if(keyPressed == KeyEvent.VK_G){
				motionToTake = 0x2;
			}
			
			// Straight Motion
			else if(keyPressed == KeyEvent.VK_S){
				motionToTake = 0x0;
			}
			
			// Uniform Motion Going Up
			else if(keyPressed == KeyEvent.VK_UP){
				bulletDirection = 0x0;
				motionToTake = 0x0;
			}
			
			// Uniform Motion Going Right
			else if(keyPressed == KeyEvent.VK_RIGHT){
				bulletDirection = 2;
				motionToTake = 0x0;
			}
			
			// Uniform Motion Going Down
			else if(keyPressed == KeyEvent.VK_DOWN){
				bulletDirection = 4;
				motionToTake = 0x0;
			}
			
			// Uniform Motion Going Left
			else if(keyPressed == KeyEvent.VK_LEFT){
				bulletDirection = 6;
				motionToTake = 0x0;
			}
		}
		
		// X-coordinate update for circular, straight, tech, and random motion.
		private void updateX(){
			if(bulletDirection >= 1 && bulletDirection <= 3)
				x = (x + (2)) % current.getWidth();
			if(bulletDirection >=5 && bulletDirection <= 7){
				x = (x + current.getWidth() - (2)) % current.getWidth();
			}
		}
		
		// Y-coordinate update for circular, straight, tech, and random motion.
		private void updateY(){
			if(bulletDirection == 7 || bulletDirection <= 1)
				y = (y + current.getHeight() - (2)) % current.getHeight();
			if(bulletDirection >=3 && bulletDirection <= 5){
				y = (y + (2)) % current.getHeight();
			}
		}

		// Coordinate update for orbital motion
		private void updateRotation(){
			theta += Math.PI / 270f;
			if(theta >= Math.PI * 2)			// If theta goes over 2*Pi, subtract a revolution (2*Pi)
				theta -= Math.PI * 2;
			
			x = (radius * Math.cos(theta)) + WIDTH;
			y = (radius * Math.sin(theta)) + HEIGHT;
		}
		
		// Coordinate update for gravitational motion
		private void updateGravitationalMotion(){
			x += x_vel;
			if(x > WIDTH * SCALE)
				x = 0;
			
			y += y_vel;
			if(y > HEIGHT * SCALE){
				y = HEIGHT * SCALE;
				y_vel *= -1;
			}
			
			y_vel += GRAVITY;
		}
		
		public void render(Graphics2D g2d){
			g2d.setColor(new Color(bulletColor));
			g2d.fillOval((int)x, (int)y, 2, 2);
		}
		
		public void clockwiseTurn(){
			bulletDirection = (byte) ((bulletDirection + 1)%8);
		}
		
		public void counterClockwiseTurn(){
			bulletDirection = (byte) ((bulletDirection + 8 - 1)%8);
		}
		
		public void prepareOrbitalMotion(){
			// Determining radius
			radius = Math.hypot(x - WIDTH, y - HEIGHT);
			
			// Determining theta
			theta = Math.acos((x-WIDTH)/radius);
			if(y - HEIGHT < 0 || (y - HEIGHT == 0 && x - WIDTH < 0)){
				theta = (2*Math.PI) - theta;
			}
		}
		
		public void prepareGravitationalMotion(){
			x_vel = (r.nextDouble() * 2);
			y_vel = 0;
		}
	}
	
	public static final int WIDTH = 320;
	public static final int HEIGHT = WIDTH * 9 / 12;	// 4:3 frame ratio
	public static final int SCALE = 2;
	public static final double GRAVITY = .05;
	public static final String TITLE = "String of Lights";
	public Random r;
	
	private BufferedImage current;
	private BufferedImage commands;
	private BufferedImage open;
	private BufferedImage close;
	private BufferedImage help;
	
	private LinkedList<Bullet> bullets = new LinkedList<Bullet>();
	
	private boolean running = false;
	private boolean held = false;
	private boolean deleteMode = false;
	private boolean helpOpen = false;
	
	private int trailDecrease;
	private int keyPressed;
	private int opacityCounter;
	private int button_x = 300, button_y = 460;
	private int menu_x = 25, menu_y = 20;
	
	private float commandOpacity = 0f;
	private float buttonOpacity = 0f;
	private float menuOpacity = 0f;

	public void start(){
		running = true;
		new Thread(this).start();
	}

	public void stop(){
		running = false;
		System.exit(1);
	}
	
	private void init(){
		current = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_ARGB);
		// Set all pixels to black
		for(int y = 0; y < current.getHeight(); y++){
			for(int x = 0; x < current.getWidth(); x++){
				current.setRGB(x, y, 0xff000000);
			}
		}
		
		commands = new BufferedImage(this.getWidth(), 50, BufferedImage.TYPE_INT_ARGB);
		opacityCounter = 0;
		
		initializeHelpButtons();
		initializeHelpMenu();
		
		r = new Random();
		keyPressed = 'n';
		trailDecrease = 4;
		
		this.addMouseListener(new MouseAdapter(){
			public void mousePressed(MouseEvent e){
				if(buttonSelected(open) && !held)
					helpOpen = !helpOpen;
				else
					held = true;
			}
			public void mouseReleased(MouseEvent e){
				held = false;
			}
			public void mouseExited(MouseEvent e){
				held = false;
			}
		});
		
		this.addKeyListener(new KeyAdapter(){
			public void keyPressed(KeyEvent e){
				int key = e.getKeyCode();
				String command = "";
				if(key == KeyEvent.VK_S){
					System.out.println("STRAIGHT MOTION ACTIVATED");
					command = "STRAIGHT MOTION ACTIVATED";
					keyPressed = key;
				}
				else if(key == KeyEvent.VK_R){
					System.out.println("RANDOM MOTION ACTIVATED");
					command = "RANDOM MOTION ACTIVATED";
					keyPressed = key;
				}
				else if(key == KeyEvent.VK_C){
					System.out.println("CIRCULAR MOTION ACTIVATED");
					command = "CIRCULAR MOTION ACTIVATED";
					keyPressed = key;
				}
				else if(key == KeyEvent.VK_T){
					System.out.println("TECH MOTION ACTIVATED");
					command = "TECH MOTION ACTIVATED";
					keyPressed = key;
				}
				else if(key == KeyEvent.VK_O){
					System.out.println("ORBITAL MOTION ACTIVATED");
					command = "ORBITAL MOTION ACTIVATED";
					for(int i = 0; i < bullets.size(); i++){
						bullets.get(i).prepareOrbitalMotion();
					}
					keyPressed = key;
				}
				else if(key == KeyEvent.VK_G){
					System.out.println("GRAVITATIONAL MOTION ACTIVATED");
					command = "GRAVITATIONAL MOTION ACTIVATED";
					for(int i = 0; i < bullets.size(); i++){
						bullets.get(i).prepareGravitationalMotion();
					}
					keyPressed = key;
				}
				else if(key == KeyEvent.VK_UP){
					System.out.println("UP MOTION ACTIVATED");
					command = "UP MOTION ACTIVATED";
					keyPressed = key;
				}
				else if(key == KeyEvent.VK_RIGHT){
					System.out.println("RIGHT MOTION ACTIVATED");
					command = "RIGHT MOTION ACTIVATED";
					keyPressed = key;
				}
				else if(key == KeyEvent.VK_DOWN){
					System.out.println("DOWN MOTION ACTIVATED");
					command = "DOWN MOTION ACTIVATED";
					keyPressed = key;
				}
				else if(key == KeyEvent.VK_LEFT){
					System.out.println("LEFT MOTION ACTIVATED");
					command = "LEFT MOTION ACTIVATED";
					keyPressed = key;
				}
				else if(key == KeyEvent.VK_N){
					System.out.println("LEFT TURN");
					command = "LEFT TURN";
					for(int i = 0; i < bullets.size(); i++){
						bullets.get(i).counterClockwiseTurn();
					}
				}
				else if(key == KeyEvent.VK_M){
					System.out.println("RIGHT TURN");
					command = "RIGHT TURN";
					for(int i = 0; i < bullets.size(); i++){
						bullets.get(i).clockwiseTurn();
					}
				}
				// Delete bullets 10 at a time
				else if(key == KeyEvent.VK_D){
					command = "DELETE ALL BULLETS";
					deleteMode = true;
				}
				// Decreasing Trail Length
				else if(key == KeyEvent.VK_1){
					trailDecrease = (trailDecrease / 2);
					
					if(trailDecrease <= 0)
						trailDecrease = 1;
					command = "TRAIL LENGTH: " + (int)((256 / trailDecrease) - 1) + " PIXELS";
				}
				// Increasing Trail Length
				else if(key == KeyEvent.VK_2){
					trailDecrease = (trailDecrease * 2);
					
					if(trailDecrease > 256)
						trailDecrease = 256;
					command = "TRAIL LENGTH: " + (int)((256 / trailDecrease) - 1) + " PIXELS";
				}
				else if(key == KeyEvent.VK_Q){
					stop();
				}
				
				// Set Up Commands
				if(command.length() != 0)
					printCommand(command);
			}
			
			public void keyReleased(KeyEvent e){
				if(keyPressed == KeyEvent.VK_UP || keyPressed == KeyEvent.VK_RIGHT || keyPressed == KeyEvent.VK_DOWN || keyPressed == KeyEvent.VK_LEFT)
					keyPressed = KeyEvent.VK_S;
			}
		});
	}
	
	private void initializeHelpButtons(){
		// Setting up the Help Menu Buttons
		open = new BufferedImage(40, 20, BufferedImage.TYPE_INT_ARGB);
		close = new BufferedImage(80, 50, BufferedImage.TYPE_INT_ARGB);
		
		// Getting graphics for the buttons
		Graphics2D button_g = open.createGraphics();
		
		// Creating Design for Open Button
		button_g.setColor(new Color(0,0,0,140));									// Black Background for the button
		button_g.fillRect(0, 0, open.getWidth(), open.getHeight());
		
		button_g.setColor(Color.white);									// White color for details
		button_g.setStroke(new BasicStroke(2));							// A line thickness of 2
		button_g.drawRect(1, 1, open.getWidth() - 2, open.getHeight() - 2);
		
		button_g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,		// AntiAlias - make images smoother
				RenderingHints.VALUE_ANTIALIAS_ON);
		
		button_g.drawLine(10, 9, 20, 4);
		button_g.drawLine(20, 4, 30, 9);
		button_g.drawLine(10, 15, 20, 10);
		button_g.drawLine(20, 10, 30, 15);
		
		
		// Creating Design for Close Button
		button_g = close.createGraphics();
		
		button_g.setColor(new Color(0,0,0,140));									// Black Background for the button
		button_g.fillRect(0, 0, open.getWidth(), open.getHeight());
		
		button_g.setColor(Color.white);									// White color for details
		button_g.setStroke(new BasicStroke(2));							// A line thickness of 2
		button_g.drawRect(1, 1, open.getWidth() - 2, open.getHeight() - 2);
		
		button_g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,		// AntiAlias - make images smoother
				RenderingHints.VALUE_ANTIALIAS_ON);
		
		button_g.drawLine(10, 4, 20, 9);
		button_g.drawLine(20, 9, 30, 4);
		button_g.drawLine(10, 10, 20, 15);
		button_g.drawLine(20, 15, 30, 10);
		
		// Disposing of the Graphics
		button_g.dispose();
	}
	
	private void initializeHelpMenu(){
		help = new BufferedImage(600, 400, BufferedImage.TYPE_INT_ARGB);
		Font header = new Font("Haettenschweiler", Font.BOLD, 25);
		Font instruction = new Font("Haettenschweiler", Font.PLAIN, 20);
		
		// Get graphics
		Graphics2D help_g = help.createGraphics();
		
		// Set Background to an translucent black
		help_g.setColor(new Color(0,0,0,180));
		help_g.fillRect(0, 0, help.getWidth(), help.getHeight());
		
		// Setting Up the Border
		help_g.setColor(Color.white);
		help_g.setStroke(new BasicStroke(5));
		help_g.drawRect(2, 2, help.getWidth() - 5, help.getHeight() - 5);
		
		help_g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,		// AntiAlias - make images smoother
				RenderingHints.VALUE_ANTIALIAS_ON);
		
		// Instruction Headers
		help_g.setFont(header);
		help_g.drawString("INSTRUCTION:", 20, 40);
		help_g.drawString("MOVEMENT KEY:", 20, 105);
		
		// Instruction Details
		help_g.setFont(instruction);
		help_g.drawString("Click anywhere on the screen to produce bullets of color.", 40, 65);
		
		help_g.drawString("\'S\' - STRAIGHT MOTION", 40, 130);
		help_g.drawString("\'R\' - RANDOM MOTION", 40, 155);
		help_g.drawString("\'C\' - CIRCULAR MOTION", 40, 180);
		help_g.drawString("\'N\' - LEFT TURN", 40, 205);
		help_g.drawString("\'1\' - LENGTHEN TRAIL", 40, 230);
		
		help_g.drawString("\'T\' - TECH MOTION", 340, 130);
		help_g.drawString("\'O\' - ORBITAL MOTION", 340, 155);
		help_g.drawString("\'G\' - GRAVITY MOTION", 340, 180);
		help_g.drawString("\'M\' - RIGHT TURN", 340, 205);
		help_g.drawString("\'2\' - SHORTEN TRAIL", 340, 230);
		
		help_g.drawString("ARROW KEYS - Control the direction of all bullets of color.", 40, 255);
		
		help_g.drawString("\'Q\' - QUIT", 40, 280);
		help_g.drawString("\'D\' - DELETE BULLETS", 340, 280);
		
		// Dispose of Graphics
		help_g.dispose();
		
	}
	
	private boolean buttonSelected(BufferedImage image){
		int mouse_x = this.getMousePosition().x;
		int mouse_y = this.getMousePosition().y;
		Rectangle buttonRect = new Rectangle(button_x, button_y, image.getWidth(), image.getHeight());
		
		return buttonRect.contains(new Point(mouse_x, mouse_y));
	}
	
	private void printCommand(String command){
		BufferedImage newCommand = new BufferedImage(this.getWidth(), 50, BufferedImage.TYPE_INT_ARGB);		// The new Command
		
		Graphics2D g2d = newCommand.createGraphics();				// Get Graphics
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,		// AntiAlias - make images smoother
				RenderingHints.VALUE_ANTIALIAS_ON);
		
		g2d.setFont(new Font("Century Gothic", Font.BOLD, 25));		// Set Font	
		
		// Centering the Command on the BufferedImage (and window)
		int stringLength = (int) g2d.getFontMetrics().getStringBounds(command, g2d).getWidth();			// Get the String Length for a specific Font
		int start_x = commands.getWidth()/2 - stringLength/2;											// Find the starting point to draw the string for the x-coordinate		
		
		// Drawing out Command
		g2d.setColor(new Color(0xffffffff));
		g2d.drawString(command, start_x, 20);
		
		// Set Up for next commands and current rendering of command.
		opacityCounter = 60;
		commandOpacity = 1.0f;
		
		g2d.dispose();
		
		commands = newCommand;
	}

	public void run() {

		init();										// Initialize some objects
		
		long lastTime = System.nanoTime();
		final double amountOfTicks = 60;			//  "Frames per second"
		double ns = 1000000000 / amountOfTicks;		// Number of nanoseconds spent in each frame
		double delta = 0;							// Delta serves as a counter. It's keeping track of when enough time has passed to constitute a tick, or a frame. Once it is surpassed, then the world is updated.
		int updates = 0;							// Keeps track of how many ticks have passed
		int frames = 0;								// Keeps track of how many times the loop has been ran for a second.
		long timer = System.currentTimeMillis();
		
		while(running){
			long now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;
			
			// If a frame has passed, update the program environment.
			if(delta >= 1){
				tick();
				updates++;
				delta--;
			}
			render();
			frames++;
			
			// If a second has passed by, then print to the console some info. 
			if(System.currentTimeMillis() - timer > 1000){
				
				if(updates < 50){
					System.out.println("LOW FRAME RATE WARNING - TOO MANY BULLETS");
					deleteMode = true;
					printCommand("LOW FRAME RATE WARNING - DELETE ALL BULLETS");
					opacityCounter = 300;
				}
				
				timer += 1000;
				System.out.println(updates + " Ticks, Fps " + frames);
				updates = 0;
				frames = 0;
			}
		}
	}
	
	private void tick(){
		try{
			if(!helpOpen){
				int center_x = button_x + (open.getWidth()/2);
				int center_y = button_y + (open.getHeight()/2);
				int mouse_x = this.getMousePosition().x;
				int mouse_y = this.getMousePosition().y;
				int distance = (int) Math.hypot(mouse_x - center_x, mouse_y - center_y);
				
				buttonOpacity = (80.0f - distance)/ 50.0f;
				if(buttonOpacity > 1)
					buttonOpacity = 1.0f;
				else if(buttonOpacity < 0){
					buttonOpacity = 0.0f;
				}
				
				if(menuOpacity > 0.0f){
					menuOpacity -= 0.05f;
					if(menuOpacity < 0.0f)
						menuOpacity = 0.0f;
				}
				
			}
			else{
				if(menuOpacity < 1.0f){
					menuOpacity += 0.05f;
					if(menuOpacity > 1.0f)
						menuOpacity = 1.0f;
				}
			}
		}
		catch(NullPointerException e){
			
		}
		
		// Tick through Bullets
		for(int i = 0; i < bullets.size(); i++){
			bullets.get(i).tick();
		}
		
		// If there are too many bullets on screen and it lowers framerate, the bullets will be deleted 10 at a time until all have disappeared.
		if(deleteMode){
			if(bullets.size() <= 0){
				deleteMode = false;
			}
			else{
				for(int i = 0; i < 10; i++){
					if(bullets.size() == 0)
						break;
					
					bullets.remove(0);
				}
			}
		}
		
		// Update the background 
		renderNewField();
		
		// Add new bullets if the mouse is clicked
		if(held){
			try{
			bullets.add(new Bullet(this.getMousePosition().x, this.getMousePosition().y));
			}
			catch(NullPointerException e){
				System.out.println("Nullpointer Eliminated");   // Ignore infrequent null-pointer errors due to dragging the clicked mouse out of the window.
			}
		}
		
		// If the command has been on screen too long, allow the command to fade out.
		if(opacityCounter <= 0 && commandOpacity > 0){
			commandOpacity -= 0.015f;
			if(commandOpacity < 0){
				commandOpacity = 0f;
			}
		}
		else
			opacityCounter--;
	}
	
	private void render(){
		BufferStrategy bs = this.getBufferStrategy();
		
		// Create out new BufferStrategy if we start 
		if(bs == null){	
			createBufferStrategy(3);		// This creates 3 Buffers - Triple Buffering
			return;	
		}
		
		Graphics g = bs.getDrawGraphics();	// Creates a graphics context in order to draw out Buffers. This g is the canvas
		Graphics2D g2d = (Graphics2D) g;
		
		Graphics2D g2d_current = current.createGraphics();
		g2d_current.setRenderingHint(RenderingHints.KEY_ANTIALIASING,		// AntiAlias - make images smoother
				RenderingHints.VALUE_ANTIALIAS_ON);
		
		// ###RENDERING###
		for(int i = 0; i < bullets.size(); i++){	// Render the Bullets
			bullets.get(i).render(g2d_current);		// This createdGraphics is for BufferedImage current.
		}
		
		// Draw BufferedImages
		g.setColor(new Color(0xff000000));
		g.fillRect(0, 0, this.getWidth(), this.getHeight());		// Painting over the buffer of the previous frame (with black).
		g.drawImage(current, 0, 0, this);
		
		if(commandOpacity > 0){
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, commandOpacity));
			g2d.drawImage(commands, 0, 50, this);
		}
		
		if(!helpOpen){
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, buttonOpacity));
			g2d.drawImage(open, 320 - 20, 460, this);
			
			if(menuOpacity > 0.0f){
				g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, menuOpacity));
				g2d.drawImage(help, menu_x, menu_y, this);
			}
		}
		else if(helpOpen){
			g2d.drawImage(close, 320 - 20, 460, this);
			
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, menuOpacity));
			g2d.drawImage(help, menu_x, menu_y, this);
		}
		
		// ###END OF RENDERING###
		
		g.dispose();
		g2d.dispose();
		g2d_current.dispose();
		bs.show();
	}
	
	private void renderNewField(){
		BufferedImage update = new BufferedImage(current.getWidth(), current.getHeight(), BufferedImage.TYPE_INT_ARGB);
		for(int y = 0; y < current.getHeight(); y++){
			for(int x = 0; x < current.getWidth(); x++){
				// Get Colors
				int color = current.getRGB(x, y);

				if((color & 0x00ffffff) == 0x00000000) continue;		// Skip pixels that are already black
				
				int red = (color & 0x00ff0000) >> 16;
				int green = (color & 0x0000ff00) >> 8;
				int blue = (color & 0x000000ff);
				
				// Darken the pixel
				red -= trailDecrease;
				green -= trailDecrease;
				blue -= trailDecrease;
				
				// Make certain color does not go below 0
				if(red < 0){ red = 0x0;}
				if(green < 0){ green = 0x0;}
				if(blue < 0){ blue = 0x0;}
				
				// Set pixel to new color for update image
				red = red << 16;
				green = green << 8;
				color = red + green + blue + 0xff000000;
				
				update.setRGB(x, y, color);
			}
		}
		current = update;		// Set the current BufferedImage to the updated one
	}
	
	public static void main(String[] args){
		StringLights panel = new StringLights();
		
		panel.setPreferredSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
		panel.setMaximumSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
		panel.setMinimumSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
		
		JFrame frame = new JFrame(StringLights.TITLE);
		frame.add(panel);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
		panel.start(); 			// Starts the whole program loop
	}
}