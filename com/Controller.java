package com;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import java.io.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Controller {
    @FXML
    private TextArea log;
    @FXML
    private TextArea input;
    @FXML
    private ListView<String> userList;

    private CopyOnWriteArrayList<String> messages = new CopyOnWriteArrayList<>();

    private ObservableList<String> company = FXCollections.observableArrayList();

    private synchronized void setLog() {
        userList.setItems(company);
        initLogFile();
        log.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                log.setScrollTop(Double.MAX_VALUE);
            }
        });

        input.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode().equals(KeyCode.ENTER)) {
                    send();
                    event.consume();
                }
            }
        });

        StringBuilder stringBuilder = new StringBuilder();

        for (String string : messages) {
            stringBuilder.append(string + "\n");
        }

        log.setText(stringBuilder.toString());
        log.appendText("");
    }

    private void addToLog(String message) {
        log.appendText(message + "\n");
    }

    @FXML
    private void initialize() {
        setLog();
    }

    @FXML
    void send() {
        Main.send("*mes*" + input.getText());
        input.setText("");
    }

    public void receive(String message) {
        if (message.startsWith("*add*")){
            message = message.replaceFirst("\\*add\\*", "");
            final String finalMessage = message;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    company.add(finalMessage);
                }
            });
        }
        else if (message.startsWith("*del*")) {
            message = message.replaceFirst("\\*del\\*", "");
            final String finalMessage = message;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    company.remove(finalMessage);
                }
            });
        } else {
            message = message.replaceFirst("\\*mes\\*", "") + "\n";
            messages.add(message);
            addToLog(message);
        }
    }

    private void initLogFile() {
        String str;
        try (BufferedReader fileReader = new BufferedReader(new FileReader("log.txt"))){
            while ((str = fileReader.readLine()) != null) {
                messages.add(str);
            }
        } catch (Exception e) {
            try {
                new FileWriter("log.txt");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public void writeLogFile() {
        try (FileWriter fileWriter = new FileWriter("log.txt")) {
            for (String string : messages) {
                fileWriter.write(string + "\r\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}