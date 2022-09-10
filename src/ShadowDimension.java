import bagel.*;
import bagel.util.Point;
import bagel.util.Rectangle;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Skeleton Code for SWEN20003 Project 1, Semester 2, 2022
 * <p>
 * Please enter your name below
 *
 * @author
 */

public class ShadowDimension extends AbstractGame {
    private final static int WINDOW_WIDTH = 1024;
    private final static int WINDOW_HEIGHT = 768;
    private final static String GAME_TITLE = "SHADOW DIMENSION";
    private final static String PRESS_SPACE_TO_START = "PRESS SPACE TO START";
    private final static String USE_ARROW_KEYS_TO_FIND_GATE = "USE ARROW KEYS TO FIND GATE";
    private final static String CONGRATULATIONS = "CONGRATULATIONS!";
    private final static String GAME_OVER = "GAME OVER!";
    private final Image BACKGROUND_IMAGE = new Image("res/background0.png");
    private final Image SINKHOLE_IMAGE = new Image("res/sinkhole.png");
    private final Image WALL_IMAGE = new Image("res/wall.png");
    private final String PLAYER_RIGHT = "res/faeRight.png";
    private final String PLAYER_LEFT = "res/faeLeft.png";
    private Image PLAYER = new Image(PLAYER_LEFT);
    private final static String FONT = "res/frostbite.ttf";
    private String WORLD_DATA = "res/level0.csv";
    private final List<Point> walls = new ArrayList<>();
    private final List<Point> sinkHoles = new ArrayList<>();
    private Point topLeft = null;
    private Point bottomRight = null;
    private int playerX = 0;
    private int playerY = 0;
    private final int MAXIMUM_HEALTH_POINTS = 100;
    private final int DAMAGE_POINT = 30;
    private int healthStatus = 100;
    private boolean START = true;
    private boolean WIN = false;
    private boolean LOSE = false;

    public ShadowDimension() {
        super(WINDOW_WIDTH, WINDOW_HEIGHT, GAME_TITLE);
    }

    /**
     * The entry point for the program.
     */
    public static void main(String[] args) {
        ShadowDimension game = new ShadowDimension();
        game.readCSV();
        game.run();
    }

    /**
     * Method used to read file and create objects (You can change this
     * method as you wish).
     */
    private void readCSV() {
        try (BufferedReader br = new BufferedReader(new FileReader(WORLD_DATA))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                switch (values[0]) {
                    case "Player":
                        // Init Fae's coordination
                        playerX = Integer.parseInt(values[1]);
                        playerY = Integer.parseInt(values[2]);
                        break;
                    case "Wall":
                        // Add a wall to the array
                        walls.add(new Point(Integer.parseInt(values[1]), Integer.parseInt(values[2])));
                        break;
                    case "Sinkhole":
                        // Add a sinkhole to the array
                        sinkHoles.add(new Point(Integer.parseInt(values[1]), Integer.parseInt(values[2])));
                        break;
                    case "TopLeft":
                        // Config topLeft corner coordination
                        topLeft = new Point(Integer.parseInt(values[1]), Integer.parseInt(values[2]));
                        break;
                    case "BottomRight":
                        // Config bottom right corner coordination
                        bottomRight = new Point(Integer.parseInt(values[1]), Integer.parseInt(values[2]));
                        break;
                    default:
                        // ignore others
                        break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected boolean wallCheck(Point player) {
        // Method is to check overlaps or intersections
        Rectangle playerRect = new Rectangle(player, PLAYER.getWidth(), PLAYER.getHeight());
        for (Point p : walls) {
            // checks all walls regarding current coords of fae to check for intersections.
            Rectangle wallRect = new Rectangle(p, WALL_IMAGE.getWidth(), WALL_IMAGE.getHeight());
            if (wallRect.intersects(playerRect)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Performs a state update.
     * allows the game to exit when the escape key is pressed.
     */
    @Override
    protected void update(Input input) {
        Font font75 = new Font(FONT, 75);
        Font font40 = new Font(FONT, 40);
        Font font30 = new Font(FONT, 30);
        DrawOptions drawOptions = new DrawOptions();

        if (START) {
            // First Screen
            int x = 260; // As specified in the document
            int y = 250; // As specified in the document
            font75.drawString(GAME_TITLE, x, y);
            font40.drawString(PRESS_SPACE_TO_START, x + 90, y + 190);
            font40.drawString(USE_ARROW_KEYS_TO_FIND_GATE, Window.getWidth() / 2.0 - font40.getWidth(USE_ARROW_KEYS_TO_FIND_GATE) / 2.0, y + 240);
            if (input.wasPressed(Keys.SPACE)) {
                // Trigger to start the Game
                START = false;
            }
        } else if (WIN) {
            // Win Screen
            font75.drawString(CONGRATULATIONS, Window.getWidth() / 2.0 - font75.getWidth(CONGRATULATIONS) / 2.0, Window.getHeight() / 2.0);
        } else if (LOSE) {
            // Lose Screen
            font75.drawString(GAME_OVER, Window.getWidth() / 2.0 - font75.getWidth(GAME_OVER) / 2.0, Window.getHeight() / 2.0);
        } else {
            // Game Started Screen
            // DRAW Background
            BACKGROUND_IMAGE.draw(Window.getWidth() / 2.0, Window.getHeight() / 2.0);
            // DRAW Walls
            for (Point p : walls) {
                WALL_IMAGE.drawFromTopLeft(p.x, p.y);
            }
            // DRAW SinkHoles
            for (Point p : sinkHoles) {
                SINKHOLE_IMAGE.drawFromTopLeft(p.x, p.y);
                Rectangle sinkHoleRect = new Rectangle(p, SINKHOLE_IMAGE.getWidth(), SINKHOLE_IMAGE.getHeight()); // Rect of sinkhole
                Rectangle faeRect = new Rectangle(new Point(playerX, playerY), PLAYER.getWidth(), PLAYER.getHeight()); // Rect of Fae
                if (sinkHoleRect.intersects(faeRect)) {
                    // Intersection of rects detected
                    healthStatus -= DAMAGE_POINT;
                    // Log
                    if(healthStatus<0)
                        healthStatus = 0;
                    System.out.println("Sinkhole inflicts " + DAMAGE_POINT + " damage points on Fae. Fae’s current health:" + healthStatus + "/" + MAXIMUM_HEALTH_POINTS);
                    sinkHoles.remove(p); // Delete the sinkhole
                    break;
                }
            }
            // DRAW Fae
            PLAYER.drawFromTopLeft(playerX, playerY);

            // HEALTH LABEL
            if (healthStatus < 35) {
                drawOptions.setBlendColour(1, 0, 0);  // Red
            } else if (healthStatus >= 35 && healthStatus < 65) {
                drawOptions.setBlendColour(0.9, 0.6, 0);  // Orange
            } else {
                drawOptions.setBlendColour(0., 0.8, 0.2); // Green
            }
            // DRAW Health label at (20,25) with variable colors
            font30.drawString((100 * healthStatus) / MAXIMUM_HEALTH_POINTS + "%", 20, 25, drawOptions);

            // MOVE Fae up
            if (input.isDown(Keys.UP) && playerY >= topLeft.y) {
                if (!wallCheck(new Point(playerX, playerY - 2)))
                    playerY -= 2;
            }
            // MOVE Fae down
            if (input.isDown(Keys.DOWN) && playerY <= bottomRight.y) {
                if (!wallCheck(new Point(playerX, playerY + 2)))
                    playerY += 2;
            }
            // MOVE Fae left
            if (input.isDown(Keys.LEFT) && playerX >= topLeft.x) {
                PLAYER = new Image(PLAYER_LEFT);
                if (!wallCheck(new Point(playerX - 2, playerY)))
                    playerX -= 2;
            }
            // MOVE Fae right
            if (input.isDown(Keys.RIGHT) && playerX <= bottomRight.x) {
                PLAYER = new Image(PLAYER_RIGHT);
                if (!wallCheck(new Point(playerX + 2, playerY)))
                    playerX += 2;
            }

            // CONDITION WIN
            if (this.playerX >= 950 && this.playerY >= 670) {
                WIN = true;
            }
            // CONDITION LOSE
            if (healthStatus <= 0) {
                LOSE = true;
            }
        }

        // EXIT anytime
        if (input.wasPressed(Keys.ESCAPE)) {
            Window.close();
        }
    }
}
