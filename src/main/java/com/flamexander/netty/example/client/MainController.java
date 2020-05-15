package com.flamexander.netty.example.client;

import com.flamexander.netty.example.common.AbstractMessage;
import com.flamexander.netty.example.common.FileMessage;
import com.flamexander.netty.example.common.FileRequest;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML
    TextField tfFileName;


    @FXML
    ListView<String> filesList;
    @FXML
    ListView<String> filesList1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Network.start();
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    AbstractMessage am = Network.readObject();    //  блокируется до получения
                    if (am instanceof FileMessage) {
                        FileMessage fm = (FileMessage) am;
                        Files.write(Paths.get("client_storage/" + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
                        refreshLocalFilesList();

                    }
                    if (am instanceof FileRequest) {
                        System.out.println(((FileRequest) am).getFilename()+" <обратное сообщение");
                    }
                    refreshLocalFilesList1();
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            } finally {
                Network.stop();
            }
        });
        t.setDaemon(true);
        t.start();
        refreshLocalFilesList();
        refreshLocalFilesList1();
    }

    public void pressOnDownloadBtn(ActionEvent actionEvent) {
        if (tfFileName.getLength() > 0) {
            Network.sendMsg(new FileRequest(tfFileName.getText()));
            tfFileName.clear();
        }
    }
    public void pressOnDownloadBtn1(ActionEvent actionEvent) throws IOException {
        if (tfFileName.getLength() > 0) {

            if (Files.exists(Paths.get("client_storage/" + tfFileName.getText()))) {
                FileMessage fm = new FileMessage(Paths.get("client_storage/" + tfFileName.getText()));
                System.out.println(fm.getFilename());
                Network.sendMsg(fm);
                tfFileName.clear();

                System.out.println("Button 1 works");
            }
        }
    }



    public void refreshLocalFilesList() {
        Platform.runLater(() -> {
            try {
                filesList.getItems().clear();
                Files.list(Paths.get("client_storage"))
                        .filter(p -> !Files.isDirectory(p))
                        .map(p -> p.getFileName().toString())
                        .forEach(o -> filesList.getItems().add(o));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    public void refreshLocalFilesList1() {
        Platform.runLater(() -> {
            try {
                filesList1.getItems().clear();
                Files.list(Paths.get("server_storage"))
                        .filter(p -> !Files.isDirectory(p))
                        .map(p -> p.getFileName().toString())
                        .forEach(o -> filesList1.getItems().add(o));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

}
