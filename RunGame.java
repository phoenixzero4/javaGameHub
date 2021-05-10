
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class RunGame implements Runnable {

	public int score;
	private String name;
	protected boolean exit = false;
	protected Tetris game;
	public Player player;

	public int getScore() {
		return score;
	}

	public boolean isGameOver() {
		return game.isGameOver();
	}

	public RunGame(String name) {
		this.name = name;
	}

	public RunGame(Player player) {
		player = player;
		this.name = player.getPlayerName();
		System.out.println("RunGame task initiated");

	}

	@Override
	public void run() {
		game = Tetris.getInstance(player);
		game.setAlwaysOnTop(true);
		game.setVisible(true);

		game.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				System.out.println("Closed tetris game");
				System.out.println("Score is " + game.findScore() + "for " + name);
				score = game.findScore();

			}
		});
		while (!game.isGameOver()) {
			exit = false;
		}
		exit = true;

	}

}
