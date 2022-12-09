package com.company;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ClientGUI extends JFrame implements ActionListener {

    private JLabel label;
    private JTextField tf;
    private JTextField tfServer, tfPort;
    private JButton login, logout, whoIsIn;
    private JTextArea ta;
    private boolean connected;
    private Client client;

    private int defaultPort;
    private String defaultHost;

    ClientGUI(String host, int port) {

        super("Chat Client");
        defaultPort = port;
        defaultHost = host;

        JPanel northPanel = new JPanel(new GridLayout(3,1));

        JPanel serverAndPort = new JPanel(new GridLayout(1,5, 1, 3));

        tfServer = new JTextField(host);
        tfPort = new JTextField("" + port);
        tfPort.setHorizontalAlignment(SwingConstants.RIGHT);

        tfServer.enable(false);
        serverAndPort.add(new JLabel("Server Address:  "));
        serverAndPort.add(tfServer);
        serverAndPort.add(new JLabel("Port Number:  "));
        serverAndPort.add(tfPort);
        serverAndPort.add(new JLabel(""));

        northPanel.add(serverAndPort);

        label = new JLabel("Enter your username below and click LOGIN button", SwingConstants.CENTER);
        northPanel.add(label);
        tf = new JTextField("User Name");
        tf.setBackground(Color.WHITE);
        northPanel.add(tf);
        add(northPanel, BorderLayout.NORTH);

        ta = new JTextArea("Welcome to the Chat room\n\n", 80, 80);
        JPanel centerPanel = new JPanel(new GridLayout(1,1));
        centerPanel.add(new JScrollPane(ta));
        ta.setEditable(false);
        add(centerPanel, BorderLayout.CENTER);

        login = new JButton("Login");
        login.addActionListener(this);
        logout = new JButton("Logout");
        logout.addActionListener(this);
        logout.setEnabled(false);
        whoIsIn = new JButton("Who is in");
        whoIsIn.addActionListener(this);
        whoIsIn.setEnabled(false);
        JPanel southPanel = new JPanel();
        southPanel.add(login);
        southPanel.add(logout);
        southPanel.add(whoIsIn);
        add(southPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 600);
        setVisible(true);
        tf.requestFocus();

    }

    void append(String str) {
        ta.append(str);
        ta.setCaretPosition(ta.getText().length() - 1);
    }
    void connectionFailed() {
        login.setEnabled(true);
        logout.setEnabled(false);
        whoIsIn.setEnabled(false);
        label.setText("Enter your username below");
        tf.setText("User Name");
        tfPort.setText("" + defaultPort);
        tfServer.setText(defaultHost);
        tfServer.setEditable(false);
        tfPort.setEditable(false);
        tf.removeActionListener(this);
        connected = false;
    }


    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();

        if(o == logout) {
            client.sendMessage(new ChatMessage(ChatMessage.LOGOUT, ""));
            return;
        }
        // if it the who is in button
        if(o == whoIsIn) {
            client.sendMessage(new ChatMessage(ChatMessage.WHOISIN, ""));
            return;
        }

        if(connected) {
            client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, tf.getText()));
            tf.setText("");
            return;
        }


        if(o == login) {
            String username = tf.getText().trim();
            if(username.length() == 0)
                return;

            String server = tfServer.getText().trim();
            if(server.length() == 0)
                return;
            String portNumber = tfPort.getText().trim();
            if(portNumber.length() == 0)
                return;
            int port = 0;
            try {
                port = Integer.parseInt(portNumber);
            }
            catch(Exception en) {
                return;
            }

            client = new Client(server, port, username, this);
            // test if we can start the Client
            if(!client.start())
                return;
            tf.setText("");
            label.setText("Enter your message below and hit enter ");
            connected = true;

            // disable login button
            login.setEnabled(false);
            // enable the 2 buttons
            logout.setEnabled(true);
            whoIsIn.setEnabled(true);
            // disable the Server and Port JTextField
            tfServer.setEditable(false);
            tfPort.setEditable(false);
            // Action listener for when the user enter a message
            tf.addActionListener(this);
        }

    }

    // to start the whole thing the server
    public static void main(String[] args) {
        new ClientGUI("localhost", 1027);
    }

}
