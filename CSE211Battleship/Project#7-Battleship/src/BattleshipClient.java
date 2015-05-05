import java.awt.Point;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.JPanel;

/**
 * This class sets up a client socket that connects to a server. It then sends
 * guesses that were input by the user to the server. When the server sends back
 * two ints to evaluate the user's guess, the client interprets them and
 * displays an updated board to the user. The client closes when the game is
 * over.
 * 
 * @author Miranda
 * 
 */
public class BattleshipClient {

	final int SERVER_PORT = 32102;
	public  final int NUM_ROWS = 15;
	public final int NUM_COLUMNS = 15;
	private final int OKTOSEND = 1;
	private final int DONTSEND = 0;
	private final int IWIN = 20;
	private final int ILOSE = 30;
	private final int CONNECTIONDISCONNECTED = 40;
	private final int UPDATEBOARD = 50;
	private static final int MISS = 10;
	private static final int HIT = 20;
	private static final int SINK = 30;
	private static final int GAME_CONTINUES = 10;
	private static final int GAMEOVER = 20;
	private static final char AIRCRAFT_CARRIER = 'A';
	private static final char  BATTLESHIP = 'B';
	private static final char CRUISER = 'C';
	private static final char DESTROYER = 'D';
	private static final char SUBMARINE = 'S';
	private static final int AIRCRAFT_CARRIER_NUMBER = 5;
	private static final int  BATTLESHIP_NUMBER = 4;
	private static final int CRUISER_NUMBER = 3;
	private static final int SUBMARINE_NUMBER = 6;
	private static final int DESTROYER_NUMBER = 2;
	private boolean isPlayingMatch = true;
	//TODO CHANGE GAMESTATUS TO BOOLEAN
	private static int gameStatus;
	private Socket clientSocket = null;
	private DataOutputStream dos = null;
	private DataInputStream dis = null;
	int myGuessesBoard[][];
	int myShipsBoard [][];
	InetAddress serverIp;
	private String clientName = "";

	/**
	 * This constructor sets up a BattleshipClient and plays a game of
	 * battleship until the game is over.
	 */
	BattleshipClient() {


		getIpAddress();

		//WHILE FOR ALL OF MATCH == MULTIPLE GAMES ?????
		createSocket();
		createServerStreams();
		
		try {
			dis.readInt();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Scanner readFromConsole = new Scanner(System.in);
		System.out.println("Please enter your name: ");
		clientName = readFromConsole.nextLine();
		try {
			dos.writeUTF(clientName);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		while (isPlayingMatch) {
			
			System.out.println("New Game!!");
			myShipsBoard = createNewBoard();
			myGuessesBoard = createNewBoard();

			placeShips();

			



			System.out.println("id = " + this.clientSocket);
			//Set up Board
			Point [] aircraft = new Point [5];
			aircraft[0] = new Point(0,0);
			aircraft[1] = new Point(0,1);
			aircraft[2] = new Point(0,2);
			aircraft[3] = new Point(0,3);
			aircraft[4] = new Point(0,4);

			Point [] battleship = new Point [4];
			battleship[0] = new Point(1,0);
			battleship[1] = new Point(1,1);
			battleship[2] = new Point(1,2);
			battleship[3] = new Point(1,3);


			Point [] cruiser = new Point [3];
			cruiser[0] = new Point(2,0);
			cruiser[1] = new Point(2,1);
			cruiser[2] = new Point(2,2);


			Point [] submarine = new Point [3];
			submarine[0] = new Point(3,0);
			submarine[1] = new Point(3,1);
			submarine[2] = new Point(3,2);

			Point [] destroyer = new Point [2];
			destroyer[0] = new Point(4,0);
			destroyer[1] = new Point(4,1);


			//Send Board To Server


			try {
				dos.writeChar('A');
				for (int i = 0; i < aircraft.length; i++) {
					dos.writeInt(aircraft[i].x);
					dos.writeInt(aircraft[i].y);
				}
				dos.writeChar('B');
				for (int i = 0; i < battleship.length; i++) {
					dos.writeInt(battleship[i].x);
					dos.writeInt(battleship[i].y);
				}
				dos.writeChar('C');
				for (int i = 0; i < cruiser.length; i++) {
					dos.writeInt(cruiser[i].x);
					dos.writeInt(cruiser[i].y);
				}
				dos.writeChar('S');
				for (int i = 0; i < submarine.length; i++) {
					dos.writeInt(submarine[i].x);
					dos.writeInt(submarine[i].y);
				}
				dos.writeChar('D');
				for (int i = 0; i < destroyer.length; i++) {
					dos.writeInt(destroyer[i].x);
					dos.writeInt(destroyer[i].y);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}



			// exchange numbers with the server
			try {
				
				while (!(gameStatus > 10)) {
					int serverMessage = dis.readInt();
					System.out.println("SERVER MESSAGE before SENDRECEIVE" + serverMessage);
					sendAndReceiveNumbers(serverMessage);
					System.out.println("SERVER MESSAGE before READ" + serverMessage);
					//serverMessage = dis.readInt();
					System.out.println("SERVER MESSAGE AFTER READ" + serverMessage);
				} 
				
			}catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("CLIENT: CHECKING IF PLAYING AGAIN!");

			checkIfPlayingAgain();
			
		}

		// RESET GAME!!!!!!!!
		closeServerConnection();
	} // end BattleshipClient

	private void checkIfPlayingAgain() {
		try {
			System.out.println("CHECKING IF PLAYING CLIENT!!!! READING!!!...");
			int serverMessage = dis.readInt();
			
			System.out.println("GAMESTATUS: " + gameStatus);
			Scanner readFromConsole = new Scanner(System.in);
			System.out.println("Would you like to play again? ");
			int isPlayingAgain = readFromConsole.nextInt();
			dos.writeInt(isPlayingAgain);
			
			int areBothPlayersPlayingAgain = dis.readInt();
			
			System.out.println("CONSOLE READ: " + areBothPlayersPlayingAgain);
			if (areBothPlayersPlayingAgain == GAMEOVER) {
				isPlayingMatch = false;
				System.out.println("NOT PLAYING AGAIN");
			}
			else {
				System.out.println("Playing again");
				gameStatus = 10;
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	/**
	 * This main method creates a BattleshipClient object.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		new BattleshipClient();
	}

	private void placeShips() {

		System.out.println("PLacing ships");
		Point [] aircraft = new Point [5];
		aircraft[0] = new Point(0,0);
		aircraft[1] = new Point(0,1);
		aircraft[2] = new Point(0,2);
		aircraft[3] = new Point(0,3);
		aircraft[4] = new Point(0,4);

		Point [] battleship = new Point [4];
		battleship[0] = new Point(1,0);
		battleship[1] = new Point(1,1);
		battleship[2] = new Point(1,2);
		battleship[3] = new Point(1,3);


		Point [] cruiser = new Point [3];
		cruiser[0] = new Point(2,0);
		cruiser[1] = new Point(2,1);
		cruiser[2] = new Point(2,2);


		Point [] submarine = new Point [3];
		submarine[0] = new Point(3,0);
		submarine[1] = new Point(3,1);
		submarine[2] = new Point(3,2);

		Point [] destroyer = new Point [2];
		destroyer[0] = new Point(4,0);
		destroyer[1] = new Point(4,1);


		for (int i = 0; i < aircraft.length; i++) {

			myShipsBoard[aircraft[i].x][aircraft[i].y] = AIRCRAFT_CARRIER_NUMBER;
		}

		for (int i = 0; i < battleship.length; i++) {

			myShipsBoard[battleship[i].x][battleship[i].y] = BATTLESHIP_NUMBER;
		}

		for (int i = 0; i < cruiser.length; i++) {

			myShipsBoard[cruiser[i].x][cruiser[i].y] = CRUISER_NUMBER;
		}

		for (int i = 0; i < submarine.length; i++) {

			myShipsBoard[submarine[i].x][submarine[i].y] = SUBMARINE_NUMBER;
		}
		for (int i = 0; i < destroyer.length; i++) {

			myShipsBoard[destroyer[i].x][destroyer[i].y] = DESTROYER_NUMBER;
		}


	}

	/**
	 * This method asks the user to enter the IP address of the server and
	 * creates an InetAddress from the entry. If the field is blank, the IP
	 * address is set to 127.0.0.1 (the same computer as the client).
	 */
	protected void getIpAddress() {
		Scanner consoleIn = new Scanner(System.in);
		System.out.print("Enter the IP address of the server or hit enter: ");
		String serverIpStrg = "";
		serverIpStrg = consoleIn.nextLine();

		if (serverIpStrg.isEmpty()) { // if the field is empty
			serverIpStrg = "127.0.0.1";
		}

		System.out.println("\nServer IP is " + serverIpStrg);

		try {
			serverIp = InetAddress.getByName(serverIpStrg);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	} // end getIpAddress

	/**
	 * This method initializes a socket between the client and server.
	 */
	protected void createSocket() {
		try {
			clientSocket = new Socket(serverIp, SERVER_PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	} // end createSocket

	/**
	 * This method sets up input and output streams for the socket.
	 */
	protected void createServerStreams() {
		try {
			dos = new DataOutputStream(clientSocket.getOutputStream());
			dis = new DataInputStream(clientSocket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	} // end createServerStreams

	/**
	 * This method sends the user's guess to the server and reads the ints sent
	 * back. It then calls methods to interpret the ints sent from the server.
	 */
	protected void sendAndReceiveNumbers(int serverMessage) {
		try {
			// write user's guess
			if (serverMessage == OKTOSEND) {
				//Print Opponents Play on my ships board

				Scanner readFromConsole = new Scanner(System.in);
				System.out.print("Selected row: ");
				int rowGuess = readFromConsole.nextInt();
				System.out.print("Selected column: ");
				int columnGuess = readFromConsole.nextInt();

				dos.writeInt(rowGuess);
				dos.writeInt(columnGuess);

				int moveStatus = dis.readInt();
				gameStatus = dis.readInt();

				registerMove(rowGuess, columnGuess, moveStatus, myGuessesBoard);
				boolean IJustGuessed = true;
				checkGameStatus(IJustGuessed);

				///Game
				printMyGuessesBoard();
				
			
			}

			else if (serverMessage == UPDATEBOARD) {


				System.out.println("CLIENT WAITING TO UPDATEBOARD");
				int row = dis.readInt();
				int col = dis.readInt();
				int moveStatus = dis.readInt();
				gameStatus = dis.readInt();

				registerMove(row, col, moveStatus, myShipsBoard);
				boolean IJustGuessed = false;
				checkGameStatus(IJustGuessed);
				printMyShipsBoard();
			}


			// read server's response
			//moveStatus = dis.readInt();
			//gameStatus = dis.readInt();
		} catch (IOException e) {
			System.err.println("Sorry! Connection was disconnected!");
			closeServerConnection();
			gameStatus = CONNECTIONDISCONNECTED;

		}




		// evaluate server's numbers
		//moveStatuses();
		//gameStatuses();


	} // end sendAndReceiveNumbers


	/**
	 * This method interprets the first number sent back from the server. It
	 * will say whether the user's guess was a miss, hit, sink, or illegal move
	 * and update the board accordingly.
	 */
	protected void registerMove(int row, int col, int moveStatus, int [] [] board) {
		if (moveStatus == MISS) { // miss
			System.out.println("Miss!");
			board[row][col] = -1;
		}
		if (moveStatus == HIT) { // hit
			System.out.println("Hit!");
			board[row][col] = 1;
		}
		if (moveStatus == SINK) { // sink
			System.out.println("Ship Sank!");
			board[row][col] = 1;
		}
		if (moveStatus == CONNECTIONDISCONNECTED) // illegal
			System.out.println("Illegal Move! Game Over!");


		//printBoard();
	} // end moveStatus

	/**
	 * This method interprets the second number sent back from the server. It
	 * will say whether the user won, the server won, or there was an illegal
	 * move. If the game is continuing, there won't be anything printed to the
	 * console.
	 */
	protected void checkGameStatus(boolean IJustGuessed) {

		if (gameStatus == GAMEOVER && IJustGuessed)
			System.out.println("You won! All of the ships have been sunk!");
		else if (gameStatus == GAMEOVER && !IJustGuessed)
			System.out.println("You lose! All of your ships have been sunk!");
		else if (gameStatus == CONNECTIONDISCONNECTED) // Connection Disconnected
			System.out.println("Illegal move! The game is over.");
	} // end gameStatuses

	/**
	 * This method initializes the board to 2-d array of 0s.
	 */
	protected int [] [] createNewBoard() {
		int [] [] board = new int[NUM_ROWS][NUM_COLUMNS];

		// initialize to 0s
		for (int column = 0; column < (NUM_COLUMNS - 1); column++) {
			for (int row = 0; row < (NUM_ROWS - 1); row++)
				board[column][row] = 0;
		}
		return board;
	} // end createBoard



	/**
	 * This method prints the board to the console. It shows an M for a missed
	 * guess, a H for a hit, and a _ for an unguessed location.
	 */
	protected void printMyGuessesBoard() {
		for (int column = 0; column < NUM_COLUMNS; column++) {
			for (int row = 0; row < NUM_ROWS; row++) {
				System.out.print("| ");
				if (myGuessesBoard[column][row] < 0) // miss
					System.out.print("M");
				else if (myGuessesBoard[column][row] > 0) // hit
					System.out.print("H");
				else
					// not guessed yet
					System.out.print("_");
				System.out.print(" ");
			}
			System.out.println("|");
		}
	} // end printBoard

	protected void printMyShipsBoard () {
		for (int column = 0; column < NUM_COLUMNS; column++) {
			for (int row = 0; row < NUM_ROWS; row++)
				System.out.print("| " + myShipsBoard[column][row] + " ");
			System.out.println("|");
		}
	}

	/**
	 * This method closes the client/server connection.
	 */
	protected void closeServerConnection() {
		try {
			clientSocket.close();
			dis.close();
			dos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	} // end closeServerConnection

	/**
	 * This method reads in the user's row and column guess from the console.
	 */
	protected void read() {
		// take in a guess

	} // end read

}
