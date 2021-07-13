package AsteroidGame;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.util.Pair;
import javafx.scene.shape.Line;

import java.util.WeakHashMap;

/**
 * class handling the player ship
 */
public class Player extends PFigure
{


   /**
    * maximum velocity magnitude
    */
   private final double MAX_VELOCITY = 15;

   /**
    * deceleration constant
    */
   private final double DECELERATION = -0.0075;

   /**
    * rotation in degrees from positive x-axis
    * 0 - 360 deg
    */
   private double rotation;

   /**
    * velocity in the x direction
    */
   private double velX;

   /**
    * velocity in the y direction
    */
   private double velY;

   public Player(Pane p)
   {
      super(600, 500, 25, 25, p);
      rotation = 0;
      draw();
   }

   /**
    * converts degree angle to radian and evaluates cosine
    * @param angle
    * @return cos(angle)
    */
   private double cos (double angle)
   {
      return Math.cos(Math.toRadians(angle));
   }

   /**
    * converts degree angle to radian and evaluates sine
    * @param angle
    * @return sin(angle)
    */
   private double sin (double angle)
   {
      return Math.sin(Math.toRadians(angle));
   }

   /**
    * tests if a number is negative
    * @param num
    * @return
    */
   private boolean isNegative(double num) { return num < 0;}


   /**
    * @return rotation
    */
   public double getRotation()
   {
      return rotation;
   }

   /**
    * @return x velocity
    */
   public double getVelX() { return velX; }

   /**
    * @return y velocity
    */
   public double getVelY()
   {
      return velY;
   }

   /**
    * @return x-position
    */
   public double getXPos()
   {
      return x;
   }

   /**
    * @return y-position
    */
   public double getYPos()
   {
      return y;
   }

   /**
    * resets ship to the default position
    */
   public void reset()
   {
      x = 600;
      y = 500;
      velX = 0;
      velY = 0;
      rotation = 0;
   }

   /**
    * @return the coordinates of the nose (front) of the ship
    */
   public Pair<Double, Double> getNosePosition()
   {
      return new Pair<Double, Double>(cos(rotation) * height + x,
      -sin(rotation) * height + y);
   }

   /**
    * rotates the ship
    * positive number is CCW
    * @param rotationChange
    */
   public void rotate(double rotationChange)
   {
      rotation += rotationChange;
      if (rotation > 360) rotation -= 360;
      else if (rotation < 0 ) rotation += 360;
   }

   /**
    * accelerates ship in the direction of its rotation
    * @param dVel
    */
   public void accelerate(double dVel)
   {
      double newVelX = velX + dVel * cos(rotation);
      double newVelY = velY + dVel * -sin(rotation);
      // max velocity check
      if (newVelX * newVelX + newVelY * newVelY <= MAX_VELOCITY * MAX_VELOCITY)
      {
         velX = newVelX;
         velY = newVelY;
      }
   }

   /**
    * accelerate (decelerate with negative num) in the direction of the ships velocity
    */
   public void decelerate(double dVel)
   {
      double velRotation = Math.toDegrees(Math.atan2(velY, velX));
      double newVelX, newVelY;

      newVelX = velX +  dVel * Math.cos(velRotation);
      newVelY = velY +  dVel * Math.sin(velRotation);

      // if new vel sign is different than current, clamp to zero
      if ( isNegative(newVelX) != isNegative(velX)) newVelX = 0;
      if ( isNegative(newVelY) != isNegative(velY)) newVelY = 0;

      velX = newVelX;
      velY = newVelY;
   }

   /**
    * updates ship position
    * wraps ship around screen if escaping the current window border
    */
   public void move()
   {
      super.move(velX, velY);
      decelerate(DECELERATION);

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
    * draws the ship body
    */
   public void draw()
   {
      Polygon shipHull = new Polygon();
      shipHull.getPoints().addAll(
              sin(rotation) * (width/2) - cos(rotation)*(width/2) + x,
              cos(rotation) * (height/2) + sin(rotation) * (height/2) + y,
              sin(rotation + 180) * (width/2) - cos(rotation)*(width/2) + x,
              cos(rotation + 180) * (height/2) + sin(rotation) * (height/2) + y,
              cos(rotation) * height + x,
              -sin(rotation) * height + y
              );

      Polygon shipRear = new Polygon();
      shipRear.getPoints().addAll(
              sin(rotation) * (width/2 + 5) - cos(rotation)*(width/2 + 5) + x,
              cos(rotation) * (height/2 + 5) + sin(rotation) * (height/2 + 5) + y,
              sin(rotation + 180) * (width/2 + 5) - cos(rotation)*(width/2 + 5) + x,
              cos(rotation + 180) * (height/2 + 5) + sin(rotation) * (height/2 + 5) + y,
              x,
              y
      );
      shipHull.setFill(Color.YELLOW);
      shipRear.setFill(Color.DARKORANGE);
      pane.getChildren().addAll(shipHull, shipRear);
   }

}
