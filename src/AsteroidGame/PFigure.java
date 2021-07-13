package AsteroidGame;

import javafx.scene.layout.Pane;

public abstract class PFigure extends Pane
{
   protected double x, y;           // Current position of the figure
   protected double width, height;  // Drawn or displayed this size
   protected Pane pane;             // Pane the figure lives on

   public PFigure(double startX, double startY, double _width, double _height, Pane p )
   {
      x = startX;
      y = startY;
      width = _width;
      height = _height;
      pane = p;
   }

   public boolean collidedWith ( PFigure p )
   {
      if (  p == null )
         return false;

      return ( x + width ) >= p.x && ( p.x + p.width ) >= x &&
              ( y + height ) >= p.y && ( p.y + p.height ) >= y;
   }

   public void move ( double deltaX, double deltaY )
   {
      x += deltaX;
      y += deltaY;
   }

   // Draw the figure.
   abstract public void draw();

   public void hide()
   {
      pane.setVisible(false);
   }


}