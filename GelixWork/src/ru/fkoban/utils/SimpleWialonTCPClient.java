package ru.fkoban.utils;

import java.io.*;
import java.net.*;
import java.util.List;

public class SimpleWialonTCPClient implements Runnable{

    private String loginStr;
    private String dataStr;

    public SimpleWialonTCPClient (String loginStr,String dataStr){
        this.loginStr = loginStr;
        this.dataStr = dataStr;
    }

    public void run(){

        //String sentence;
        String serverLoginAnswer;
        //BufferedReader inFromUser = new BufferedReader( new InputStreamReader(System.in));
        try{
            System.out.println("entered in run method: ");
            Socket clientSocket = new Socket("193.193.165.165", 20332);
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            //sentence = inFromUser.readLine();
            outToServer.writeBytes(this.loginStr);
            serverLoginAnswer = inFromServer.readLine();
            System.out.println("FROM SERVER serverLoginAnswer: " + serverLoginAnswer);
            if (serverLoginAnswer.equals("#AL#1")) {
                System.out.println("we passed: " + serverLoginAnswer);
                outToServer.writeBytes(this.dataStr);
            }
            serverLoginAnswer = inFromServer.readLine();
            System.out.println("FROM SERVER serverLoginAnswer: " + serverLoginAnswer);


            clientSocket.close();

        } catch (Exception e) {
            System.out.println("exception e=" + e.getMessage());
        }



    }
}