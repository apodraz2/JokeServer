
//referenced libraries
import java.io.*;
import java.net.*;
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
 * JokeClient class
 * Connects to JokeServer to receive
 * jokes and proverbs.
 * 
 * @author Adam Podraza
 *
 */

public class JokeClient {
	//UUID identifier for client
	static UUID id;
	//String that client passes to JokeServer of user's name
	static String name;
	
	public static void main(String [] args) {
		//initializes UUID
		id = UUID.randomUUID(); 
		
		//string to specify name of the server
		String serverName;
		
		//if no arguments are passed, serverName is assigned default
		//"localhost"
		if(args.length < 1) serverName = "localhost";
		//otherwise, serverName is assigned the first argument
		else serverName = args[0];
		
		//Adam Podraza's Joke Client is printed
		System.out.println("Adam Podraza's Joke Client, 1.7.\n");
		
		//print out serverName and the current port
		System.out.println("Using server: " + serverName + ", Port: 5418");
		
		//new reader is created to read user input from command line
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		
		try {
			//prompt for user to enter name
			System.out.println("Please enter your name: ");
			System.out.flush();
			//get name from command prompt
			name = in.readLine();
			String command;
			do {
				//prompts for user input
				System.out.println("Please type ok to receive your jokes or quit to end the session. ");
				//clears out
				System.out.flush();
				//reads in host name or ip address
				command = in.readLine();
				//if user types "quit" the client will close
				if (command.indexOf("quit") < 0)
					//otherwise, client uses getJokes method
					//to connect with the server and receive jokes.
					getJokes(name, serverName);
				if (command.indexOf("shutdown") > -1)
					getJokes("shutdown", serverName);
				
		} while (command.indexOf("quit") < 0);
		System.out.println ("Cancelled by user request.");
	} catch (IOException x) { x.printStackTrace();}
	}

	//This method connects the client to the server at port 5418
	private static void getJokes(String name, String serverName) {
		// TODO Auto-generated method stub
		//reference to connection to server
		Socket sock;
		
		//inputs and outputs for connection to server.
		BufferedReader fromServer; 
		PrintStream toServer; 
		String textFromServer; 
		 
		try{ 
		 //connects to server using port number 
		 sock = new Socket(serverName, 5418); 
		 
		 //Initializes reader to read from server
		 fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream())); 
		 
		 //initializes PrintStream to write to server
		 toServer = new PrintStream(sock.getOutputStream()); 
		 
		 //Shutdown case
		 if (name.indexOf("shutdown") > -1) {
			 //print UUID to server
			 toServer.println(id);
			 toServer.flush();
			 
			 //print shutdown to server
			 toServer.println(name);
			 toServer.flush();
			 
			 //print shutdown to server again
			 toServer.println(name);
			 toServer.flush();
			 
			//Wait for and read response from server
			 for (int i = 1; i <=3; i++){ 
				 textFromServer = fromServer.readLine(); 
			 //if server does not provide response, print null
			 if (textFromServer != null) System.out.println(textFromServer);
			 }
		 }
			 else {
				 //print UUID to server
				 toServer.println(id);
				 toServer.flush();
		 
				 //print name to Server
				 toServer.println(name); 
				 toServer.flush(); 
		 
				 //print command to Server
				 toServer.println(name);
				 toServer.flush();
		 
				 //Wait for and read response from server
				 for (int i = 1; i <=3; i++){ 
					 textFromServer = fromServer.readLine(); 
					 //if server does not provide response, print null
					 if (textFromServer != null) System.out.println(textFromServer); 
				 }	 
		 }
		 //close connection to server
		 sock.close(); 
		 } catch (IOException x) { 
		 System.out.println ("Socket error."); 
		 x.printStackTrace ();
	
		 }
	}	
	}

/**
 * AdminClient class
 * The AdminClient communicates with the server regarding the
 * administrative functions (setting joke-mode and maintenance-mode).
 * 
 * @author Adam Podraza
 *
 */
class AdminClient {
	
	public static void main (String[] args) {
		//local fields
		String serverName;
		String command;
		Socket sock;
		PrintStream toServer;
		BufferedReader fromServer;
		String textFromServer;
		
		if (args.length < 1) serverName = "localhost";
		else serverName = args[0];
		
		System.out.println("Adam Podraza's JokeAdmin, 1.7.\n");
		
		System.out.println("Using Server: " + serverName + ", at port: 5419");
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		try{
			
			System.out.flush();
			
			do {
				//Prompt for user input
				System.out.println("Please enter your command: ");
				//clears out
				System.out.flush();
				//reads in host name or ip address
				command = in.readLine();
				//if user types "quit" the client will close
				if (command.indexOf("quit") < 0)
					//otherwise, client uses getRemoteAddress method
					//to connect with the server about the hostname the user passed
					sendCommand(command, serverName);
		} while (command.indexOf("quit") < 0);
		System.out.println ("Cancelled by user request.");
	} catch (IOException x) { x.printStackTrace();}
	}
	
	/**
	 * Sends user command to server
	 * @param String command
	 * @param String serverName
	 */
	private static void sendCommand(String command, String serverName) {
		// TODO Auto-generated method stub
		//reference to connection to server
		Socket sock;
		
		//connections to server
		BufferedReader fromServer; 
		PrintStream toServer; 
		String textFromServer; 
		 
		try{ 
		 //connects to server using port number 5419
		 sock = new Socket(serverName, 5419); 
		 
		 //Initializes reader to read from server
		 fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream())); 
		 
		 //initializes PrintStream to write to server
		 toServer = new PrintStream(sock.getOutputStream()); 
		 
		 //sends information to server
		 toServer.println(command); 
		 toServer.flush(); 
		 
		 //Wait for and read response from server
		 for (int i = 1; i <=3; i++){ 
			 textFromServer = fromServer.readLine(); 
		 //if server does not provide response, print null
		 if (textFromServer != null) System.out.println(textFromServer); 
		 } 
		 
		 //close connection to server
		 sock.close(); 
		 } catch (IOException x) { 
		 System.out.println ("Socket error."); 
		 x.printStackTrace ();
	
		 }
	}
}

