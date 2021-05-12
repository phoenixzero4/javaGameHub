
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.Border;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

public class GameHubGUI extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	private JPanel textPanel, inputPanel;
	private JTextField textField;
	private String message, name, line;
	private Font meiryoFont = new Font("Meiryo", Font.PLAIN, 14);
	private Border blankBorder = BorderFactory.createEmptyBorder(10, 10, 20, 10);// top,r,b,l
	private JList<String> list;
	private DefaultListModel<String> listModel;

	protected JTextArea textArea, userArea;
	protected JFrame frame;
	protected JButton privateMsgButton, sendPrivateButton, cancelDMButton, joinButton, sendButton, tetrisButton,
			snakeButton, exitButton;
	protected JPanel clientPanel, userPanel;
	protected String[] games;

	private static BufferedReader in;
	private static PrintWriter out;
	protected boolean gameRunning;
	protected Map gameScores;
	protected ObjectOutputStream objOutput;

	private static Socket socket;
	protected boolean connected = false;
	protected static HashSet<String> currentUsers;

	protected static int count = 0;
	protected String privateName = "";
	protected static String serverAddress;
	public Player player = null;

	// stylizing experiment
	private JTextPane pane;
	private JPanel panel;

	/**
	 * GUI Constructor
	 */
	public GameHubGUI(String name) {
		this.name = name;
		frame = new JFrame("Java Game Hub");
		currentUsers = new HashSet<>();
		gameRunning = false;

		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (Exception e) {
		}

		// -----------------------------------------
		/*
		 * intercept close method, inform server we are leaving then let the system
		 * exit.
		 */
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {

				if (this != null) {
					try {
						out.println("EXIT" + name);
						// close client
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				System.exit(0);
			}
		});

		// -----------------------------------------
		// frame.setUndecorated(true);
		// frame.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);

		Container c = getContentPane();
		JPanel outerPanel = new JPanel(new BorderLayout());

		outerPanel.add(getInputPanel(), BorderLayout.CENTER);
		outerPanel.add(getTextPanel(), BorderLayout.NORTH);

		c.setLayout(new BorderLayout());
		c.add(outerPanel, BorderLayout.CENTER);
		c.add(getUsersPanel(), BorderLayout.WEST);

		frame.add(c);
		frame.pack();
		frame.setAlwaysOnTop(true);
		frame.setLocation(150, 150);
		textField.requestFocus();
		frame.setBackground(Color.gray);

		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	/**
	 * Method to set up the JPanel to display the chat text
	 */
	public JPanel getTextPanel() {

		String heading = "Java Arcade Chat";
		textArea = new JTextArea(heading, 14, 34);
		textArea.setMargin(new Insets(10, 10, 10, 10));
		textArea.setFont(meiryoFont);

		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(textArea);
		textPanel = new JPanel();
		textPanel.add(scrollPane);
		textPanel.setFont(new Font("Meiryo", Font.PLAIN, 14));
		return textPanel;
	}

	/**
	 * Method to build the panel with input field
	 */
	public JPanel getInputPanel() {
		inputPanel = new JPanel(new GridLayout(1, 1, 5, 5));
		inputPanel.setBorder(blankBorder);
		textField = new JTextField();
		textField.setFont(meiryoFont);
		textField.addActionListener(this);
		inputPanel.add(textField);
		return inputPanel;
	}

	/**
	 * Method to build the panel displaying currently connected users with a call to
	 * the button panel building method
	 */
	public JPanel getUsersPanel() {

		userPanel = new JPanel(new BorderLayout());
		// userPanel.setBackground(Color.gray);
		String userStr = " Current Users      ";

		JLabel userLabel = new JLabel(userStr, JLabel.CENTER);
		userPanel.add(userLabel, BorderLayout.NORTH);
		userLabel.setFont(new Font("Meiryo", Font.PLAIN, 16));

		setClientPanel(currentUsers);
		clientPanel.setFont(meiryoFont);
		userPanel.add(makeButtonPanel(), BorderLayout.SOUTH);
		userPanel.setBorder(blankBorder);

		return userPanel;
	}

	/**
	 * Populate current user panel with a list of currently connected users
	 */
	public void setClientPanel(HashSet<String> currentUsers) {
		clientPanel = new JPanel(new BorderLayout());
		clientPanel.setBackground(Color.gray);
		listModel = new DefaultListModel<String>();

		Iterator<String> iterator = currentUsers.iterator();
		while (iterator.hasNext()) {
			int i = 1;
			String next = (String) iterator.next();
			if (next.equals(name)) {
				next += " (you)";
			}
			listModel.addElement(next);
		}
		if (currentUsers.size() > 1) {
			privateMsgButton.setEnabled(true);
			privateMsgButton.setForeground(Color.magenta);
		}

		// Create the list and put it in a scroll pane.
		list = new JList<String>(listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setVisibleRowCount(8);
		list.setFont(meiryoFont);
		JScrollPane listScrollPane = new JScrollPane(list);

		clientPanel.add(listScrollPane, BorderLayout.CENTER);
		userPanel.add(clientPanel, BorderLayout.CENTER);
	}

	/**
	 * Make the buttons and add the listener
	 */
	public JPanel makeButtonPanel() {
		sendButton = new JButton("Send ");
		sendButton.addActionListener(this);
		sendButton.setBackground(Color.gray);
		sendButton.setForeground(Color.blue);

		joinButton = new JButton("Challenge Player");
		joinButton.addActionListener(this);
		joinButton.setBackground(Color.gray);
		joinButton.setForeground(Color.blue);

		privateMsgButton = new JButton("DM");
		privateMsgButton.setBackground(Color.gray);
		privateMsgButton.addActionListener(this);
		privateMsgButton.setEnabled(false);

		sendPrivateButton = new JButton("Send Private Message");
		sendPrivateButton.setEnabled(false);
		sendPrivateButton.addActionListener(this);

		exitButton = new JButton("Leave ");
		exitButton.addActionListener(this);
		exitButton.setBackground(Color.gray);
		exitButton.setForeground(Color.cyan);

		tetrisButton = new JButton("Tetris ");
		tetrisButton.setBackground(Color.gray);
		tetrisButton.setForeground(Color.cyan);
		tetrisButton.addActionListener(this);

		snakeButton = new JButton("Snake ");
		snakeButton.setBackground(Color.gray);
		snakeButton.setForeground(Color.blue);
		snakeButton.addActionListener(this);

		JPanel buttonPanel = new JPanel(new GridLayout(4, 2));
		buttonPanel.add(privateMsgButton);
		buttonPanel.add(sendButton);
		buttonPanel.add(joinButton);
		buttonPanel.add(exitButton);

		// add the current games
		buttonPanel.add(tetrisButton);
		buttonPanel.add(snakeButton);
		buttonPanel.add(sendPrivateButton);

		return buttonPanel;
	}

	/**
	 * Action handling on the buttons
	 */
	@Override
	public void actionPerformed(ActionEvent e) {

		try {

			if (e.getSource() == joinButton) {
				out.println("CHALLENGE:" + name);
				textArea.append("\n* * * Waiting for Opponent * * *\n");
				if (player == null) {
					player = new Player(name, out);
					player.addHub(this);
				}
				sendButton.setEnabled(false);
				joinButton.setEnabled(false);
				privateMsgButton.setEnabled(false);
				snakeButton.setEnabled(false);
				tetrisButton.setEnabled(false);
			}

			if (e.getSource() == exitButton) {
				out.println("EXIT" + name);
				System.exit(0);
			}

			// send a private message, to selected users
			if (e.getSource() == privateMsgButton) {
				privateName = list.getSelectedValue();
				sendPrivateButton.setEnabled(true);
				sendPrivateButton.setForeground(Color.magenta);
				sendButton.setEnabled(false);
			}

			if (e.getSource() == sendPrivateButton) {
				sendPrivate();
			}

			if (e.getSource() == tetrisButton) {
				if (player == null) {
					player = new Player(name, out);
					player.addHub(this);
				}
				player.runSingleGame();

			}
			if (e.getSource() == snakeButton) {
				gameRunning = true;
				SnakeGame game = new SnakeGame();
				game.setVisible(true);

				game.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(WindowEvent we) {
						System.out.println("Closed snake game");
					}
				});
				System.out.println("Launching game Snake for " + name);
			}

			if (e.getSource() == textField) {
				if (sendPrivateButton.isEnabled()) {
					sendPrivate();
				} else if (!sendPrivateButton.isEnabled() && sendButton.isEnabled()) {
					message = textField.getText();
					textField.setText("");
					out.println("MESSAGE" + message);
				}
			}

			if (e.getSource() == sendButton) {
				message = textField.getText();
				textField.setText("");
				out.println("MESSAGE" + message);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}// end actionPerformed

	// --------------------------------------------------------------------

	/**
	 * Send a message, to be relayed, only to selected chatters
	 */

	private void sendPrivate() {
		message = textField.getText();
		textField.setText("");
		out.println("DM" + privateName + ":" + message + ":" + name);

		textArea.append("DM to " + privateName + ":  " + message);
		sendPrivateButton.setEnabled(false);
		sendButton.setEnabled(true);
	}

	private static String getServerAddress() {
		return JOptionPane.showInputDialog(null, "Enter IP Address of the Server:", "Welcome to Java Game Hub",
				JOptionPane.QUESTION_MESSAGE);
	}

	private static String getPlayerName() {
		return JOptionPane.showInputDialog(null, "Choose a screen name:", "Enter Username" + "",
				JOptionPane.PLAIN_MESSAGE);
	}

	private static boolean connectToServer(String address) {
		boolean connected = false;
		serverAddress = address;
		try {
			socket = new Socket(address, 9001);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
			connected = true;
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return connected;
	}

	private static boolean checkName(String name) throws IOException {
		String line = "";
		out.println(name);
		line = in.readLine();
		if (line.startsWith("NAMEACCEPTED")) {
			return true;
		}
		return false;
	}

	protected void run() throws IOException {

		Player player = new Player(name, out);

		textArea.removeAll();
		textArea.append("\nWelcome " + name + "\n");

		while (true) {
			while ((line = in.readLine()) == null) {
			}
			;

			System.out.println("Client received line: " + line);
			if (line.startsWith("MESSAGE")) {
				textArea.append(line.substring(8) + "\n");

			} else if (line.startsWith("REPORT")) {
				int delim = line.indexOf(":");
				int delim2 = line.lastIndexOf(":");
				String scoreString = line.substring(delim + 1, delim2);
				String thisName = line.substring(delim2 + 1);
				int score = Integer.valueOf(scoreString);
				textArea.append("You scored " + score);

				int oldScore = player.getScore("TETRIS");
				String msg = "";
				if (oldScore < score) {
					msg = "\nYou earned a new hi-score!";
					msg += "\nPrevious hi-score: " + oldScore;
					textArea.append(msg);
					player.addScore(score, "TETRIS");
					System.out.println("Updating highscore to " + score + " from " + oldScore);
				} else {
					msg = "\nPrevious Hi-score: " + oldScore;
				}

			} else if (line.startsWith("UPDATE")) {
				String updateName = line.substring(6);
				if (!currentUsers.contains(updateName)) {
					currentUsers.add(updateName);
					updateUsers(currentUsers);
				}

			} else if (line.startsWith("REMOVE")) {
				currentUsers.remove(line.substring(6));
				updateUsers(currentUsers);
			} else if (line.startsWith("TETRIS")) {
				if (player == null) {
					player = new Player(name, out);
					player.addHub(this);

				}

				player.runTetris();

			} else if (line.startsWith("YOU")) {
				snakeButton.setEnabled(true);
				joinButton.setEnabled(true);
				privateMsgButton.setEnabled(true);
				tetrisButton.setEnabled(true);
				sendButton.setEnabled(true);
				textArea.append(line + "\n");
			} else {

				textArea.append(line + "\n");

				// panel = new JPanel();
				// pane = new JTextPane();
				// panel.add(pane);
				// appendToPane(pane, "Private Message From: ", Color.cyan);
				// appendToPane(pane, "Phoenix", Color.blue);
				// panel.setVisible(true);
				// panel.setDefaultCloseOperation(EXIT_ON_CLOSE);
			}
		}

	}

	private void appendToPane(JTextPane tp, String msg, Color c) {
		StyleContext sc = StyleContext.getDefaultStyleContext();
		AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

		aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
		aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

		int len = tp.getDocument().getLength();
		tp.setCaretPosition(len);
		tp.setCharacterAttributes(aset, false);
		tp.replaceSelection(msg);
	}

	private void updateUsers(HashSet<String> currentUsers) {
		try {

			this.userPanel.remove(this.clientPanel);
			this.setClientPanel(currentUsers);
			this.clientPanel.repaint();
			this.clientPanel.revalidate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		String address, name;

		if (args.length < 1) {
			address = getServerAddress();

			try {
				if (connectToServer(address)) {
					name = getPlayerName();
					name = name.substring(0, 1).toUpperCase() + name.substring(1);
					if (checkName(name)) {
						GameHubGUI gamehub = new GameHubGUI(name);
						currentUsers.add(name);
						gamehub.updateUsers(currentUsers);

						gamehub.run();
					} else {
						System.err.println("Name already taken.\nTry again");
						System.exit(1);
						// TODO create a method so we can loop this rather than
						// exiting
					}
				} else {
					System.err.println("No server at " + address);
					System.exit(0);
				}
			} catch (ClassCastException cce) {
				cce.printStackTrace();
			} catch (Exception exception) {
				System.err.println(exception);
				exception.printStackTrace();
			}

		} else if (args.length == 2) {
			address = args[0];
			name = args[1];

			try {
				if (connectToServer(address)) {
					name = name.substring(0, 1).toUpperCase() + name.substring(1);
					if (checkName(name)) {

						GameHubGUI gamehub = new GameHubGUI(name);
						currentUsers.add(name);
						gamehub.updateUsers(currentUsers);
						gamehub.run();

					} else {
						System.err.println("Name already taken.\nTry again");
						System.exit(1);
						// TODO create a method so we can loop this rather than
						// exiting
					}
				} else {
					System.err.println("No server at " + address);
					System.exit(0);
				}
			} catch (ClassCastException cce) {
				cce.printStackTrace();
			} catch (Exception exception) {
				System.err.println(exception);
				exception.printStackTrace();
			}
		}
	}
}// end class
