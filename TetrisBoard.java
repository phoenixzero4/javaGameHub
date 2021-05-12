
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

public class TetrisBoard extends JPanel {

	private final int BOARD_WIDTH = 10;
	private final int BOARD_HEIGHT = 22;
	private final int PERIOD = 300;

	private Timer timer;
	private boolean isFallingFinished = false;
	private boolean isPaused = false;
	private int numLinesRemoved = 0;
	private int curX = 0;
	private int curY = 0;

	private JLabel statusBar;
	private Shape curPiece;
	private Shape.Tetrominoe[] board;

	public int score;
	private boolean gameOver = false;

	public TetrisBoard(Tetris parent) {
		initBoard(parent);
	}

	public int getScore() {
		return score;
	}

	private void initBoard(Tetris parent) {
		setPreferredSize(new Dimension(200, 400));
		setFocusable(true);

		statusBar = parent.getStatusBar();
		score = 0;
		addKeyListener(new TAdapter());
	}

	private int squareWidth() {
		return (int) getSize().getWidth() / BOARD_WIDTH;
	}

	private int squareHeight() {
		return (int) getSize().getHeight() / BOARD_HEIGHT;
	}

	private Shape.Tetrominoe shapeAt(int x, int y) {
		return board[(y * BOARD_WIDTH) + x];
	}

	void start() {
		curPiece = new Shape();
		board = new Shape.Tetrominoe[BOARD_WIDTH * BOARD_HEIGHT];

		clearBoard();
		newPiece();

		timer = new Timer(PERIOD, new GameCycle());
		timer.start();
	}

	private void pause() {
		isPaused = !isPaused;

		if (isPaused) {
			statusBar.setText("PAUSED");
		} else {
			// statusBar.setText(String.valueOf(numLinesRemoved));
			statusBar.setText("Lines: " + numLinesRemoved + " Score: " + score);
		}
		repaint();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		doDrawing(g);
	}

	private void doDrawing(Graphics g) {
		var size = getSize();
		int boardTop = (int) size.getHeight() - BOARD_HEIGHT * squareHeight();

		for (int i = 0; i < BOARD_HEIGHT; i++) {
			for (int j = 0; j < BOARD_WIDTH; j++) {
				Shape.Tetrominoe shape = shapeAt(j, BOARD_HEIGHT - i - 1);

				if (shape != Shape.Tetrominoe.NoShape) {
					drawSquare(g, j * squareWidth(), boardTop + i * squareHeight(), shape);
				}
			}
		}

		if (curPiece.getShape() != Shape.Tetrominoe.NoShape) {
			for (int i = 0; i < 4; i++) {
				int x = curX + curPiece.x(i);
				int y = curY - curPiece.y(i);

				drawSquare(g, x * squareWidth(), boardTop + (BOARD_HEIGHT - y - 1) * squareHeight(),
						curPiece.getShape());
			}
		}
	}

	private void dropDown() {

		int newY = curY;

		while (newY > 0) {
			if (!tryMove(curPiece, curX, newY - 1)) {
				break;
			}
			newY--;
		}
		pieceDropped();
	}

	private void oneLineDown() {
		if (!tryMove(curPiece, curX, curY - 1)) {
			pieceDropped();
		}
	}

	private void clearBoard() {
		for (int i = 0; i < BOARD_HEIGHT * BOARD_WIDTH; i++) {
			board[i] = Shape.Tetrominoe.NoShape;
		}
	}

	private void pieceDropped() {
		for (int i = 0; i < 4; i++) {
			int x = curX + curPiece.x(i);
			int y = curY - curPiece.y(i);
			board[(y * BOARD_WIDTH) + x] = curPiece.getShape();
		}

		removeFullLines();

		if (!isFallingFinished) {
			newPiece();
		}
	}

	public boolean isGameOver() {
		return gameOver;
	}

	private void newPiece() {
		curPiece.setRandomShape();
		curX = BOARD_WIDTH / 2 + 1;
		curY = BOARD_HEIGHT - 1 + curPiece.minY();

		if (!tryMove(curPiece, curX, curY)) {
			curPiece.setShape(Shape.Tetrominoe.NoShape);
			timer.stop();
			gameOver = true;

			this.setEnabled(false);
			var msg = String.format("Game Over. Score %d", score);
			statusBar.setText(msg);

		}
	}

	private boolean tryMove(Shape newPiece, int newX, int newY) {
		for (int i = 0; i < 4; i++) {
			int x = newX + newPiece.x(i);
			int y = newY - newPiece.y(i);

			if (x < 0 || x >= BOARD_WIDTH || y < 0 || y >= BOARD_HEIGHT) {
				return false;
			}
			if (shapeAt(x, y) != Shape.Tetrominoe.NoShape) {
				return false;
			}
		}

		curPiece = newPiece;
		curX = newX;
		curY = newY;

		repaint();
		return true;
	}

	private void removeFullLines() {
		int numFullLines = 0;
		for (int i = BOARD_HEIGHT - 1; i >= 0; i--) {
			boolean lineIsFull = true;
			for (int j = 0; j < BOARD_WIDTH; j++) {
				if (shapeAt(j, i) == Shape.Tetrominoe.NoShape) {
					lineIsFull = false;
					break;
				}
			}

			if (lineIsFull) {
				numFullLines++;

				for (int k = i; k < BOARD_HEIGHT - 1; k++) {
					for (int j = 0; j < BOARD_WIDTH; j++) {
						board[(k * BOARD_WIDTH) + j] = shapeAt(j, k + 1);
					}
				}
			}
		}

		if (numFullLines > 0) {
			numLinesRemoved += numFullLines;
			// statusBar.setText(String.valueOf(numLinesRemoved));
			switch (numFullLines) {
			case 1:
				score += 10;
				break;
			case 2:
				score += (numLinesRemoved * 12);
				break;
			case 3:
				score += (numLinesRemoved * 15);
				break;
			case 4:
				score += (numLinesRemoved * 20);
			}
			statusBar.setText("Lines: " + numLinesRemoved + " Score: " + score);
			isFallingFinished = true;
			/*
			 * try { TimeUnit.MILLISECONDS.sleep(500); }catch(InterruptedException e) {
			 * System.err.println(e); }
			 */
			curPiece.setShape(Shape.Tetrominoe.NoShape);
		}
	}

	private void drawSquare(Graphics g, int x, int y, Shape.Tetrominoe shape) {

		Color colors[] = { new Color(0, 0, 0), new Color(204, 102, 102), new Color(102, 204, 102),
				new Color(102, 102, 204), new Color(204, 204, 102), new Color(204, 102, 204), new Color(102, 204, 204),
				new Color(218, 170, 0) };

		// brighter pastel colors
		Color colors2[] = { new Color(0, 0, 0), new Color(255, 0, 250), new Color(0, 131, 255), new Color(0, 255, 114),
				new Color(246, 255, 0), new Color(127, 0, 255), new Color(255, 0, 182), new Color(218, 170, 0) };

		var color = colors2[shape.ordinal()];

		g.setColor(color);
		g.fillRect(x + 1, y + 1, squareWidth() - 2, squareHeight() - 2);

		g.setColor(color.brighter());
		g.drawLine(x, y + squareHeight() - 1, x, y);
		g.drawLine(x, y, x + squareWidth() - 1, y);

		g.setColor(color.darker());
		g.drawLine(x + 1, y + squareHeight() - 1, x + squareWidth() - 1, y + squareHeight() - 1);
		g.drawLine(x + squareWidth() - 1, y + squareHeight() - 1, x + squareWidth() - 1, y + 1);
	}

	private void doGameCycle() {

		update();
		repaint();
	}

	private void update() {

		if (isPaused) {
			return;
		}
		if (isFallingFinished) {
			isFallingFinished = false;
			newPiece();
		} else {
			oneLineDown();
		}
	}

	class TAdapter extends KeyAdapter {

		@Override
		public void keyPressed(KeyEvent e) {

			if (curPiece.getShape() == Shape.Tetrominoe.NoShape) {
				return;
			}

			int keycode = e.getKeyCode();

			switch (keycode) {

			case KeyEvent.VK_P:
				pause();
				break;

			case KeyEvent.VK_LEFT:
				tryMove(curPiece, curX - 1, curY);
				break;

			case KeyEvent.VK_RIGHT:
				tryMove(curPiece, curX + 1, curY);
				break;

			case KeyEvent.VK_UP:
				tryMove(curPiece.rotateLeft(), curX, curY);
				break;

			case KeyEvent.VK_DOWN:
				tryMove(curPiece.rotateRight(), curX, curY);
				break;

			case KeyEvent.VK_SPACE:
				dropDown();
				break;

			case KeyEvent.VK_D:
				oneLineDown();
				break;
			}
		}
	}

	private class GameCycle implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			doGameCycle();
		}
	}

}
