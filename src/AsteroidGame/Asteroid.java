package AsteroidGame;

import javafx.scene.layout.Pane;

import javafx.scene.image.ImageView;

public class Asteroid extends PFigure
{
   public int size;
   public double direction;
   private final double velX;
   private final double velY;
   private final ImageView imageView;

   /**
    * Constructor for the Asteroids,
    * creates a velocity and loads in the image
    * @param startX starting x pos
    * @param startY starting y pos
    * @param direction starting angular direction
    * @param size Size of the asteroid, 100 for bigger, 50 for smaller
    * @param velocity speed of the asteroid
    * @param p Pane asteroid will live on
    */
   public Asteroid(double startX, double startY, double direction, int size,  double velocity, Pane p)
   {
      super(startX, startY, size, size, p);
      this.size = size;
      this.direction = direction;
      velX = Math.cos(Math.toRadians(direction)) * velocity;
      velY = Math.sin(Math.toRadians(direction)) * velocity;
      imageView = new ImageView("file:src/AsteroidGame/Assets/asteroid.png");
      draw();
   }

   /**
    * Moves the asteroid, wraps across the screen if needed
    */
   public void move()
   {
      super.move(velX, velY);

      // screen wrapping
      if ( x < -width / 2 )
         x = (pane.getWidth() - width / 2);
      else if ( (x + width / 2) > pane.getWidth() )
         x = -width / 2;
      if ( y < -height / 2 )
         y = (pane.getHeight() - height / 2);
      else if ( (y + height / 2) > pane.getHeight() )
         y = -height / 2;
   }

   /**
    * Checks if the asteroid is big enough
    * two create smaller ones once it is destroyed
    * @return true size is 100, false if not
    */
   public boolean isBigEnough()
   {
      return size == 100;
   }

   /**
    * Sets the images x and y coordinates,
    * size, then shows it to the pane
    */
   @Override
   public void draw()
   {
      if( imageView != null )
      {
         imageView.setX(x);
         imageView.setY(y);
         imageView.setFitHeight(size);
         imageView.setFitWidth(size);
         pane.getChildren().add(imageView);
      }
   }
}