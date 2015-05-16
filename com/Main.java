package com;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class Main extends Application {

    private static DataInputStream inputStream;
    private static DataOutputStream outputStream;
    private Stage primaryStage;
    private AnchorPane anchorPane;
    private static Controller controller;
    private Socket socket;
    private static String userName;
    private static final String DATE_PATTERN = "dd.MM.yyyy_hh:mm:ss";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);
    private String ipAddress = "31.43.139.22";

    public static boolean isRunning = true;

    @Override
    public void start(Stage primaryStage) throws Exception{

        initConnection();

        setUserName();

        //Loading main app frame
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("MSGRMainFrame.fxml"));
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle(userName + " - MSGR");
        anchorPane = (AnchorPane) loader.load();
        primaryStage.setScene(new Scene(anchorPane));
        primaryStage.show();

        //initializing receiver class
        controller = loader.getController();
        Receiver receiver = new Receiver(inputStream, controller);

        //Set action on close
        this.primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                ending();
            }
        });
    }

    private static void ending() {
        controller.writeLogFile();
        isRunning = false;
        try {
            inputStream.close();
            outputStream.close();
        } catch (IOException | NullPointerException ignored) {
            //NOP
        }
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void initConnection() {
        try {
            socket = new Socket(ipAddress, 3333);
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setContentText("Cannot connect server");
            alert.showAndWait();
            System.exit(0);
        }
    }

    public static void connectionErrorDialog() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error!");
                alert.setContentText("Lost connection with server");
                alert.showAndWait();
                ending();
            }
        });
    }

    public static void send(String message) {
        if (message.startsWith("*mes*")) {
            message = userName + "_" + setDate() + "\n" + message;
        }
        try {
            outputStream.writeUTF(message);
            outputStream.flush();
        } catch (IOException e) {
            connectionErrorDialog();
        }
    }

    private void setUserName() {
        while (true) {
            TextInputDialog inputDialog = new TextInputDialog();
            inputDialog.setTitle("MSGR");
            inputDialog.setHeaderText("Enter your name");
            Optional<String> name = inputDialog.showAndWait();
            if (name.isPresent()) {
                if (name.get().length() != 0) {
                    userName = name.get();
                    send("*nam*" + userName);
                    break;
                } else {
                    inputDialog.setHeaderText("Name cannot be empty");
                }
            } else {
                System.exit(0);
            }
        }
    }

    private static String setDate() {
        LocalDateTime date = LocalDateTime.now();
        return DATE_FORMATTER.format(date);
    }
}
