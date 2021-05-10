import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;

public class Player {

	protected String name;
	protected int port;
	protected InetAddress address;
	protected PrintWriter out;

	protected HashMap<String, Integer> gameScores;
	protected Socket socket;
	private Scanner input;
	protected GameHubGUI gui;

	Tetris game = null;
	protected boolean exit = false;
	int score;

	public Player(String name) {

	}

	public Player(String name, PrintWriter out) {
		this.name = name;
		this.out = out;

		gameScores = new HashMap<>();
		gameScores.put("TETRIS", 0);
		gameScores.put("SNAKE", 0);
	}

	public boolean donePlaying() {
		return exit;
	}

	public Socket getSocket() {
		return socket;
	}

	public String getPlayerName() {
		return this.name;
	}

	public void setPlayerName(String name) {
		this.name = name;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getPort() {
		return this.port;
	}

	public void setAddress(InetAddress address) {
		this.address = address;
	}

	public InetAddress getAddress() {
		return this.address;
	}

	public PrintWriter getWriter() {
		return this.out;
	}

	public void setWriter(PrintWriter out) {
		this.out = out;
	}

	public void addScore(int score, String game) {
		if (gameScores.containsKey(game)) {
			int prevscore = gameScores.get(game);
			if (score > prevscore) {
				gameScores.put(game, score);
			}
		}
	}

	public int getScore(String game) {
		int score = 0;
		if (gameScores.containsKey(game)) {
			score = gameScores.get(game);
		}
		return score;
	}

	public void addGame(String game) {
		if (!gameScores.containsKey(game)) {
			gameScores.put(game, 0);
		}
	}

	public void addHub(GameHubGUI gui) {
		this.gui = gui;
	}

	public void runSingleGame() {

		game = Tetris.getInstance(this);
		game.setAlwaysOnTop(true);
		game.setVisible(true);

		game.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				score = game.findScore();
				gameScores.put("TETRIS", score);
				out.println("TETRIS:" + name + ":" + score);

			}
		});

		game.addComponentListener(new ComponentListener() {
			@Override
			public void componentHidden(ComponentEvent e) {
				score = game.findScore();
				gameScores.put("TETRIS", score);
				out.println("TETRIS:" + name + ":" + score);
			}

			public void componentMoved(ComponentEvent e) {
				// displayMessage(e.getComponent().getClass().getName() + " --- Moved");
			}

			public void componentResized(ComponentEvent e) {
				// displayMessage(e.getComponent().getClass().getName() + " --- Resized ");
			}

			public void componentShown(ComponentEvent e) {
				// displayMessage(e.getComponent().getClass().getName() + " --- Shown");

			}
		});
	}

	public void runTetris() {
		game = new Tetris(this);
		game = Tetris.getInstance(this);
		game.setAlwaysOnTop(true);
		game.setVisible(true);

		game.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				score = game.findScore();
				gameScores.put("TETRIS", score);
				out.println("TETRIS:" + name + ":" + score);
				out.println("GETSCORE" + name);

			}
		});
		game.addComponentListener(new ComponentListener() {
			@Override
			public void componentHidden(ComponentEvent e) {
				score = game.findScore();
				gameScores.put("TETRIS", score);
				out.println("TETRIS:" + name + ":" + score);
				out.println("GETSCORE" + name);

			}

			public void componentMoved(ComponentEvent e) {
				// displayMessage(e.getComponent().getClass().getName() + " --- Moved");
			}

			public void componentResized(ComponentEvent e) {
				// displayMessage(e.getComponent().getClass().getName() + " --- Resized ");
			}

			public void componentShown(ComponentEvent e) {
				// displayMessage(e.getComponent().getClass().getName() + " --- Shown");

			}
		});
	}

}
