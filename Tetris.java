
import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class Tetris extends JFrame {

	private JLabel statusbar;
	private TetrisBoard board;
	protected static String name;

	protected static Tetris tetris = null;
	static Player thisPlayer;

	protected Tetris(String name) {
		this.name = name;
		initUI();
	}

	protected Tetris(Player thisPlayer) {

		this.thisPlayer = thisPlayer;
		this.name = thisPlayer.getPlayerName();
		initUI();
	}

	private void initUI() {

		statusbar = new JLabel("Lines: -- Score:--");

		add(statusbar, BorderLayout.SOUTH);

		board = new TetrisBoard(this);
		add(board);
		board.start();

		setResizable(false);
		pack();

		setTitle(name + "Tetris");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);

		board.addPropertyChangeListener("enabled", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				if (!board.isEnabled()) {
					try {
						TimeUnit.SECONDS.sleep(2);

					} catch (InterruptedException ie) {
						ie.printStackTrace();
					}
				}
			}

		});
	}

	public JLabel getStatusBar() {

		return statusbar;
	}

	public int findScore() {
		return board.getScore();
	}

	public boolean isGameOver() {
		return board.isGameOver();
	}

	// make Singleton so there can only be one game running at a time
	protected static Tetris getInstance(Player thisPlayer) {
		if (tetris == null) {
			tetris = new Tetris(thisPlayer);
		} else {
			// JOptionPane.showMessageDialog(null, "Tetris already Running");
			tetris = null;
			tetris = new Tetris(thisPlayer);
		}
		return tetris;
	}

	/*
	 * public static void main(String[] args) {
	 * 
	 * EventQueue.invokeLater(() -> {
	 * 
	 * var game = new Tetris(); game.setVisible(true);
	 * 
	 * });
	 * 
	 * }
	 */
}
