package com.company;

import java.net.*;
import java.io.*;

public class Client  {

    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;
    private Socket socket;

    private ClientGUI cg;

    private String server, username;
    private int port;

    Client(String server, int port, String username, ClientGUI cg) {
        this.server = server;
        this.port = port;
        this.username = username;


        this.cg = cg;
    }


    public boolean start() {
        try {
            socket = new Socket(server, port);
        }
        catch(Exception ec) {
            display("Error connection to server:" + ec);
            return false;
        }

        String msg = "\nConnection accepted " + socket.getInetAddress() + ":" + socket.getPort()+"\n";
        display(msg);


        try
        {
            sInput  = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        }
        catch (IOException eIO) {
            display("Exception creating new Input/output Streams: " + eIO);
            return false;
        }


        new ListenFromServer().start();


        try
        {
            sOutput.writeObject(username);
        }
        catch (IOException eIO) {
            display("Exception doing login : " + eIO);
            disconnect();
            return false;
        }

        return true;
    }


    private void display(String msg) {
        cg.append(msg + "\n");
    }


    void sendMessage(ChatMessage msg) {
        try {
            sOutput.writeObject(msg);
        }
        catch(IOException e) {
            display("Exception writing to server: " + e);
        }
    }


    private void disconnect() {
        try {
            if(sInput != null) sInput.close();
        }
        catch(Exception e) {}
        try {
            if(sOutput != null) sOutput.close();
        }
        catch(Exception e) {}
        try{
            if(socket != null) socket.close();
        }
        catch(Exception e) {}

        if(cg != null)
            cg.connectionFailed();

    }
    class ListenFromServer extends Thread {

        public void run() {
            while(true) {
                try {
                    String msg = (String) sInput.readObject();
                    cg.append(msg);
                }
                catch(IOException e) {
                    display("\nThis client has disconnected\n");
                    break;
                }
                catch(ClassNotFoundException e2) {
                }
            }
        }
    }
}
