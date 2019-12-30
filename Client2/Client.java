import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class Client {
	Socket requestSocket;           //socket connect to the server
	ObjectOutputStream out;         //stream write to the socket
 	ObjectInputStream in;          //stream read from the socket
	String message;                //message send to the server
	String MESSAGE;                //capitalized message read from the server
	String URL;
	int port;

	public Client(String URL, int port) {
		this.URL = URL;
		this.port = port;
	}
	
	public void sendFileToServer(String fileName) {
    	
		try {
    		File file = new File(fileName);
    		if(file.exists()) {

                byte[] byteArray = new byte[(int) file.length()];

                FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis);
                DataInputStream dis = new DataInputStream(bis);
                
                dis.readFully(byteArray, 0, byteArray.length);
                
                DataOutputStream dos = new DataOutputStream(out); 
                
                dos.writeLong(byteArray.length);
                dos.write(byteArray, 0, byteArray.length);
                dos.flush();
        		dis.close();
        		
        		System.out.println("\nFile: "+'"'+fileName+'"'+" sent successfully to server!");
    			
    		}
    		else {
    			System.out.println("\nFile: "+ '"' + fileName + '"' +" not present!");
    			DataOutputStream dos = new DataOutputStream(out);
    			dos.writeLong(0);
    			dos.flush();
    		}
    	}
		
    	catch(Exception e) {
    		e.printStackTrace();
    	}
		

    }
	
	public void receiveFileFromServer(String fileName) {
		
		try {
			int bytesRead;
	        
			DataInputStream dis = new DataInputStream(in);
	        
	        
	        long size = dis.readLong();
	        
	        if(size == 0) {
	        	System.out.println("\nSorry, there is no file with name: "+ '"' + fileName + '"' + " on the server :(");
	        	return;
	        }

	        byte[] buffer = new byte[1024];
	        
	        OutputStream fos = new FileOutputStream(fileName);
	        
	        while (size > 0 && (bytesRead = dis.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
	            fos.write(buffer, 0, bytesRead);
	            size -= bytesRead;
	        }
	        System.out.println("\nReceived File: " + '"' +fileName+ '"' + " from server");   
	        fos.close();

		}
		catch(Exception e) {
			
			e.printStackTrace();
			
		}
		
	}
    
	void invalidCommandInstructions() {
		System.out.println("\nInvalid Command!, Valid commands are: \n"
				+ "                                         ************************************************************\n"
				+ "                                                              1) dir\n"
				+ "                                                              2) get <filename>\n"
				+ "                                                              3) upload <filename>\n\n "
				+ "                                        *************************************************************\n");
	}

	void run()
	{
		try {
			//Authenticate Users with username and password (For now let username and password be equal)
			while(true) {
				System.out.print("\nUsername: ");
				String username, password;
				Console console=System.console();
				Scanner myObj = new Scanner(System.in);
				username = myObj.nextLine();
				
				char[] pass_word;
				pass_word = console.readPassword("\nPassword: ");
				password = new String(pass_word);
				if(!(username.equals(password))) {
					System.out.println("\nAuthentication Failed!");
				}
				else
					break;
			}

			//create a socket to connect to the server
			requestSocket = new Socket(URL, port);
			System.out.println("\nConnected to localhost in port 8000");
			//initialize inputStream and outputStream
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			out.flush();
			in = new ObjectInputStream(requestSocket.getInputStream());
						
			
			//get Input from standard input
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
			while(true)
			{
					System.out.println();
					//read a sentence from the standard input
					System.out.print("Client->");
					
		
					message = bufferedReader.readLine();
					message = message.trim();
					if(message.length() == 0) {
						invalidCommandInstructions();
						continue;
					}
						
					String[] messageArray = message.split(" ");
					if(messageArray[0].equals("dir") && messageArray.length != 1) {
						invalidCommandInstructions();
						continue;
					}
					if((messageArray[0].equals("get") || messageArray[0].equals("upload")) && messageArray.length != 2) {
						invalidCommandInstructions();
						continue;
					}
						
					switch(messageArray[0]) {
						case "dir":
							sendMessage(message);
							String [] listOfContents = (String []) in.readObject();
							System.out.println();
							System.out.println("************************* There are "+ listOfContents.length + " files on the server **********************");
							int count = 1;
							for(String content : listOfContents) {
								System.out.println(count + ") "+content);
								count++;
							}
							
							break;
						case "get":
							sendMessage(message);
							String fileName = messageArray[1];
							receiveFileFromServer(fileName);				            
						
							break;
						case "upload":
							sendMessage(message);
							fileName = messageArray[1];
							sendFileToServer(fileName);
							break;
						default:
							invalidCommandInstructions();
					}
						
					
					/*System.out.println("Enter message: ");
					message = bufferedReader.readLine();
					//Send the sentence to the server
					sendMessage(message);
					//Receive the upperCase sentence from the server
					MESSAGE = (String)in.readObject();
					//show the message to the user
					System.out.println("Receive message: " + MESSAGE);*/
					
				
			}
		}
		catch (ConnectException e) {
    			System.err.println("Connection refused. You need to initiate a server first.");
		} 
		catch ( ClassNotFoundException e ) {
            		System.err.println("Class not found");
        	} 
		catch(UnknownHostException unknownHost){
			System.err.println("You are trying to connect to an unknown host!");
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
		finally{
			//Close connections
			try{
				in.close();
				out.close();
				requestSocket.close();
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			}
		}
	}
	//send a message to the output stream
	void sendMessage(String msg)
	{
		try{
			//out.reset();
			//stream write the message
			out.writeObject(msg);
			out.flush();
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
	//main method
	public static void main(String args[])
	{
		int noOfArgs = args.length;
		if(noOfArgs != 2) {
			System.out.println("\nInsufficient number of Arguments given, Please provide a URL and a port to establish a connection to Server!");
			System.out.println("\n Example: java Client <URL> <port>");
			return;
		}
		else {
			if(!(args[0].equals("localhost")) || (Integer.parseInt(args[1]) != 8000))
			{
				System.out.println("\nThere is no server listening at the address: "+ args[0] +" port: "+args[1]);
				System.out.println("\nTry Again!");
				return;
			}
			else {
				Client client = new Client(args[0], Integer.parseInt(args[1]));
				client.run();
			}
		}
			
		
	}

}
