/*
 *Hannah Edwards
 *460
 *Bingo
 */
package server;

import java.net.*;
import java.io.*;
import java.lang.Thread;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.Timer;
import java.io.IOException;


/**
 *
 * @author hsedw
 */
public class Server {

    
    public static void main(String[] args) throws IOException {
        //this is here to make it run for testing
        String[] a = {"12345"};
        args = a;
        
        ServerSocket serverSocket = null;
        boolean listening = true;        
        ChatRoomInfo cri = new ChatRoomInfo();

        // verify we have a port #
        if ( args.length != 1 ) {
                System.out.println("Error: No port # provided!");
                System.exit(0);
        }

        // display the IP address(es) of the server
        Enumeration e = NetworkInterface.getNetworkInterfaces();
        while(e.hasMoreElements())
        {
            NetworkInterface n = (NetworkInterface) e.nextElement();
            Enumeration ee = n.getInetAddresses();
            while (ee.hasMoreElements())
            {
                InetAddress i = (InetAddress) ee.nextElement();
                // don't worry about link local or loopback addrs
                if ( i.isLinkLocalAddress() || i.isLoopbackAddress() )
                    continue;
                System.out.println( "Local IP Addr: " + i.getHostAddress());
            }
        }
        
        //try to connect to port
        try {
            serverSocket = new ServerSocket(12345);
        } catch (IOException ex) {
            System.err.println("Could not listen on port 12345" );
            System.exit(-1);
        }

        while (listening){
            // start a client thread
            new ChatClient(serverSocket.accept(), cri).start();

            // debugging output
            System.out.println("Started new thread");
        }
        serverSocket.close();
    }
    
}

class ChatRoomInfo {    
    Game game = new Game();
    
    ArrayList<ChatClient> clientList = new ArrayList<>();

    // add a new client
    public synchronized void newClient( ChatClient c ) {
        System.out.println("Adding client");
        clientList.add(c);        
        if(game.getGameInProgress()){
            c.notifyClient("\nPlease wait until next game to play");
        }
    }

    // handle a client who is quitting
    public synchronized void removeClient( ChatClient c ) {
        // remove the client object from the list of clients
        clientList.remove( c );
    }
    
    //send a message to everyone
    public synchronized void notifyAll( String message ) {
        System.out.println("Sending to " + clientList.size() + " clients" );
        for( int i=0; i<clientList.size(); i++ ) {
                clientList.get(i).notifyClient( message );
        }
    }
    
    //start or end game
    //default is false (no game being played)
    void updateGameProgress(boolean b){
        game.setGameInProgress(b);
    }
    
    //generate new cards for new game
    void updateCards(){
        System.out.println("Updatating " + clientList.size() + " cards" );
        for( int i=0; i<clientList.size(); i++ ) {
                clientList.get(i).player.createCard();
        }
    }
}

class ChatClient extends Thread {
    ChatRoomInfo chatRoom;
    private Socket socket = null;
    InputStream in;
    OutputStream out;
    
    Player player;   //start them as a player
     

    public ChatClient(Socket s, ChatRoomInfo cr) {
            socket = s;
            chatRoom = cr;
            player = new Player();
    }

    // send a message to this client
    public void notifyClient(String message) {
        try {
            // send it to the client
            byte[] userdata = message.getBytes();
            out.write( userdata );
        } catch (IOException e) {
                closeClient();
        }
    }

    // close this client
    public void closeClient() {
        try {
                // forget this client
                socket.close();
                chatRoom.removeClient(this);
        } catch (IOException e) {
                // ignore now
        }
    }
	
    // handle incoming data from this client
    public void run() {
        try {
            in = socket.getInputStream();
            out = socket.getOutputStream();

            // ready to handle messages, so register with the chat room
            chatRoom.newClient(this);

            while (true)
            {
                
                byte[] userdata = new byte[200];

                // read up to 200 chars from the input
                int chars_read = in.read( userdata, 0, 200 );

                // end of file?
                if ( chars_read < 0 ){                    
                    break;	
                }

                
                // convert to a string
                String str = new String( userdata, 0, chars_read );
                System.out.println("Received: " + str);                
                    
                // client leaving
                if (str.substring(0, 5).equals("/quit") ) {
                    // client is leaving
                    chatRoom.notifyAll(str.substring(6) + " is leaving");                    
                    // confirm the quit
                    notifyClient("/quit");
                    closeClient();
                    int num = chatRoom.clientList.size();
                    chatRoom.game.setNumOfPlayers(num);
                    break;
                } 
                
                //the client has started the game
                else if(str.substring(0,6).equals("/START")){
                    chatRoom.notifyAll("Let the games begin!");
                    
                    //set number of players and start game
                    int num = chatRoom.clientList.size();
                    chatRoom.game.setNumOfPlayers(num);
                    chatRoom.updateGameProgress(true);
                    
                    //give the first bingo number
                    chatRoom.notifyAll(chatRoom.game.callBingoNumber());
                    
                }
                
                //client wishes to see their player
                else if(str.substring(0,5).equals("/card")){
                    //t/STARThis should work
                    String s = player.printCard();
                    notifyClient(s);
                }                     
                
                
                //the client wishes to fill a player spot
                else if(str.substring(0, 5).equals("/fill") && str.length() > 9){
                    //fill spot if possible
                    if(player.isValidInt(str.charAt(6)) && player.isValidInt(str.charAt(8))){
                        System.out.println("mark off that spot");
                        int r = (int)str.charAt(6)-48;
                        int c = (int)str.charAt(8)-48;
                        player.markPiece(r, c);
                        String s = player.printCard();
                        notifyClient(s);
                        
                        //update players ready
                        chatRoom.game.addReadyPlayer();
                        if(chatRoom.game.allReady()){
                            chatRoom.notifyAll(chatRoom.game.callBingoNumber());
                        }
                    }
                    //cannot fill spot, let player know of error
                    else{
                        notifyClient("The space you are trying to fill does not exist");
                    }
                }
                
                else if(str.substring(0,5).equals("/next")){
                    chatRoom.game.addReadyPlayer();
                    notifyClient("Tough luck, maybe next call");
                        if(chatRoom.game.allReady()){
                            chatRoom.notifyAll(chatRoom.game.callBingoNumber());
                        }
                }
                
                //client has won
                else if(str.substring(0,6).equals("/bingo")){
                    chatRoom.notifyAll("Someone has won!\n"
                            + "Type /START to begin a new game");
                    //end game
                    chatRoom.updateGameProgress(false);                    
                    //update player count
                    chatRoom.game.setNumOfPlayers(chatRoom.clientList.size());   
                    //update everyones cards for next game
                    chatRoom.updateCards();     
                    //clear memory of numbers that have been called
                    chatRoom.game.clearCalledList();    
                } 
                
                else{
                    chatRoom.notifyAll(str);
                }
                                
            }

            out.close();
            in.close();
            socket.close();
        } catch ( SocketException e ) {				
                closeClient();
        } catch (IOException e) {
                e.printStackTrace();
        }
        System.out.println("Thread exiting");
    }
    
	
}


