package chatApp;

import chatApp.ChatServer;
import javax.swing.*;
import java.io.IOException;

/**
 * Class containing the main method to drive
 * the program.
 *
 * @author Alex Kelly
 */
public class Main {

    public static void main(String[] args) throws IOException {
        ChatServer alex = new ChatServer("alexa");
        alex.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

//        ChatClient kat = new ChatClient("127.0.0.1", "katja");
//        kat.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    }
}
