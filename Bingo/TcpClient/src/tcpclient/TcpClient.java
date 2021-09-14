/*
 *Hannah Edwards
 *460
 *Bingo
 */
package tcpclient;

import java.io.*;
import java.net.*;

public class TcpClient {
    public static void main(String[] args) throws IOException {
        //here to make it run for testing
        String[] a = {"111.11.1111.1111"};//Enter an IP address
        args = a;
        
        Socket echoSocket = null;
        PrintWriter out = null;

        try {
            echoSocket = new Socket(args[0], 12345);
            out = new PrintWriter(echoSocket.getOutputStream(), true);

        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: " + args[0]);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: " + args[0] );
            System.exit(1);
        }
        
        //SUSCESSFUL    
        TcpClientDataThread inThread = new TcpClientDataThread(echoSocket);
        inThread.start();

        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String userInput;

        // get the username
        System.out.print("Type your username: ");
        String username = stdIn.readLine();

        // send data without CR/LF
        OutputStream rawout = echoSocket.getOutputStream();

        byte[] b = new byte[200];
        //talk, /card /fill /next /bingo /quit /START
        System.out.println("Type anything to talk with other players");
        System.out.println("Type /card to view your card "
                + "\nType /fill row collum if you have a space (Ex: /fill 3 6)"
                + "\nTyep /next if you do not have the called number"
                + "\nType /bingo if you have bingo"
                + "\nType /quit to quit"
                + "\nType /START when ready");
        
        // process one line of text
        while ( true ) {
            userInput = stdIn.readLine();
            
            //quit program
            if ( userInput == null || userInput.equals("/quit") ) {
                // tell server we are quitting
                String quitMessage = "/quit " + username;
                rawout.write(quitMessage.getBytes());
                break;
            }
            
            //start game
            else if(userInput.equals("/START")){
                String startMessage = "/START " + username;
                rawout.write(startMessage.getBytes());
            }          
            
            //get card
            else if(userInput.equals("/card")){ 
                String cardMessage = "/card " + username;
                rawout.write(cardMessage.getBytes());
            } 
            
            //get bingo
            else if(userInput.equals("/bingo")){
                String bingoMessage = "/bingo " + username;
                rawout.write(bingoMessage.getBytes());
            }
            
            //fill card
            else if(userInput.length() > 4 && userInput.substring(0,5).equals("/fill")){
                //input validation?
                String fillMessage = userInput + " " + username;
                rawout.write(fillMessage.getBytes());
            }
            
            else if(userInput.equals("/next")){
                String bingoMessage = "/next " + username;
                rawout.write(bingoMessage.getBytes());
            }
            
            else{//if they didn't enter a command
                // create message with username and text
                String message = username + ": " + userInput;
                // send it to the server (no newline at end)
                rawout.write( message.getBytes() );
            }
        }
        stdIn.close();

        // wait for input thread to stop
        try {
                // wait for any incoming messages
                inThread.join();

                // close the socket
                echoSocket.close();
        } catch ( InterruptedException e ) {
                // ignore
        }
    }    
}

// handle data coming in from the server
class TcpClientDataThread extends Thread {
	Socket echoSocket = null;
	InputStream rawin;
	
	public TcpClientDataThread( Socket s ) {
		// get the socket and corresponding input stream
		echoSocket = s;
		try {
			rawin = echoSocket.getInputStream();
		} catch ( IOException e ) {
			System.out.println("i/O exception on socket");
		}
	}
	
	public void run() {
		byte[] b = new byte[200];
		
		while( true ) {
			try {
                            // get a message from the server
                            int rlen = rawin.read( b, 0, 200 );

                            // make it into a string
                            String s = new String( b, 0, rlen );

                            // is it a quit confirmation?
                            if ( s.equals("/quit") ) {
                                    // yes, confirming quit
                                    echoSocket.close();
                                    return;
                            }
                            
                            // display the message
                            System.out.println(s);
			} catch ( IOException e ) {
				System.out.println("I/O exception on socket");
				return;
			}
		}
	}
}


