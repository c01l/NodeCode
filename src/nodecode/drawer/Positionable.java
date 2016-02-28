package nodecode.drawer;

import java.awt.Point;

public interface Positionable {
	/**
	 * Returns the position of the object.
	 * 
	 * @return
	 */
	Point getPosition();

	/**
	 * Sets the new position of the object.
	 * 
	 * @param x
	 * @param y
	 */
	void setPosition(int x, int y);

	/**
	 * This method checks if the mouse is over the area where the object could
	 * be moved.
	 * 
	 * @param mx
	 * @param my
	 * @return
	 */
	boolean isMouseOverMoveArea(int mx, int my);
}
