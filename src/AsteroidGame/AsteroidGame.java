package AsteroidGame;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class AsteroidGame extends Application
{
    /**
     * main game pane
     */
    private final Pane figPane = new Pane();
    /**
     * The players ship
     */
    private final Player playerShip = new Player(figPane);
    /**
     * Arraylist containing the bullet objects
     */
    private final ArrayList<Bullet> bulletList = new ArrayList<>();
    /**
     * Arraylist containing the asteroid objects
     */
    private final ArrayList<Asteroid> asteroidList = new ArrayList<>();
    private int round = 1, score = 0;

    // playership control booleans
    private boolean debugMode;
    private boolean accelerate;
    private boolean rotateCCW;
    private boolean rotateCW;
    private boolean spaceBarReleased = true;

    // titlescreen state boolean
    private boolean titleScreen = false;

    // game control numbers for physics/difficulty/etc.
    private static final int SCREEN_HEIGHT = 1000; // game window height
    private static final int SCREEN_WIDTH = 1200; // game window width
    private final double ASTEROID_VEL_BASE = 0.4; // asteroid speed
    private final int MAX_BULLET_AGE = 350; // how long the bullet lasts
    private final double ACCELERATION = 0.04; // the acceleration of the player ship
    private final double ROTATIONAL_ACCEL = 1.5; // how fast the ship turns
    private final double DIFFICULTY_SCALE = 0.7; // scales the round difficulty as an exponential

    private Stage mainStage;
    private final Random rand = new Random();


    private Parent startGame()
    {
        figPane.setPrefSize(SCREEN_WIDTH, SCREEN_HEIGHT);
        return figPane;
    }

    /**
     * Boots the game or a new round
     * @return the javafx pane the game runs on
     */
    private Parent runGame()
    {
        setupGameWindow();
        startRound();
        AnimationTimer timer = new AnimationTimer()
        {
            @Override
            public void handle(long now)
            {
                update();
                if (titleScreen)
                {
                    this.stop();
                    showTitleScreen();
                } else if (checkPerson())
                {
                    this.stop();
                    endRound(false);
                } else if (checkWin())
                {
                    this.stop();
                    endRound(true);
                }
            }
        };
        timer.start();
        return figPane;
    }

    /**
     * main game loop that calls game logic functions
     */
    private void update()
    {
        figPane.getChildren().clear();
        controlShip();
        renderGameObjects();
        checkAsteroid();
        showScore();
        figPane.setVisible(true);
        if (debugMode)
            showDebug();
    }

    /**
     * Creates the game window, sets the background
     * depending on the round number, then adds the ship to the pane
     */
    private void setupGameWindow()
    {
        mainStage.setTitle("Asteroids");
        mainStage.show();
        figPane.requestFocus();
        figPane.setPrefSize(SCREEN_WIDTH, SCREEN_HEIGHT);

        if (round <= 4)
            figPane.setBackground(new Background(new BackgroundImage(new Image("file:src/AsteroidGame/Assets/Space2.jpg",
                    SCREEN_WIDTH, SCREEN_HEIGHT, false, true), BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT)));
        else
            figPane.setBackground(new Background(new BackgroundImage(new Image("file:src/AsteroidGame/Assets/SPACEjpg.jpg",
                    SCREEN_WIDTH, SCREEN_HEIGHT, false, true), BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT)));

        figPane.getChildren().add(playerShip);
        figPane.setVisible(true);
    }

    /**
     * Starts a new round by resetting the ships position,
     * handling the keyboard inputs, then adding the asteroids
     */
    private void startRound()
    {
        playerShip.reset();
        handleKeys();
        addAsteroids();
    }

    /**
     * Adds in the asteroids to an array list
     * then shows them to the screen
     */
    private void addAsteroids()
    {
        int startAst = 9 + ((round - 1) * 2); // num asteroids

        for(int x = 0; x < startAst; x++)
        {
            //1/4 on each side.
            if (x < startAst / 4)
                asteroidList.add(new Asteroid(50, rand.nextInt(SCREEN_HEIGHT),
                        rand.nextInt(361), 100, Math.pow((rand.nextInt(round + 1) + 1),
                        DIFFICULTY_SCALE) * ASTEROID_VEL_BASE, figPane));
            else if (x >= startAst / 4 && x < startAst / 2)
                asteroidList.add(new Asteroid(rand.nextInt(SCREEN_WIDTH), 50,
                        rand.nextInt(361), 100, Math.pow((rand.nextInt(round + 1) + 1),
                        DIFFICULTY_SCALE) * ASTEROID_VEL_BASE, figPane));
            else if (x >= startAst / 2 && x < startAst * 0.75)
                asteroidList.add(new Asteroid(1150, rand.nextInt(SCREEN_HEIGHT),
                        rand.nextInt(361), 100, Math.pow((rand.nextInt(round + 1) + 1),
                        DIFFICULTY_SCALE) * ASTEROID_VEL_BASE, figPane));
            else
                asteroidList.add(new Asteroid(rand.nextInt(SCREEN_WIDTH), 950,
                        rand.nextInt(361), 100, Math.pow((rand.nextInt(round + 1) + 1),
                        DIFFICULTY_SCALE) * ASTEROID_VEL_BASE, figPane));
        }
    }

    /**
     * When the round finished, displays a message and a button
     * to go to the next round. Also handles the button click event.
     * @param result true if you won the game, false if you lost
     */
    private void endRound(Boolean result)
    {
        bulletList.clear();
        asteroidList.clear();
        figPane.getChildren().clear();
        Text message = new Text();
        Text finalScore = new Text();
        Button nextRound = new Button();
        createText(message, finalScore, nextRound);

        if(result)
        {
            message.setText("You Win!!");
            finalScore.setX(400);
            finalScore.setText("Current Score: " + score);
            nextRound.setText("Level " + (round + 1));
            round++;
        }
        else
        {
            setNewHighScore();
            message.setText("You Lose");
            finalScore.setText("Final Score: " + score);
            nextRound.setText("New Game");
            round = 1;
        }

        figPane.setOnKeyPressed(e ->
        {
            if (e.getCode() == KeyCode.ENTER)
            {
                titleScreen = false;
                runGame();
            }
        });
        nextRound.setOnAction(e -> runGame());
        if(!result)
            score = 0;
        figPane.getChildren().addAll(message, nextRound, finalScore);
    }

    /**
     * Helper method for endRound,
     * sets the position for the labels and button
     */
    private void createText(Text message, Text finalScore, Button nextRound)
    {
        message.setX(415);
        message.setY(450);
        message.setFont(Font.font(90));
        message.setFill(Color.WHITE);

        finalScore.setY(300);
        finalScore.setX(415);
        finalScore.setFont(Font.font(50));
        finalScore.setFill(Color.WHITE);

        nextRound.setLayoutX(400);
        nextRound.setLayoutY(510);
        nextRound.setPrefSize(400, 125);
        nextRound.setFont(Font.font(40));
    }

    /**
     * Handles the ship movements, accelerating,
     * rotating, then redrawing the ship based on those movements
     */
    private void controlShip()
    {
        if (accelerate)
        {
            try { playSound("thrust"); }
            catch (Exception e) {System.out.println("Sound error: bang");}
            playerShip.accelerate(ACCELERATION);
        }
        // degrees
        if (rotateCCW)
            playerShip.rotate(ROTATIONAL_ACCEL);
        if (rotateCW)
            playerShip.rotate(-ROTATIONAL_ACCEL);
        playerShip.move();
        playerShip.draw();
    }

    /**
     * Renders the asteroids and bullets, handles there movements
     * then redraws them to the screen
     */
    private void renderGameObjects()
    {
        // render asteroids
        for(int x = 0; x < asteroidList.size(); x++)
        {
            asteroidList.get(x).move();
            asteroidList.get(x).draw();
        }

        // render bullets
        for(int i = 0; i < bulletList.size(); i++)
        {
            if (bulletList.get(i).getAge() > MAX_BULLET_AGE)
            {
                bulletList.remove(i);
                i++;
            }
            else
            {
                bulletList.get(i).move();
                bulletList.get(i).draw();
                bulletList.get(i).age();
            }
        }
    }

    /**
     * Trims down the number given to 3 precision points
     * @param num number to be trimmed
     * @return the trimmed number
     */
    private String trimDbl(double num)
    {
        return String.format("%.3f", num);
    }

    /**
     * Shows the debug menu, consisting of the players coordinates,
     * velocity, angular direction, round number,
     * and number of asteroids currently on the screen
     */
    private void showDebug()
    {
        String statistics =
                "X: " + trimDbl(playerShip.getXPos()) + "\n" +
                        "Y: " + trimDbl(playerShip.getYPos()) + "\n" +
                        "XV: " + trimDbl(playerShip.getVelX()) + "\n" +
                        "YV: " + trimDbl(playerShip.getVelY()) + "\n" +
                        "TH: " + trimDbl(playerShip.getRotation()) + "\n" +
                        "RD: " + round + "\n" +
                        "#A: " + asteroidList.size();

        Text debugInfo = new Text();
        debugInfo.setFill(Color.WHITE);
        debugInfo.setText(statistics);
        debugInfo.setX(50);
        debugInfo.setY(50);
        figPane.getChildren().add(debugInfo);
    }

    /**
     * Shows the score to the screen
     */
    private void showScore()
    {
        Text lbl = new Text("Score");
        lbl.setX(1095);
        lbl.setY(35);
        lbl.setFill(Color.WHITE);
        lbl.setFont(Font.font(25));

        Text sc = new Text();
        sc.setText(String.valueOf(score));
        sc.setFill(Color.WHITE);
        sc.setX(1100);
        sc.setY(75);
        sc.setFont(Font.font(25));

        figPane.getChildren().addAll(lbl, sc);
    }

    /**
     * checks if asteroid list is empty
     * @return true is list is empty, false if not
     */
    private Boolean checkWin()
    {
        return asteroidList.isEmpty();
    }

    /**
     * Checks if any bullets have collided with an asteroid.
     * If they have then it checks if the size is big enough to create two
     * smaller asteroids or not.
     */
    private void checkAsteroid()
    {
        for(int y = 0; y < asteroidList.size(); y++)
        {
            for(int x = 0; x < bulletList.size(); x++)
            {
                if(asteroidList.size() > y)
                {
                    if (bulletList.get(x).collidedWith(asteroidList.get(y)))
                    {
                        try { playSound("bang"); }
                        catch (Exception e) {System.out.println("Sound error: bang");}
                        score += 100;
                        bulletList.remove(x);
                        Asteroid temp = asteroidList.remove(y);
                        if(temp.isBigEnough())
                        {
                            asteroidList.add(new Asteroid(temp.x, temp.y, temp.direction, temp.size / 2,
                                    Math.pow((rand.nextInt(round + 1) + 2),0.6) * ASTEROID_VEL_BASE, figPane));
                            asteroidList.add(new Asteroid(temp.x, temp.y, -temp.direction, temp.size / 2,
                                    Math.pow((rand.nextInt(round + 1) + 2),0.6) * ASTEROID_VEL_BASE, figPane));
                        }
                    }
                }
            }
        }
    }

    /**
     * checks if the asteroids and players ship have collided
     * @return true if they have collided, false if not
     */
    private boolean checkPerson()
    {
        for(int x = 0 ; x < asteroidList.size(); x++)
            if(playerShip.collidedWith(asteroidList.get(x)))
                return true;

        return false;
    }

    /**
     * handles all keyboard input
     */
    private void handleKeys()
    {
        figPane.setOnKeyPressed(e ->
        {
            switch (e.getCode())
            {
                case UP:
                    accelerate = true;
                    break;
                case LEFT:
                    rotateCCW = true;
                    break;
                case RIGHT:
                    rotateCW = true;
                    break;
                case SPACE:
                    if (spaceBarReleased) shootBullet();
                    break;
                case F3:
                    debugMode = !debugMode;
                    break;
                case K:
                    if (debugMode) asteroidList.clear();
                    break;
                case ESCAPE:
                    titleScreen = true;
                    break;
                default:
                    break;
            }
        });
        keyRelease();
    }

    /**
     * Helper method for handleKeys,
     * handles the released keys
     */
    private void keyRelease()
    {
        figPane.setOnKeyReleased(e ->
        {
            switch (e.getCode())
            {
                case UP -> accelerate = false;
                case LEFT -> rotateCCW = false;
                case RIGHT -> rotateCW = false;
                case SPACE -> spaceBarReleased = true;
            }
        });
    }

    /**
     * Plays a shoot sounds, then adds a new bullet to the arraylist
     */
    public void shootBullet()
    {
        try { playSound("fire"); }
        catch (Exception e) {System.out.println("Sound error: fire");}
        Pair<Double, Double> shipsNosePosition = playerShip.getNosePosition();
        bulletList.add(new Bullet(shipsNosePosition.getKey(), shipsNosePosition.getValue(),
                playerShip.getRotation(), playerShip.getVelX(), playerShip.getVelY(), figPane));
        spaceBarReleased = false;
    }

    /**
     * Plays a sound based on what action is happening
     * @param soundID ID for the needed sound
     */
    public void playSound(String soundID)
    {
        String soundPath = null;
        switch (soundID) {
            case "fire" -> soundPath = "src/AsteroidGame/Assets/fire.wav";
            case "thrust" -> soundPath = "src/AsteroidGame/Assets/thrust.wav";
            case "bang" -> {
                int randBang = rand.nextInt(2) + 1;
                soundPath = switch (randBang) {
                    case 1 -> "src/AsteroidGame/Assets/bangLarge.wav";
                    case 2 -> "src/AsteroidGame/Assets/bangMedium.wav";
                    case 3 -> "src/AsteroidGame/Assets/bangSmall.wav";
                    default -> soundPath;
                };
            }
        }
        try
        {
            assert soundPath != null;
            java.io.File soundFile = new java.io.File(soundPath);
            javax.sound.sampled.AudioInputStream audioIn = javax.sound.sampled.AudioSystem.getAudioInputStream(soundFile);
            javax.sound.sampled.Clip clip = javax.sound.sampled.AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        }
        catch (Exception e)
        {
            System.out.println("Invalid SoundID or Filepath for sound: " + soundID);
        }
    }

    /**
     * stops the game and boots the title screen window
     */
    private void showTitleScreen()
    {
        titleScreen = true; round = 1; score = 0;
        bulletList.clear();
        asteroidList.clear();
        figPane.getChildren().clear();
        figPane.setBackground(new Background(new BackgroundImage(new Image("file:src/AsteroidGame/Assets/titleScreenBackground.png",
                SCREEN_WIDTH, SCREEN_HEIGHT, false, true), BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT)));
        Button playButton = new Button();
        setupButton(playButton, SCREEN_WIDTH/2 - 100, SCREEN_HEIGHT/2 + 100, 200, 100, 40, "START");
        playButton.setOnAction(e ->
        {
            titleScreen = false;
            runGame();
        });
        Button scoresButton = new Button();
        setupButton(scoresButton, SCREEN_WIDTH/2 - 100, SCREEN_HEIGHT/2 + 210, 200, 100, 37, "SCORES");
        scoresButton.setOnAction(e -> { showScoresScreen(); });
        Button quitButton = new Button();
        setupButton(quitButton, SCREEN_WIDTH/2 - 100, SCREEN_HEIGHT/2 + 320, 200, 100, 40, "QUIT");
        quitButton.setOnAction(e -> {mainStage.close(); });

        figPane.setOnKeyPressed(e ->
        {
            if (e.getCode() == KeyCode.ENTER)
            {
                titleScreen = false;
                runGame();
            } else if (e.getCode() == KeyCode.ESCAPE)
                mainStage.close();
        });
        figPane.getChildren().addAll(playButton, scoresButton, quitButton);
    }

    /**
     * displays the high scores screen
     */
    private void showScoresScreen()
    {
        figPane.getChildren().clear();
        figPane.setBackground(new Background(new BackgroundImage(new Image("file:src/AsteroidGame/Assets/scoresScreenBackground.png",
                SCREEN_WIDTH, SCREEN_HEIGHT, false, true), BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT)));
        Button backButton = new Button();
        setupButton(backButton, SCREEN_WIDTH/2 - 100, SCREEN_HEIGHT/2 + 100, 200, 100, 40, "BACK");
        backButton.setOnAction(e->{ showTitleScreen(); });
        getHighScores();
        figPane.getChildren().addAll(backButton);
    }

    /**
     * checks if a new high score has been made
     * edits high score list if yes
     */
    private void setNewHighScore()
    {
        File scoreFile = new File("src/AsteroidGame/Assets/game.txt");
        int score1 = 0, score2 = 0, score3 = 0;
        try
        {
            Scanner scoreReader = new Scanner(scoreFile);
            score1 = Integer.parseInt(scoreReader.nextLine());
            score2 = Integer.parseInt(scoreReader.nextLine());
            score3 = Integer.parseInt(scoreReader.nextLine());
            scoreReader.close();
        }
        catch (Exception e) {System.out.println("Error reading scores file: " + e.getMessage());}
        try
        {
            FileWriter scoreWriter = new FileWriter(scoreFile);
            scoreWriter.flush();
            if (score > score3)
                showHighScore();
            if (score > score1)
            {
                score3 = score2;
                score2 = score1;
                score1 = score;
            } else if (score > score2)
            {
                score3 = score2;
                score2 = score;
            } else if (score > score3)
                score3 = score;
            scoreWriter.write(score1 + "\n" + score2 + "\n" + score3);
            scoreWriter.close();
        }
        catch (Exception e) {System.out.println("Error writing scores file: " + e.getMessage());}
    }

    /**
     * displays the high score notification message on the game over screen
     * if a new high score has been achieved
     */
    private void showHighScore()
    {
        Text highScoreNotify = new Text();
        setupText(highScoreNotify, SCREEN_WIDTH/2 - 180, SCREEN_HEIGHT/4, Color.YELLOW, 40, "NEW HIGH SCORE!");
        figPane.getChildren().add(highScoreNotify);
    }

    /**
     * retrieves high scores from the scores file and displays them on the scores screen
     */
    private void getHighScores()
    {
        File scoreFile = new File("src/AsteroidGame/Assets/game.txt");
        try
        {
            Scanner scoreReader = new Scanner(scoreFile);
            String score1 = scoreReader.nextLine();
            String score2 = scoreReader.nextLine();
            String score3 = scoreReader.nextLine();
            Text highScore1 = new Text();
            Text highScore2 = new Text();
            Text highScore3 = new Text();
            setupText(highScore1, SCREEN_WIDTH/2 - 60, SCREEN_HEIGHT/3, Color.YELLOW, 35, "1.   " + score1);
            setupText(highScore2, SCREEN_WIDTH/2 - 60, SCREEN_HEIGHT/3 + 50, Color.YELLOW, 35, "2.   " + score2);
            setupText(highScore3, SCREEN_WIDTH/2 - 60, SCREEN_HEIGHT/3 + 100, Color.YELLOW, 35, "3.   " + score3);
            figPane.getChildren().addAll(highScore1,highScore2, highScore3);
        }
        catch (Exception e) {System.out.println("Error reading scores file: " + e.getMessage());}
    }


    /**
     * helper method that sets up a javafx text object
     * @param inText text object
     * @param layoutX x position
     * @param layoutY y position
     * @param colorFill fill color
     * @param fontSize font size
     * @param text text to display
     */
    private void setupText(Text inText, double layoutX, double layoutY,
                           Color colorFill, int fontSize, String text)
    {
        inText.setX(layoutX);
        inText.setY(layoutY);
        inText.setFill(colorFill);
        inText.setFont(new Font(fontSize));
        inText.setText(text);
    }

    /**
     * helper method that sets up a javafx button object
     * @param inButton button to be set
     * @param layoutX x position
     * @param layoutY y position
     * @param xsize x size
     * @param ysize y size
     * @param fontsize size of the button text
     * @param text text to display on the button
     */
    private void setupButton(Button inButton, double layoutX, double layoutY,
                             double xsize, double ysize, double fontsize, String text)
    {
        inButton.setLayoutX(layoutX);
        inButton.setLayoutY(layoutY);
        inButton.setPrefSize(xsize, ysize);
        inButton.setFont(Font.font(fontsize));
        inButton.setText(text);
    }

    /**
     * Starts the program
     * @param primaryStage stage that everything will be on
     */
    @Override
    public void start(Stage primaryStage)
    {
        mainStage = primaryStage;
        mainStage.setScene(new Scene(startGame()));
        handleKeys();
        Image icon = new Image("file:src/AsteroidGame/Assets/asteroid.png");
        mainStage.getIcons().add(icon);
        mainStage.centerOnScreen();
        mainStage.show();
        figPane.requestFocus();
        showTitleScreen();
    }

    /**
     * Main method
     * @param args
     */
    public static void main(String[] args)
    {
        try
        {
            launch(args);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
}
