package AsteroidGame;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Bullet extends PFigure
{
   private final double velX;
   private final double velY;
   private int age;
   private static final double BULLET_SIZE = 3;
   private static final double BULLET_BASE_VEL = 3;
   private static double SHIP_VEL_FACTOR = 0.8;

   /**
    * constructor for a single bullet
    * @param startX starting x position
    * @param startY starting y position
    * @param rotation rotation
    * @param velXIn x velocity
    * @param velYIn y velocity
    * @param p pane the bullet lives on
    */
   public Bullet(double startX, double startY, double rotation, double velXIn, double velYIn, Pane p)
   {
      super(startX, startY, BULLET_SIZE, BULLET_SIZE, p);
      velX = Math.cos(Math.toRadians(rotation)) * BULLET_BASE_VEL + SHIP_VEL_FACTOR * velXIn;
      velY = -Math.sin(Math.toRadians(rotation)) * BULLET_BASE_VEL + SHIP_VEL_FACTOR * velYIn;
      draw();
   }

   /**
    * increments bullet age by one
    */
   public void age()
   {
      age++;
   }

   /**
    * returns the bullets age
    * @return
    */
   public int getAge()
   {
      return age;
   }

   /**
    * moves the bullet in the direction of the velocity
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
    * renders the bullet
    */
   public void draw()
   {
      Circle bulletBody = new Circle(
              x,           // center x
              y,           // center y
              BULLET_SIZE, // radius
              Color.GHOSTWHITE  // color
      );

      pane.getChildren().add(bulletBody);
   }
}
