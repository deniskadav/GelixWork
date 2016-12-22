package ru.fkoban.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class SimpleWialonTCPClient implements Runnable{

    private String loginStr;
    private String dataStr;

    public SimpleWialonTCPClient (String loginStr,String dataStr){
        this.loginStr = loginStr;
        this.dataStr = dataStr;
    }

    public void run(){
        String serverLoginAnswer;
        try{
            System.out.println("Try to send data to Wialon: ");
            Socket clientSocket = new Socket("193.193.165.165", 20332);
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            outToServer.writeBytes(this.loginStr);
            serverLoginAnswer = inFromServer.readLine();
            System.out.println("FROM SERVER AFTER LOGIN: " + serverLoginAnswer);
            if (serverLoginAnswer.equals("#AL#1")) {
                System.out.println("We passed, try to send data");
                outToServer.writeBytes(this.dataStr);
            }
            serverLoginAnswer = inFromServer.readLine();
            System.out.println("FROM SERVER AFTER SEND DATA ANSWER: " + serverLoginAnswer);
            clientSocket.close();

        } catch (Exception e) {
            System.out.println("exception e=" + e.getMessage());
        }
    }
}
