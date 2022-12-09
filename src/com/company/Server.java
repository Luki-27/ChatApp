package com.company;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;


public class Server {
    private static int uniqueId;
    private ArrayList<ClientThread> al;
    private ServerGUI sg;
    private SimpleDateFormat sdf;
    private int port;
    private boolean keepGoing;




    public Server(int port, ServerGUI sg) {
        this.sg = sg;
        this.port = port;
        sdf = new SimpleDateFormat("HH:mm:ss");
        al = new ArrayList<ClientThread>();
    }

    public void start() {
        keepGoing = true;
        try
        {
            ServerSocket serverSocket = new ServerSocket(port);

            while(keepGoing)
            {
                display("Server waiting for Clients on port " + port + ".");

                Socket socket = serverSocket.accept();

                if(!keepGoing)
                    break;
                ClientThread t = new ClientThread(socket);
                al.add(t);
                t.start();
            }

            try {
                serverSocket.close();
                for(int i = 0; i < al.size(); ++i) {
                    ClientThread tc = al.get(i);
                    try {
                        tc.sInput.close();
                        tc.sOutput.close();
                        tc.socket.close();
                    }
                    catch(IOException ioE) {
                        // not much I can do
                    }
                }
            }
            catch(Exception e) {
                display("Exception closing the server and clients: " + e);
            }
        }

        catch (IOException e) {
            String msg = sdf.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
            display(msg);
        }
    }

    protected void stop() {
        keepGoing = false;
        try {
            new Socket("localhost", port);
        }
        catch(Exception e) {
        }
    }

    private void display(String msg) {
        String time = sdf.format(new Date()) + " " + msg;
        sg.appendEvent(time + "\n");
    }

    private synchronized void broadcast(String message) {

        String time = sdf.format(new Date());
        String messageLf = time + " " + message + "\n";
        sg.appendRoom(messageLf);     // append in the room window


        for(int i = al.size(); --i >= 0;) {
            ClientThread ct = al.get(i);

            if(!ct.writeMsg(messageLf)) {
                al.remove(i);
                display("Disconnected Client " + ct.username + " removed from list.");
            }
        }
    }


    synchronized void remove(int id) {

        for(int i = 0; i < al.size(); ++i) {
            ClientThread ct = al.get(i);
            // found it
            if(ct.id == id) {
                al.remove(i);
                return;
            }
        }
    }


    /** One instance of this thread will run for each client */
    class ClientThread extends Thread {




        // the socket where to listen/talk
        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        // my unique id (easier for deconnection)
        int id;
        // the Username of the Client
        String username;
        // the only type of message a will receive
        ChatMessage cm;
        // the date I connect
        String date;

        // Constructor
        ClientThread(Socket socket) {
            // a unique id
            id = ++uniqueId;
            this.socket = socket;
            /* Creating both Data Stream */
            System.out.println("Thread trying to create Object Input/Output Streams");
            try
            {
                // create output first
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput  = new ObjectInputStream(socket.getInputStream());
                // read the username
                username = (String) sInput.readObject();
                display(username + " just connected.");
            }
            catch (IOException e) {
                display("Exception creating new Input/output Streams: " + e);
                return;
            }
            // have to catch ClassNotFoundException
            // but I read a String, I am sure it will work
            catch (ClassNotFoundException e) {
            }
            date = new Date() + "\n";
        }

        // what will run forever
        public void run() {
            // to loop until LOGOUT
            boolean keepGoing = true;
            while(keepGoing) {
                // read a String (which is an object)
                try {
                    cm = (ChatMessage) sInput.readObject();
                }
                catch (IOException e) {
                    display(username + " Exception reading Streams: " + e);
                    break;
                }
                catch(ClassNotFoundException e2) {
                    break;
                }
                // the message part of the ChatMessage
                String message = cm.getMessage();

                // Switch on the type of message receive
                switch(cm.getType()) {

                    case ChatMessage.MESSAGE:
                        broadcast(username + ": " + message);
                        break;
                    case ChatMessage.LOGOUT:
                        display(username + " disconnected with a LOGOUT message.");
                        keepGoing = false;
                        break;
                    case ChatMessage.WHOISIN:
                        writeMsg("List of the users connected at " + sdf.format(new Date()) + "\n");
                        // scan al the users connected
                        for(int i = 0; i < al.size(); ++i) {
                            ClientThread ct = al.get(i);
                            writeMsg((i+1) + ") " + ct.username + " since " + ct.date);
                        }
                        break;
                }
            }
            // remove myself from the arrayList containing the list of the
            // connected Clients
            remove(id);
            close();
        }

        // try to close everything
        private void close() {
            // try to close the connection
            try {
                if(sOutput != null) sOutput.close();
            }
            catch(Exception e) {}
            try {
                if(sInput != null) sInput.close();
            }
            catch(Exception e) {};
            try {
                if(socket != null) socket.close();
            }
            catch (Exception e) {}
        }

        /*
         * Write a String to the Client output stream
         */
        private boolean writeMsg(String msg) {
            // if Client is still connected send the message to it
            if(!socket.isConnected()) {
                close();
                return false;
            }
            // write the message to the stream
            try {
                sOutput.writeObject(msg);
            }
            // if an error occurs, do not abort just inform the user
            catch(IOException e) {
                display("Error sending message to " + username);
                display(e.toString());
            }
            return true;
        }
    }



/*
    public static void main(String[] args) {
        // start server on port 1500 unless a PortNumber is specified
        int portNumber = 1500;
        switch(args.length) {
            case 1:
                try {
                    portNumber = Integer.parseInt(args[0]);
                }
                catch(Exception e) {
                    System.out.println("Invalid port number.");
                    System.out.println("Usage is: > java Server [portNumber]");
                    return;
                }
            case 0:
                break;
            default:
                System.out.println("Usage is: > java Server [portNumber]");
                return;

        }
        // create a server object and start it
        Server server = new Server(portNumber);
        server.start();
    }

    /*
     *  To run as a console application just open a console window and:
     * > java Server
     * > java Server portNumber
     * If the port number is not specified 1500 is used
     */

}



