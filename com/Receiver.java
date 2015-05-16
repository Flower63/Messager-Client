package com;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by Dennis on 5/13/2015.
 */
public class Receiver extends Thread {
    private DataInputStream inputStream;
    private Controller controller;

    public Receiver(DataInputStream inputStream, Controller controller) {
        this.inputStream = inputStream;
        this.controller = controller;
        this.start();
    }

    public void run() {
        if (controller != null) {
            try {
                while (true) {
                    controller.receive(inputStream.readUTF());
                }
            } catch (IOException | NullPointerException e) {
                if (Main.isRunning) {
                    Main.connectionErrorDialog();
                }
            }
        }
    }
}
