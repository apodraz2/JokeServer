
//referenced libraries
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.Stack;
import java.util.UUID;

/*--------------------------------------------------------

1. Name / Date: Adam Podraza 4/16/14

2. Java version used, if not the official version for the class:

Java 1.7

3. Precise command-line compilation examples / instructions:

e.g.:

> javac JokeServer.java
> javac JokeClient.java

4. Precise examples / instructions to run this program:

e.g.: 

In separate shell windows:

> java JokeServer
> java JokeClient
> java AdminClient

All acceptable commands are displayed on the various consoles.

5. List of files needed for running the program.

 a. checklist.html
 b. JokeServer.java
 c. JokeClient.java

5. Notes:

e.g.:

The jokes and proverbs may repeat after five have been printed.
The shutdown command sometimes doesn't shutdown the server. Killing
the process in the command prompt window will close the server.

----------------------------------------------------------*/

/**
 * JokeServer class
 * The JokeServer has 3 modes: Joke-Mode, Proverb-Mode and Maintenance-Mode.
 * 
 * The JokeServer uses a HashMap that uses a UUID passed in by the client to track
 * a "State" class. The State class contains the name 
 * passed by the client, the ArrayList of jokes and the ArrayList of proverbs.
 * 
 * This JokeServer uses a separate thread to listen for and communicate with the
 * AdminClient.
 * @author Adam Podraza
 *
 */

//class JokeServer
public class JokeServer {
	//Boolean that tests and sets whether the server is in joke-mode or proverb-mode.
	private static boolean isJokeMode = true;
	
	//Boolean that tests and sets whether the server is in maintenance-mode.
	private static boolean isAdminMode = false;
	
	//HashMap container that tracks client state's by UUID passed.
	static HashMap<String, Worker.State> clientTracker = new HashMap<String, Worker.State> ();
	
	
	
	
	
	/**
	 * setJokeMode() sets the boolean field 
	 * that determines whether the server is in
	 * joke-mode or proverb-mode.
	 * @param t
	 */
	static void setJokeMode(boolean t) {
		isJokeMode = t;
	}
	
	/**
	 * getJokeMode() gets the boolean field
	 * that determines whether the server is in
	 * joke-mode or proverb-mode.
	 * @return isJokeMode
	 */
	static boolean getJokeMode() {
		return isJokeMode;
	}
	
	/**
	 * setAdminMode() sets the boolean field
	 * that determines whether the server is in
	 * maintenance-mode.
	 * @param t
	 */
	static void setAdminMode(boolean t) {
		isAdminMode = t;
	}
	
	/**
	 * getAdminMode() gets the boolean field
	 * that determines whether the server is in
	 * maintenance-mode.
	 * @return isAdminMode
	 */
	static boolean getAdminMode() {
		return isAdminMode;
	}
	
	//boolean determines whether loop continues to run,
	//value upon initialization is true
	public static boolean controlSwitch = true;
	
	public static void main(String [] args) throws IOException {
		//populate ArrayLists
		
		
		//max number of arguments
		int q_len = 6;
		//specified port
		int port = 5418;
		
		//This variable will be assigned the client connection
		Socket sock;
		
		//The following three lines of code are taken from the class notes.
		//The AdminLooper class runs a loop on a separate thread that listens
		//for an AdminClient connection.
		AdminLooper al = new AdminLooper();
		Thread t = new Thread(al);
		t.start();
		
		//create new ServerSocket and initialize with port and q_len variables
		ServerSocket servsock = new ServerSocket(port, q_len);
		
		//Print out line of text specifying whose server and starting up
		System.out.println("Adam Podraza's Joke Server starting up,"
				+ " listening at port " + port + " \n");
		
		//While the controlSwitch boolean is true, this while loop
		//runs and continuously accepts client connections.
		//For each new client connection, a new worker thread is created
		//to handle it.
		while (controlSwitch) {
			//System.out.println("In the default loop.");
			//local variable sock receives client connection
			sock = servsock.accept();
			
			//new worker thread is spawned to handle client requests.
			if (controlSwitch) new Worker(sock).start();
		}
		//close server's connection when controlSwitch is no longer true.
		servsock.close();
	}
		
	
}

/**
 * AdminLooper class.
 * This class is created in the main method of
 * the JokeServer. It implements the runnable interface.
 * The AdminLooper is started and run to listen for connecting
 * AdminClients.
 * 
 * @author Adam Podraza
 *
 */
class AdminLooper implements Runnable {
	//boolean to break out of Admin listening loop.
	public static boolean adminControlSwitch = true;

	//Required method for implementing Runnable interface.
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		//max number of arguments
		int q_len = 6;
		//default port
		int port = 5419;
		//Declaration of Socket to connect to AdminClient
		Socket sock;
		
		try {
			//Initialize serversocket to connect with AdminClient
			ServerSocket servSock = new ServerSocket(port, q_len);
			
			while (adminControlSwitch) {
				//Accept new connections with AdminClients
				sock = servSock.accept();
				
				//Spawn new threads to work with AdminClients
				new AdminWorker (sock).start();
			}
			JokeServer.controlSwitch = false;
			//close connection if adminControlSwitch is no longer true.
			servSock.close();
		} catch (IOException ioe) {System.out.println(ioe);}
		
		
	}
	
}

/**
 * AdminWorker class.
 * Executes instructions communicated by AdminClient.
 * 
 * @author Adam Podraza
 *
 */

class AdminWorker extends Thread {
	
	
	//declaration of local socket.
	static Socket sock;
	//declaration of local String command.
	String command;
	
	//Constructor that takes a Socket as a parameter and assigns
	//to sock
	AdminWorker(Socket s) {
		sock = s;
	}
	
	public void run() {
		
		//Declarations of new PrintStream and BufferedReader
		//to communicate with AdminClient.
		PrintStream out = null;
		BufferedReader in = null;
		
		try {
			FileWriter adminFile = new FileWriter("output.txt", true);
			BufferedWriter adminWrite = new BufferedWriter(adminFile);
			
			//Initialize in and out to sock's input and output streams
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintStream(sock.getOutputStream());
			
			//Test to ensure JokeServer's adminControlSwitch is still true.
			if (AdminLooper.adminControlSwitch != true) {
				System.out.println
				("JokeServer is now shutting down as per client request.");
				out.println("Server is now shutting down. Goodbye!");
			}
			
			else try {
				//Read in command from client.
				command = in.readLine();
				//Print command to server screen.
				System.out.println("Received " + command + " command.");
				adminWrite.write("AdminClient sent " + command + " command\n");
				
				//set to joke-mode
				if (command.indexOf("joke-mode") > -1) {
					JokeServer.setJokeMode(true);
					out.println("Set server to joke mode");
				}
			
				//set to proverb-mode
				if (command.indexOf("proverb-mode") > -1) {
					JokeServer.setJokeMode(false);
					out.println("Set server to proverb mode.");
				}
				
				//set to maintenance-mode
				if (command.indexOf("maintenance-mode") > -1) {
					JokeServer.setAdminMode(true);
					out.println("Set server to maintenance mode.");
				}
				
				//set to default-mode (joke-mode)
				if (command.indexOf("default-mode") > -1) {
					JokeServer.setAdminMode(false);
					JokeServer.setJokeMode(true);
				}
			} catch (IOException x) {
				System.out.println("System read error");
				x.printStackTrace();
			}
			adminWrite.close();
			sock.close();
		} catch (IOException x) { 
			System.out.println("System read error"); 
			x.printStackTrace();
		}
	}
}

/**
 * Worker class executes instructions passed by JokeClient
 * 
 * @author Adam Podraza
 *
 */
class Worker extends Thread {
	FileWriter clientFile;
	BufferedWriter clientWrite;
	//Local string declaration, will receive UUID from client.
	String id;
	//Local declaration of a socket.
	Socket sock;
	 //Local declaration of joke and proverb ArrayLists
	static ArrayList<String> _jokes;
	static ArrayList<String> _proverbs;
	//Local declaration of name passed from client
	String name;
	//State class that will track the state of the thread
	State state;
	//command will receive and store commands passed by clients
	String command;
	
	//Worker constructor assigns Socket argument to local field and
	//initializes ArrayLists
	Worker (Socket s) { 
		sock = s;
		populate();
		
	}
	
	/**
	 * populate() method initializes and populates the
	 * joke and proverb ArrayList fields with the jokes 
	 * and the proverbs.
	 */
	static void populate() {
		_jokes = new ArrayList<String> ();
		_proverbs = new ArrayList<String> ();
		for (int i = 0; i < jokeArray.length; i++) {
			_jokes.add(jokeArray[i]);
		}
		
		for (int i = 0; i < proverbArray.length; i++) {
			_proverbs.add(proverbArray[i]);
		}
		
	}
	
	
	//ArrayList container that will contain the jokes.
		static ArrayList<String> jokes;
		
		//Jokes taken from http://theoatmeal.com/djtaf/
		static String[] jokeArray = new String[] {
			"A) Why do milking stools only have three legs? Because Xname's cow's got the udder!",
			"B) Xname, what kind of guns to bees use? BeeBee guns.",
			"C) Why does Xname's Moon-rock taste better than an Earth-rock? Because it's a little meteor.",
			"D) Xname, A baby seal walks into a club...",
			"E) What do Xname's cats eat for breakfast? Mice Krispies!",
		};
		
		//ArrayList container that will contain the proverbs.
		static ArrayList<String> proverbs;
		
		
		static String [] proverbArray = new String[] {
			"A) A bird in Xname's hand is worth two in the bush!",
			"B) Birds of a Xnames's feather flock together.",
			"C) Xname, Carpe Diem.",
			"D) Failing to plan for Xname is planning to fail.",
			"E) Good things come to Xname when Xname waits.",
		};
	
		
	public void run() {
		
		
		//out is local reference to PrintStream provided by argument Socket, not assigned yet
		PrintStream out = null;
		//in is local reference to InputStreamReader provided by argument Socket, not assigned yet
		BufferedReader in = null;
		try {
			
			//assign in to BufferedReader casted InputStream provided by argument passed through constructor
			in = new BufferedReader
					(new InputStreamReader(sock.getInputStream()));
			//assign out to PrintStream casted OutputStream provided by argument passed through constructor
			out = new PrintStream(sock.getOutputStream());
			
			//assign local reference id to UUID passed in from the client.
			id = in.readLine();
			System.out.println("Received connection from " + id);
			
			clientFile = new FileWriter("output.txt", true);
			clientWrite = new BufferedWriter(clientFile);
			
			//if client types "shutdown" server will shutdown.
			//this case prints a shutdown message.
			if(JokeServer.controlSwitch != true) {
				System.out.println
				("Listener is now shutting down as per client request.");
				out.println("Server is now shutting down. Goodbye!");
				//sock.close();
			}
			
			//Test to see if JokeServer is in AdminMode. Print message if true.
			else if(JokeServer.getAdminMode() == true) {
				out.println("The server is temporarily unavailable -- check-back shortly.");
				clientWrite.write("Client " + name + " received: " + "The server is temporarily unavailable -- check-back shortly.\n");
				
			}
			
			else try {
				//If this is a new client connection a new state object is created
				//to store the threads state.
				if (JokeServer.clientTracker.get(id) == null) {
					System.out.println("new client");
					name = in.readLine();
					state = new State(name, _jokes, _proverbs);
					JokeServer.clientTracker.put(id, state);
					
				} 
				//If this is not a new client connection, a state object is retrieved 
				//and this thread's fields are set to the state object's.
				else {
					System.out.println("old client");
					state = JokeServer.clientTracker.get(id);
					name = state.getName();
					_jokes = state.getJokes();
					_proverbs = state.getProverbs();
					
				}
				
				//Read in new command from client. Just used for recieving a shutdown
				//command.
				command = in.readLine();
				//shutdown case
				//if the client passed String equals the word "shutdown"
				//the server will set controlSwitch to false and begin shutting down
					if (command.indexOf("shutdown") > -1) {
						JokeServer.controlSwitch = false;
						System.out.println("Worker has captured a shutdown request.");
						out.println("Shutdown request has been noted by worker.");
						out.println("Please send final shutdown request to listener.");
					}
				
					//primary use case
					//worker will call printResponse to return either joke or proverb.
					else {
						//out.print(JokeServer.greet + name + ". ");
						printResponse(name, out, clientWrite);
					}
					
			} catch (IOException x) {
				System.out.println("Server read error");
				x.printStackTrace();
			}
			
			//close connection on shutdown
			clientWrite.close();
			sock.close();
		} catch (IOException ioe) {System.out.println(ioe);}
	}
	
	/**
	 * parseResponse() works with printResponse() to take care of
	 * the business logic.
	 * This method checks if the server is in joke-mode or proverb-mode
	 * and returns a random String object.
	 * parseResponse() also removes the String from the specific ArrayList
	 * so that dupes will not be returned.
	 * Finally, parseResponse() removes the placeholder "Xname" and replaces
	 * it with the user's name.
	 * 
	 * @return String
	 */
	
	private String parseResponse(BufferedWriter clientWrite) {
		
		if (JokeServer.getJokeMode()) {
			
			//random number generation.
			Random rn = new Random();
			int n = _jokes.size();
			int i = rn.nextInt(n);
			//get random string with random number.
			String result = _jokes.get(i);
			//remove string from joke ArrayList
			_jokes.remove(result);
			//replace "Xname" with user's name.
			String j = result.replaceAll("Xname", name);
			try {
				clientWrite.write("Client " + name + " received: " + j + "\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return j;
		} else {
			
			//random number generation
			Random rn = new Random();
			int n = _proverbs.size();
			int i = rn.nextInt(n);
			//get random string with random number
			String result = _proverbs.get(i);
			//remove string from proverb ArrayList
			_proverbs.remove(result);
			//replace "Xname" with user's name.
			String j = result.replaceAll("Xname", name);
			try {
				clientWrite.write("Client " + name + " received: " + j + "\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return j;
		}
	}
	
	/**
	 * updateState()
	 * This method updates the thread's state object and stores it
	 * in the JokeServer's HashMap.
	 */
	void updateState() {
		state.setJokes(_jokes);
		state.setProverbs(_proverbs);
		state.setName(name);
		JokeServer.clientTracker.put(id, state);
	}
	
	/**
	 * printResponse()
	 * This method tests whether the server is in joke-mode or proverb-mode.
	 * The method calls parseResponse() and prints the String returned to the client.
	 * Then, the method updates the state with updateState().
	 * 
	 * @param String name
	 * @param PrintStream out
	 */
	
	void printResponse(String name, PrintStream out, BufferedWriter clientWrite) {
		
		//case if server is in joke-mode and has more jokes to print.
		if (JokeServer.getJokeMode() == true && _jokes.size() != 0) {
			out.println(parseResponse(clientWrite));
			updateState();
		
		}
		//case if server is in proverb-mode and has more proverbs to print.
		else if(JokeServer.getJokeMode() == false && _proverbs.size() != 0) {
			
			out.println(parseResponse(clientWrite));
			updateState();
			
		}
		//case if server is in joke-mode and has no more jokes to print.
		else if(JokeServer.getJokeMode() == true && _jokes.size() == 0) {
			//repopulate joke ArrayList with jokes.
			for (int i = 0; i < jokeArray.length; i++) {
				_jokes.add(jokeArray[i]);
			}
			System.out.println("repopulated jokes");
			out.println(parseResponse(clientWrite));
			updateState();
		}
		//case if server is in proverb-mode and has no more proverbs to print.
		else if(JokeServer.getJokeMode() == false && _proverbs.size() == 0) {
			//repopulate proverb ArrayList with proverbs.
			for (int i = 0; i < proverbArray.length; i++) {
				_proverbs.add(proverbArray[i]);
			}
			System.out.println("repopulated proverbs");
			out.println(parseResponse(clientWrite));
			updateState();
		}
			
	}
	
	
	/**
	 * State class contains all the fields necessary to track and maintain state.
	 * @author Adam Podraza
	 *
	 */
	class State {
		//Client's name
		private String name;
		//Client's joke ArrayList
		private ArrayList<String> jokes;
		//Client's proverb ArrayList
		private ArrayList<String> proverbs;
		
		State(String name, ArrayList<String> jokes, ArrayList<String> proverbs) {
			this.name = name;
			this.jokes = jokes;
			this.proverbs = proverbs;
		}
		
		//Getters and setters

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public ArrayList<String> getJokes() {
			return jokes;
		}

		public void setJokes(ArrayList<String> jokes) {
			this.jokes = jokes;
		}

		public ArrayList<String> getProverbs() {
			return proverbs;
		}

		public void setProverbs(ArrayList<String> proverbs) {
			this.proverbs = proverbs;
		}
	}
}
