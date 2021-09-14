/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

//one game per chat room

import java.util.ArrayList;

public class Game{        
    private boolean inProgress = false;
    private int numOfPlayers = -1;
    private int playersReady = 0;
    ArrayList<String> numbersCalled = new ArrayList<String>();
    
    Game(){
        
    }
    
    //start and end and check game progress
    boolean getGameInProgress(){
        return inProgress;
    }    
    void setGameInProgress(boolean b){
        inProgress = b;
    } 
    
    //set and get num of players
    int getNumOfPlayers(){
        return numOfPlayers;
    }
    void setNumOfPlayers(int n){        
        numOfPlayers = n;
        System.out.println(numOfPlayers);
        
    }
    
    //set number of players ready
    void addReadyPlayer(){
        playersReady++; 
        System.out.println("incrementing");
        //check to see if they are all ready
    }
    
    //check to see if all players are ready
    boolean allReady(){
        if(playersReady == numOfPlayers){
            System.out.println("all ready");
            //reset players ready for next turn
            playersReady = 0;
            return true;
        }
        return false;
    }
    
    //on the end of a game, wipe the list of all numbers called
    void clearCalledList(){
        numbersCalled.clear();
    }
    
    
    //generate a bingo number to call
    String callBingoNumber(){
        int n = 100;
        do{
            n = (int)(Math.random()*100);
        }while(n > 75 && isUncalledNumber(n));       
            
        //add to array of called numbers
        numbersCalled.add(n+"");
        
        return ">> Check your board for: "+n;
    }  
    
    //verify this is a new bingo number
    boolean isUncalledNumber(int n){
        String num = "" + n;
        for( int i=0; i<numbersCalled.size(); i++ ) {
                //if the number is already called, return false
                String s = numbersCalled.get(i);
                if(s.equals(num)){
                    return false;
                }
        }
        return true;
    }    

        
}

//one card per person
class Player extends Game{
    String [][] card = new String[6][5];
    
    Player(){
        createCard();
        card[0][0] = " B  ";
        card[0][1] = " I  ";
        card[0][2] = " N  ";
        card[0][3] = " G ";
        card[0][4] = " O";
        
    }
    
    //formatting is rough
    void createCard(){
        //go column by column
        int min = 1;
        int max  = 15;
        int cardNum = 0;
        for (int c = 0; c < card[0].length; c++) {
            for (int r = 1; r < card.length; r++) {
                if(r == 3 && c == 2){
                    card[r][c] = "Free";
                }
                else{
                    cardNum = (int)(Math.random()*100);
                    while(!checkGoodNum(cardNum, min, max)){
                        cardNum = (int)(Math.random()*100);
                    }
                    card[r][c] = " "+cardNum+" ";
                }
                
            }
            min += 15;
            max += 15;
        }        
    }
    
    //make sure the number can be added to the card (it's not already there
    boolean checkGoodNum(int n, int min, int max){
        //check if it is in range
        if(n > 75 || n < min || n > max)
            return false;
        
        //check for duplicates
        for (int r = 0; r < card.length; r++) {
            for (int c = 0; c < card[r].length; c++) {
                if(card[r][c]!= null && card[r][c].equals(" "+n+" "))
                    return false;
            }
        }
        return true;     
    }
        
    //generate the card's string
    String printCard(){
        String s = "-------Your Card-------\n";
        for (int r = 0; r < card.length; r++){ 
            for (int c = 0; c < card[r].length; c++){ 
                s += " " + card[r][c];
            }
            s+= "\n";
        } 
        return s;
    }
    
    //are the cords that the user entered valid?
    boolean isValidInt(char n){
        switch(n){
            case '1': 
                return true;
            case '2':
                return true;
            case '3': 
                return true;
            case '4':
                return true;
            case '5': 
                return true;
            default:
                return false;
        }
    }
    
    //mark off a number from the card
    void markPiece(int r, int c){
        //adjust for the 0s with the -1 (only for the cols bc row 0 is BINGO)
        card[r][c-1] = " X ";
    }
}
