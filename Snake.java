
import java.awt.Image;

import javax.swing.ImageIcon;

public class Snake {
	private Image head;
	private Image body;
	private Direction direction = Direction.RIGHT;

	private int numOfDots = 3;

	public Snake() {
		initSnake();
	}

	private void initSnake() {
		loadImages();
	}

	private void loadImages() {
		var iih = new ImageIcon("head.png");
		head = iih.getImage();

		var iib = new ImageIcon("dot.png");
		body = iib.getImage();
	}

	public void grow() {
		numOfDots++;
	}

	public Image getHead() {
		return head;
	}

	public Image getBody() {
		return body;
	}

	public int size() {
		return numOfDots;
	}

	public Direction getDirection() {
		return direction;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}
}
