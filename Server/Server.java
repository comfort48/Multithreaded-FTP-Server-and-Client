import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.*;
import java.util.*;

public class Server {

	private static final int sPort = 8000;   //The server will be listening on this port number
	static Path path = FileSystems.getDefault().getPath(".").toAbsolutePath(); // The path where the Server.java file is located
	static File directory = new File(path.toString()); // Converting path to a directory File path.

	public static void main(String[] args) throws Exception {
		System.out.println("\nThe server is running."); 
        	ServerSocket listener = new ServerSocket(sPort);
		int clientNum = 1;
        	try {
            		while(true) {
                		new Handler(listener.accept(),clientNum).start();
				System.out.println("\nClient "  + clientNum + " is connected!");
				clientNum++;
            			}
        	} finally {
            		listener.close();
        	} 
 
    	}

	/**
     	* A handler thread class.  Handlers are spawned from the listening
     	* loop and are responsible for dealing with a single client's requests.
     	*/
    	private static class Handler extends Thread {
        	private String message;    //message received from the client
		   
		private Socket connection;
        	private ObjectInputStream in;	//stream read from the socket
        	private ObjectOutputStream out;    //stream write to the socket
		private int no;		//The index number of the client

        public Handler(Socket connection, int no) {
    		this.connection = connection;
    		this.no = no;
        }
        
        public void sendContentsOfDirectory(File directory) {
        	try {
        		if(directory.exists()) {
					out.writeObject(directory.list());
					out.flush();
				}
			} 
        	catch (Exception e) {
				e.printStackTrace();
			}
        }
        
        public void sendFileToClient(String fileName) {
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
            		
            		System.out.println("\nFile: "+'"'+fileName+'"'+" sent successfully to client: "+ no + "!");
        			
        		}
        		else {
        			System.out.println("\nUnknown file: "+ '"' +fileName + '"' +" requested from client: "+ no + "!");
        			DataOutputStream dos = new DataOutputStream(out);
        			dos.writeLong(0);
        			dos.flush();
        		}
        	}
        	catch(Exception e) {
        		e.printStackTrace();
        	}
    		
    		
    		
    		
        }
        
        public void receiveFileFromClient(String fileName) {
        	try {
        		
        		int bytesRead = 0;
	            DataInputStream dis = new DataInputStream(in);
	            
	            long size = dis.readLong();
	            
	            if(size == 0) {
		        	System.out.println("\nClient "+ no + " - tried to upload a file: "+ '"' + fileName + '"'+ " but failed :(");
		        	return;
		        }

	            byte[] buffer = new byte[1024];
	            OutputStream fos = new FileOutputStream(fileName);
	           
	            while (size > 0 && (bytesRead = dis.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
	                fos.write(buffer, 0, bytesRead);
	                size -= bytesRead;
	            }
	            
	            System.out.println("\nReceived File: " + '"' +fileName +'"' + " from client " + no);
	            fos.close();
        	}
        	catch(Exception e) {
        		e.printStackTrace();
        	}
        }
        
        	
        	

        public void run() {
 		try{
			//initialize Input and Output streams
			out = new ObjectOutputStream(connection.getOutputStream());
			out.flush();
			in = new ObjectInputStream(connection.getInputStream());
			try{
				while(true)
				{
					//receive the message sent from the client
					message = (String)in.readObject();
					String messageArray[] = message.split(" ");
					String fileName = "";
					
					switch(messageArray[0]) {
						case "dir":
							sendContentsOfDirectory(directory);
							break;
						case "get":
							fileName = messageArray[1];
							sendFileToClient(fileName);
							break;
						case "upload":
							fileName = messageArray[1];
							receiveFileFromClient(fileName);
							break;
							
							
						
					}
					
					
				}
			}
			catch(ClassNotFoundException classnot){
					System.err.println("\nData received in unknown format\n");
				}
		}
		catch(IOException ioException){
			System.out.println("\nDisconnect with Client " + no);
		}
		finally{
			//Close connections
			try{
				in.close();
				out.close();
				connection.close();
			}
			catch(IOException ioException){
				System.out.println("\nDisconnect with Client " + no);
			}
		}
	}

	//send a message to the output stream
	public void sendMessage(String msg)
	{
		try{
			//out.reset();
			out.writeObject(msg);
			out.flush();
			System.out.println("\nSend message: \n\n" + msg + " \nto Client " + no);
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}

    }

}
