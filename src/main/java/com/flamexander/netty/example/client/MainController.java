package com.flamexander.netty.example.client;

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
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;

public class MainController implements Initializable {
    @FXML
    TextField tfFileName;

    @FXML
    ListView<String> filesList;
    @FXML
      ListView<String> filesList1;
    public  ListView<String> getFilesList1(){
        return filesList1;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {


        CountDownLatch networkStarter = new CountDownLatch(1);
        new Thread(() -> ByteNetwork.getInstance().start(networkStarter)).start();
        try {
            networkStarter.await();   // чтобы подождать открытия соединения
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        refreshLocalFilesList();
//        refreshLocalFilesList1();
        refresh();
    }



    public void pressOnDownloadBtnSend(ActionEvent actionEvent) throws IOException {
        if (tfFileName.getLength() > 0) {

            if (Files.exists(Paths.get("client_storage/" +tfFileName.getText()) )) {
                ClientSender.sendFile(Paths.get("client_storage/"+tfFileName.getText()),
                        ByteNetwork.getInstance().getCurrentChannel(), future -> {
                    if (!future.isSuccess()) {
                        future.cause().printStackTrace();

                    }
                    if (future.isSuccess()) {
                        System.out.println(" Файл передан с клиента"+tfFileName.getText());
                     new Thread(()->{
                         try {
                             Thread.sleep(1000);
                         } catch (InterruptedException e) {
                             e.printStackTrace();
                         }
                         refresh();
                     }).start();

                    }
                });

                tfFileName.clear();
                System.out.println("Button Send works");

            }
        }
    }
    public void pressOnDownloadBtnGet(ActionEvent actionEvent) throws IOException {
        if (tfFileName.getLength() > 0) {


                ClientSender.sendFileREQ(Paths.get("server_storage/" + tfFileName.getText()),
                        ByteNetwork.getInstance().getCurrentChannel(), future -> {
                            if (!future.isSuccess()) {
                                future.cause().printStackTrace();

                            }
                            if (future.isSuccess()) {
                                System.out.println("Запрос файла передан с клиента" + tfFileName.getText());
                                new Thread(()->{
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    refresh();
                                }).start();
                            }
                        });

                tfFileName.clear();
                System.out.println("Button Send works");
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
    public  void refresh() {
        Platform.runLater(() -> {
            try {
                filesList.getItems().clear();
                Files.list(Paths.get("client_storage"))
                        .filter(p -> !Files.isDirectory(p))
                        .map(p -> p.getFileName().toString())
                        .forEach(o -> filesList.getItems().add(o));

                filesList1.getItems().clear();
                Files.list(Paths.get("server_storage"))
                        .filter(p -> !Files.isDirectory(p))
                        .map(p -> p.getFileName().toString())
                        .forEach(o -> filesList1.getItems().add(o));
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

}
