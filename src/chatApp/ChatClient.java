package chatApp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Client side of the instant messaging chat app.
 *
 * @author Alex Kelly
 */
public class ChatClient extends JFrame {

    //user name which is displayed before messages
    private String userName;

    //users messages
    private JTextField userText;
    //window where messages are displayed
    private JTextArea chatWindow;

    private ObjectOutputStream output;
    private ObjectInputStream input;

    private String message;

    private String serverIP;

    private Socket connection;

    /**
     * CONSTRUCTOR
     *
     * @param host IP address of the server
     */
    public ChatClient(String host, String userName) throws IOException {

        this.userName = userName;

        setTitle("Instant Message Chat Client");
        serverIP = host;

        userText = new JTextField();
        userText.setEditable(false);

        this.message = "";

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
    private void startRunning() throws IOException {

        try {
            connectToServer();
            setupStreams();
            whileChatting();

        } catch (EOFException ex) {
            ex.printStackTrace();
        } finally {
            cleanUp();
        }
    }

    /**
     * Method for connecting to the server using the IP
     * address and port which the server is listening on
     *
     * @throws IOException
     */
    private void connectToServer() throws IOException {
        connection = new Socket(InetAddress.getByName(serverIP), 6879);
        showMessage("connected to: " + connection.getInetAddress().getHostAddress());
    }

    /**
     * Method for setting up the input and output streams
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
     * is active - ends when the client (or server) types "END"
     *
     * @throws IOException
     */
    private void whileChatting() throws IOException {
        ableToType(true);

        do {
            try {
                message = (String) input.readObject();
                showMessage(message);
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
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
            showMessage(this.userName + " - " + message);

        } catch (IOException ex) {
            chatWindow.append("\noops! error sending message!");
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
     * Method for closing streams and socket once chat
     * is finished.
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
