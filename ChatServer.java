import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JFrame;
import javax.swing.JTextArea;

public class ChatServer extends JFrame {

	/**
	 * The port that the server listens on.
	 */
	private static final int PORT = 9001;

	/**
	 * The set of all names of clients in the chat room. Maintained so that we can
	 * check that new clients are not registering name already in use.
	 */
	private static HashSet<String> names = new HashSet<String>();

	/**
	 * The set of all the print writers for all the clients. This set is kept so we
	 * can easily broadcast messages.
	 */
	private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();
	private static HashMap<String, Player> userMap = new HashMap<>();
	private static ArrayList<Player> playerList = new ArrayList<>();
	private static int count, playerCount, waitingCount;

	private static JTextArea outputArea;
	private static Player[] players = new Player[2];
	private static Player[] waitingPlayers = new Player[2];
	private static ExecutorService runGame;
	private static Lock gameLock;
	private static Condition otherPlayerConnected;

	public ChatServer() {
	} // no arg default constructor

	/**
	 * A handler thread class. Handlers are spawned from the listening loop and are
	 * responsible for a dealing with a single client and broadcasting its messages.
	 */
	static class Handler extends Thread {
		private String name;
		private Socket socket;
		private BufferedReader in;
		private PrintWriter out;

		/**
		 * Constructs a handler thread
		 * 
		 */
		public Handler(Socket socket) {
			this.socket = socket;
		}

		/**
		 * Waits for a unique screen name, registers the client and handles messages
		 */
		public void run() {
			try {

				// Create character streams for the socket.
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(), true);
				playerCount = 0;
				waitingCount = 0;
				// Continually request a user name until a unique name is received
				while (true) {
					// out.println("SUBMITNAME");
					name = in.readLine();
					if (name.equals("null") || name.isEmpty()) {
						name = "anonymous" + count++;
					}

					synchronized (names) {
						if (!names.contains(name)) {
							names.add(name);

							for (PrintWriter writer : writers) {
								if (writer != out) {
									writer.println("MESSAGE " + name + " has joined the server");
								}
							}
							break;
						}
					}
				}

				// Now that a unique name has been registered, add the
				// socket's print writer to the set of all writers so
				// this client can receive broadcast messages.
				out.println("NAMEACCEPTED");
				Player player = new Player(name, out);
				player.setPort(socket.getPort());
				player.setAddress(socket.getInetAddress());
				userMap.put(player.getPlayerName(), player);
				playerList.add(player);
				writers.add(out);
				for (PrintWriter writer : writers) {
					for (String s : names) {
						writer.println("UPDATE" + s);
					}
				}
				// Accept messages from this client and broadcast them.
				// Ignore other clients that cannot be broadcasted to.
				String input;
				while (true) {
					while ((input = in.readLine()) == null) {
					}
					;

					// process a request to challenge another player head 2 head
					// TODO add more games than Tetris
					if (input.startsWith("CHALLENGE")) {
						String challenger = input.substring(10);

						players[playerCount] = userMap.get(challenger);
						playerCount++;

						if (playerCount == players.length) {
							for (int i = 0; i < players.length; i++) {

								out = players[i].getWriter();
								out.println("TETRIS");
							}
							playerCount = 0;
							players = new Player[2];
						}

						/*
						 * wait for both players to finish their games and report to each the other's
						 * score and the winner
						 */
					} else if (input.startsWith("GETSCORE")) {

						String getPlayer = input.substring(8);
						Player curP = userMap.get(getPlayer);
						waitingPlayers[waitingCount] = curP;
						waitingCount++;

						if (waitingCount >= waitingPlayers.length) {
							PrintWriter pzero = waitingPlayers[0].getWriter();
							int scorezero = waitingPlayers[0].getScore("TETRIS");
							String namezero = waitingPlayers[0].getPlayerName();
							PrintWriter pone = waitingPlayers[1].getWriter();
							int scoreone = waitingPlayers[1].getScore("TETRIS");
							String nameone = waitingPlayers[1].getPlayerName();

							String game = "TETRIS";

							int oldscorezero = userMap.get(namezero).getScore(game);
							int oldscoreone = userMap.get(nameone).getScore(game);

							if (scorezero > oldscorezero) {
								userMap.get(namezero).addScore(scorezero, "TETRIS");

							}
							if (scoreone > oldscoreone) {
								userMap.get(nameone).addScore(scoreone, "TETRIS");

							}

							writeScores(userMap);

							System.out.println("FROM USERMAP\n" + userMap.get(nameone).getPlayerName()
									+ " high score for tetris " + userMap.get(nameone).getScore("TETRIS"));
							System.out.println("FROM USERMAP\n" + userMap.get(namezero).getPlayerName()
									+ " high score for tetris " + userMap.get(namezero).getScore("TETRIS"));

							if (userMap.get(namezero).getScore("TETRIS") < scorezero)
								System.out.println("Writing" + namezero + " scores to file");

							if (userMap.get(nameone).getScore("TETRIS") < scoreone)
								System.out.println("Writing" + nameone + " scores to file");

							writeScores(userMap);

							if (scorezero > scoreone) {
								pzero.println("\nYOU WIN! Your score: " + scorezero + "\n" + nameone + ": " + scoreone);
								pone.println(
										"\nYOU LOSE! Your score: " + scoreone + "\n" + namezero + ": " + scorezero);
							} else if (scorezero < scoreone) {
								pzero.println(
										"\nYOU LOSE! Your score: " + scorezero + "\n" + nameone + ": " + scoreone);
								pone.println("\nYOU WIN! Your score: " + scoreone + "\n" + namezero + ": " + scorezero);
							} else {
								pzero.println(
										"\nYOU TIE!\n Your score: " + scorezero + "\n" + nameone + ": " + scoreone);
								pone.println(
										"\nYOU TIE!\n Your score: " + scoreone + "\n" + namezero + ": " + scorezero);
							}
							waitingCount = 0;
							waitingPlayers = new Player[2];
						}
					} else if (input.startsWith("DM")) {
						int index = input.indexOf(":");
						String name = input.substring(2, index);
						int index2 = input.lastIndexOf(":");
						String message = input.substring(index + 1, index2);
						String user = input.substring(index2 + 1);

						System.out.println("SERVER: DM to " + name + "from user " + user);
						PrintWriter writer = userMap.get(name).getWriter();
						writer.println("** DM from " + user + ":  " + message);
						continue;

					} else if (input.startsWith("EXIT")) {
						String exiting = input.substring(4);
						names.remove(exiting);
						for (PrintWriter writer : writers) {
							writer.println("REMOVE" + exiting);
							writer.println(exiting + " left the server");
							userMap.remove(exiting);
						}
					} else if (input.startsWith("TETRIS")) {
						int index = input.indexOf(":") + 1;
						int index2 = input.lastIndexOf(":");
						String game = "TETRIS";
						String name = input.substring(index, index2);
						String score = input.substring(index2 + 1);
						Player curPlayer = userMap.get(name);
						curPlayer.addScore(Integer.valueOf(score), game);

					} else if (input.startsWith("MESSAGE")) {
						input = input.substring(7);
						for (PrintWriter writer : writers) {

							writer.println("MESSAGE " + name + ": " + input);

						}
					} else {
						for (String s : names) {
							out.println("UPDATE" + s);
						}
					}
				}
			} catch (

			IOException e) {
				System.out.println(e);
			} finally {
				// This client is going down! Remove its name and its print
				// writer from the sets, and close its socket.
				if (name != null) {
					names.remove(name);
				}
				if (out != null) {
					writers.remove(out);
				}
				try {
					socket.close();
				} catch (IOException e) {
				}
			}
		}

	}

	protected static void writeScores(HashMap<String, Player> map) {
		File file = new File("HighScores.txt");

		try {
			PrintWriter pw = new PrintWriter(file);

			map.forEach((key, value) -> pw.println(key + ":" + "TETRIS:" + value.getScore("TETRIS")));
			map.forEach((key, value) -> System.out.println(key + ":" + "TETRIS:" + value.getScore("TETRIS")));

			pw.close();

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {

		}
	}

	protected static void readScores() {

		File file = new File("HighScores.txt");
		String line = "";
		String paragraph = "";
		String name, game, score;

		if (file.exists()) {
			try {
				Scanner in = new Scanner(file);
				while (in.hasNextLine()) {
					line = in.nextLine();
					paragraph += line + "\n";

					int markone = line.indexOf(":");
					int marktwo = line.lastIndexOf(":");

					name = line.substring(0, markone);
					game = line.substring(markone + 1, marktwo);
					score = line.substring(marktwo + 1);
					int scoreint = Integer.valueOf(score);

					System.out.println("name: " + name);
					System.out.println("game: " + game);
					System.out.println("score: " + scoreint);

				}
				in.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws Exception {
		System.out.println("Game Hub server running.");
		count = 1;
		/* will start Tetris game server here for multi-player */

		runGame = Executors.newFixedThreadPool(2);
		gameLock = new ReentrantLock();
		otherPlayerConnected = gameLock.newCondition();
		players = new Player[2];

		ChatServer chat = new ChatServer();
		ServerSocket listener = new ServerSocket(PORT);
		try {
			while (true) {
				new Handler(listener.accept()).start();
			}
		} finally {
			listener.close();
		}

	}
}