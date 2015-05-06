import java.net.*;
import java.io.*;

/**
 * @author Miranda
 * 
 *         Battleship server for program1 and 2. Uses a ServerSocket to listen
 *         for TCP connection requests from clients. Instantiates a
 *         TCPBattleshipThread object to handle each client.
 * 
 */
public class BattleshipServer {

	// Socket for listening for clients
	ServerSocket serverSocket = null;

	// Counter for tracking number of clients
	int clientCount = 1;

	//I'm not moving anything. Just trying things out. Also What email? We still are using threads.
	//That doesn't say anything about where to keep track of turns...
	//I'm just trying things not screwing with the whole program ;)
	//I think we're good!!!!!!!!!!!!

	// Port number for server to listen on
	final int LISTEN_PORT = 32102;

	// Flag for controlling listening loop
	// (Could be set to false to cause the server to stop
	// listening for clients
	boolean listening = true;
	public int client1board[][];
	public int client2board[][];
	BattleshipThread client1;
	BattleshipThread client2;
	private boolean isGameOver = false;
	boolean isClient1Turn = false;
	boolean isClient2Turn = false;
	private String client1name = "";
	private String client2name = "";
	private static final int NUM_ROWS = 15;
	private static final int NUM_COLUMNS = 15;
	private static final int GAME_CONTINUES = 10;
	private boolean isNewMatch = true;

	/**
	 * Constructor for the TCPListen class. It creates a ServerSocket to use for
	 * listening for clients. Displays the port number and IP address for the
	 * ServerSocket to the console and then enters an infinite loop to listen
	 * for clients. Spawns a new TCPListenThread object to handle each client.
	 */
	public BattleshipServer() throws IOException {
		try {
			// Instantiate socket to listening
			serverSocket = new ServerSocket(LISTEN_PORT);
		} catch (IOException e) {
			// Port not available
			System.err.println("Could not listen on port: " + LISTEN_PORT);
			System.exit(-1);
		}

		// Display socket information.
		displayContactInfo();

		//HandleClients -> SetupConnection (NEW THREADS) ->SetupGame -> WHILE(TAKTURNS)

		boolean isTrue = true;
		while (isTrue) {
			handleClients();
		}

		// Enter infinite loop to listen for clients
		//listenForClients();
		//setupGame();



		//while (!isGameOver) {
		//takeTurns();

		//}

		// Close the socket (unreachable code)
		serverSocket.close();

	} // end TCPListen constructor

	protected void handleClients() {
		if (isNewMatch) {
			try {
				listenForClients();
				startClientThreads();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			isNewMatch = false;
		}
		client1.newGame();
		client2.newGame();
		setupGame();

		while (!isGameOver) {
			System.out.println("Before TakeTurns: isGameOver = " + isGameOver);
		//	private static final int CHECKIFPLAYING = 300;
			
			takeTurns();
			System.out.println("After TakeTurns: isGameOver = " + isGameOver);
		}
		
		
		System.out.println("SERVER: GAME OVER RECIEVING PLAYING AGAIN!");
		System.out.println("After taketurns looop: isGameOver = " + isGameOver);
		boolean cli1pa = client1.receiveIsPlayingAgain();
		boolean cli2pa = client2.receiveIsPlayingAgain();
		
		
		System.out.println("CLIENT 1 PLAY AGAIN = " + cli1pa);
		System.out.println("CLIENT 2 PLAY AGAIN = " + cli2pa);
		if ( cli1pa == false || cli2pa == false) {
			System.out.println("WAS FALSE");
			client1.sendMatchOver();
			client2.sendMatchOver();
			isNewMatch = true;
		}
		else {
			client1.sendMatchContinues();
			client2.sendMatchContinues();
		}


	}

	protected synchronized void setupGame() {

		isGameOver = false;
		client1board = null;
		client2board = null;
		client1name = client1.getClientName();
		client2name = client2.getClientName();

		
		//Setup Boards
		client1board = client1.getBoard();
		client2board = client2.getBoard();

		System.out.println("Client1 Name: " + client1name);
		printBoard(1);
		System.out.println("Client2 Name: " + client2name);
		printBoard(2);

		client1.setOpponentBoard(client2board);
		client2.setOpponentBoard(client1board);




		determineTurns();

	}

	protected void determineTurns() {
		if (Math.random() > 0.5 ) {
			isClient1Turn = true;

		}
		else {
			isClient2Turn = true;
		}
	}

	protected synchronized void takeTurns() {
		if (isClient1Turn) {
			client1.receiveGuess();
			//MoveStatus = HIT
			//GameStatus = CONTINUE

			client2.updateBoard(client1.getRowGuess(),client1.getColumnGuess(),
					client1.getMoveStatus(), client1.getGameStatus());


			if (client1.getGameStatus() != GAME_CONTINUES || client2.getGameStatus() != GAME_CONTINUES) {
				isGameOver = true;
			}


			isClient1Turn = false;
			isClient2Turn = true;
		}
		else {

			client2.receiveGuess();

			client1.updateBoard(client2.getRowGuess(),client2.getColumnGuess(),
					client2.getMoveStatus(), client2.getGameStatus());


			if (client1.getGameStatus() != GAME_CONTINUES || client2.getGameStatus() != GAME_CONTINUES) {
				isGameOver = true;
			}

			isClient1Turn = true;
			isClient2Turn = false;
		}

	}

	/**
	 * Diplays IP address and port number that clients can use to contact the
	 * server to the console.
	 */
	protected void displayContactInfo() {
		try {
			// Display contact information.
			System.out.println("Number Server standing by to accept Clients:"
					+ "\nIP Address: " + InetAddress.getLocalHost()
					+ "\nPort: " + serverSocket.getLocalPort() + "\n\n");

		} catch (UnknownHostException e) {
			// NS lookup for host IP failed?
			// This should only happen if the host machine does
			// not have an IP address.

		}

	} // end displayContactInfo

	
	protected void startClientThreads() {
		client1.start();
		client2.start();
	}
	/**
	 * Infinite loop for listing for new clients. Spawns a new TCPListenThread
	 * object to handle each client.
	 */
	protected void listenForClients() throws IOException {
		// Create new thread to hand each client.
		// Pass the Socket object returned by the accept
		// method to the thread.
		client1 = null;
		client2 = null;
		client1 = new BattleshipThread(serverSocket.accept(), 1);
		client2 = new BattleshipThread(serverSocket.accept(), 2);		

	} // end listenForClients

	/**
	 * This method prints the board to the console.
	 */
	protected void printBoard(int clientNumber) {
		// print each element of the board
		for (int column = 0; column < NUM_COLUMNS; column++) {
			for (int row = 0; row < NUM_ROWS; row++) {
				if (clientNumber == 1) {
					System.out.print("| " + client1board[column][row] + " ");
				}
				else if (clientNumber == 2) {
					System.out.print("| " + client2board[column][row] + " ");
				}
			}
			System.out.println("|");
		}
	} // end printBoard


	/**
	 * Instantiates a Server the listens for clients that are going to send a
	 * series of numbers.
	 */
	public static void main(String[] args) throws IOException {
		// Instantiate Server
		new BattleshipServer();

	} // end main

} // end TCPListen class

