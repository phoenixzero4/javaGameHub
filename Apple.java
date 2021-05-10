
import java.awt.Image;

import javax.swing.ImageIcon;

public class Apple {

	private Image head;
	private Image body;

	private int x;
	private int y;
	private Image apple;

	public Apple() {
		loadImage();
	}

	private void loadImage() {
		var iia = new ImageIcon("apple.png");
		apple = iia.getImage();

	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public Image getApple() {
		return apple;
	}

	public void setApple(Image apple) {
		this.apple = apple;
	}

}
