package chatApp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Simple instant messaging chat application server, utilising
 * sockets and streams, which listens on port 6879.
 *
 * @author Alex Kelly
 */
public class ChatServer extends JFrame {

    //user name which is displayed before messages
    private String userName;

    //users messages
    private JTextField userText;
    //window where messages are displayed
    private JTextArea chatWindow;

    private ObjectOutputStream output;
    private ObjectInputStream input;

    private Socket connection;
    private ServerSocket server;

    /**
     * CONSTRUCTOR
     *
     * @param userName displayed before users messages
     */
    public ChatServer(String userName) throws IOException {

        this.userName = userName;

        setTitle("Instant Message Chat Server");

        userText = new JTextField();
        userText.setEditable(false);

        userText.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        sendMessage(event.getActionCommand());
                        userText.setText(""); //make text area blank once message sent
                    }
                }
        );

        add(userText, BorderLayout.NORTH);

        chatWindow = new JTextArea();
        add(new JScrollPane(chatWindow));

        setSize(500, 250);
        setVisible(true);

        startRunning();
    }

    public String getUserName() {
        return userName;
    }

    /**
     * Method which starts the process running by calling the
     * connectToServer(), setupSteams() and whileChatting
     * methods - to be called in constructor
     */
    private void startRunning() {
        try {
            server = new ServerSocket(6879);

            while(true) {
                try {
                    waitForConnection();
                    setupStreams();
                    whileChatting();

                } catch (EOFException eof) {
                    showMessage("Other user left the chat!");
                } finally {
                    cleanUp();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method which accepts connection from client.
     *
     * @throws IOException
     */
    private void waitForConnection() throws IOException {

        showMessage("waiting for somebody to join the chat...\n");

        connection = server.accept();

        showMessage("now connected to " + server.getInetAddress().getHostAddress());
    }

    /**
     * Method for setting up the input and output streams.
     *
     * @throws IOException
     */
    private void setupStreams() throws IOException {

        output = new ObjectOutputStream(connection.getOutputStream());
        output.flush();

        input = new ObjectInputStream(connection.getInputStream());
    }

    /**
     * Method which continues to show messages while the chat
     * is active - ends when the server (or client) types "END"
     *
     * @throws IOException
     */
    private void whileChatting() throws IOException {

        String message = "you may begin the chat!";
//        sendMessage(message);

        chatWindow.append("\n" + message);

        ableToType(true);

        do{
            try {
                message = (String) input.readObject();
                showMessage(message);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        } while(!message.equals(this.userName + " - END"));
    }

    /**
     * Method that updates the chatWindow by adding the
     * message passed to the end and updating it
     *
     * @param message the message sent
     */
    private void showMessage(final String message) {

        SwingUtilities.invokeLater(
                new Runnable() {
                    @Override
                    public void run() {
                        //add message to the end of the chat
                        chatWindow.append("\n" + message);
                    }
                }
        );
    }

    /**
     * Method which outputs the message using the sendMessage()
     * method.
     *
     * @param message the message being outputted
     */
    private void sendMessage(String message) {

        try {
            output.writeObject(this.userName + " - " + message);
            output.flush();
            //show message on server screen
            showMessage(this.userName + " - " + message);
        } catch (IOException e) {
            chatWindow.append("\noops! error sending message..");
        }
    }

    /**
     * Method which prevents server from typing before a client
     * has connected.
     *
     * @param trueOrFalse boolean to indicate whether client connected
     */
    private void ableToType(final boolean trueOrFalse) {

        SwingUtilities.invokeLater(
                new Runnable() {
                    @Override
                    public void run() {
                        userText.setEditable(trueOrFalse);
                    }
                }
        );
    }

    /**
     * Method for closing streams and socket once
     * chat is finished.
     */
    private void cleanUp() {
        showMessage("closing connections...");

        ableToType(false);

        try {
            output.close();
            input.close();
            connection.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
