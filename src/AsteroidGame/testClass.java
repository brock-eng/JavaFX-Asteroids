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

import java.util.ArrayList;
import java.util.Random;

public class testClass extends Application
{
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

   // boolean that determines if game win conditions should be checked
   private boolean updateCheck = true;

   // player ship control booleans
   private boolean debugMode;
   private boolean accelerate;
   private boolean rotateCCW;
   private boolean rotateCW;
   private boolean spaceBarReleased = true;

   // these are the primary control values that control the gameplay
   private static final int SCREEN_HEIGHT = 1000;  // game window height
   private static final int SCREEN_WIDTH = 1200;   // game window width
   private final double ASTEROID_VEL_BASE = 0.4;   // asteroid speed
   private final int MAX_BULLET_AGE = 350;         // how long the bullet lasts
   private final double ACCELERATION = 0.04;       // the acceleration of the player ship
   private final double ROTATIONAL_ACCEL = 1.5;    // how fast the ship turns
   private final double DIFFICULTY_SCALE = 0.65;   // scales the round difficulty as an exponential

   private Stage mainStage;                        // main game stage
   private final Random rand = new Random();       // for all random game values

   /**
    * sets up the game loop, runs on game start
    */
   private void initializeGame()
   {
      AnimationTimer timer = new AnimationTimer()
      {
         @Override
         public void handle(long now)
         {
            if (updateCheck)
            {
               update();
               showScore();
               if (checkPerson())
               {
                  this.stop();
                  endRound(false);
               } else if (checkWin())
               {
                  this.stop();
                  endRound(true);
               }
            }
         }
      };
      timer.start();
   }


   private Parent loadPanel()
   {
      setupGameWindow();
      return figPane;
   }

   /**
    * Boots the game or a new round
    * @return the javafx pane the game runs on
    */
   private void runGame()
   {
      updateCheck = true;
      setupGameWindow();
      startRound();
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

      if (round <= 6)
         figPane.setBackground(new Background(new BackgroundImage(new Image("file:src/AsteroidGame/Assets/Space2.jpg",
                 SCREEN_WIDTH, SCREEN_WIDTH, false, true), BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT,
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
         // 1/4 on each side.
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

   private void showTitleScreen()
   {
      round = 1;
      endRound(false);
      figPane.getChildren().clear();
      figPane.setBackground(new Background(new BackgroundImage(new Image("file:src/AsteroidGame/Assets/titleScreenBackground.png",
              SCREEN_WIDTH, SCREEN_WIDTH, false, true), BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT,
              BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT)));

      Text titleText = new Text();
      titleText.setX(SCREEN_WIDTH / 2);
      titleText.setY(SCREEN_HEIGHT / 2);
      titleText.setText("Asteroids");

      Button playButton = new Button();
      playButton.setOnAction(e -> runGame());
      figPane.setOnKeyPressed(e ->
      {
         if (e.getCode() == KeyCode.ENTER)
            runGame();
      });

      playButton.setLayoutX(400);
      playButton.setLayoutY(510);
      playButton.setPrefSize(400, 125);
      playButton.setFont(Font.font(40));

      figPane.getChildren().addAll(playButton, titleText);
      update();
   }

   /**
    * clears all game object data
    */
   private void clearRound()
   {
      bulletList.clear();
      asteroidList.clear();
   }

   /**
    * When the round finished, displays a message and a button
    * to go to the next round. Also handles the button click event.
    * @param result true if you won the game, false if you lost
    */
   private void endRound(Boolean result)
   {
      clearRound();
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
         message.setText("Game Over!");
         finalScore.setText("Final Score: " + score);
         nextRound.setText("New Game");
         round = 1;
      }

      figPane.setOnKeyPressed(e ->
      {
         if (e.getCode() == KeyCode.ENTER)
            runGame();
      });
      nextRound.setOnAction(e -> runGame());
      figPane.getChildren().clear();
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
    * Renders the asteroids and bullets, handles their movements
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
    * Trims down the number given to 3 precision points
    * @param num number to be trimmed
    * @return the trimmed number
    */
   private String trimDbl(double num)
   {
      return String.format("%.3f", num);
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
               if (debugMode)
               {
                  round--;
                  endRound(true);
               }
               break;
            case ESCAPE:
               updateCheck = false;
               showTitleScreen();
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
            case UP:
               accelerate = false;
               break;
            case LEFT:
               rotateCCW = false;
               break;
            case RIGHT:
               rotateCW = false;
               break;
            case SPACE:
               spaceBarReleased = true;
               break;
            default:
               break;
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
    * Starts the program
    * @param primaryStage stage that everything will be on
    */
   @Override
   public void start(Stage primaryStage)
   {
      mainStage = primaryStage;
      mainStage.setScene(new Scene(loadPanel()));
      handleKeys();
      Image icon = new Image("file:src/AsteroidGame/Assets/asteroid.png");
      mainStage.getIcons().add(icon);
      mainStage.centerOnScreen();
      mainStage.show();
      figPane.requestFocus();
      initializeGame();
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
