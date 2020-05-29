package com.flamexander.netty.example.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainClient extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/main.fxml"));
        Parent root = fxmlLoader.load();
        primaryStage.setTitle("Box Client");
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();


        MainController c = fxmlLoader.getController();
        primaryStage.setOnCloseRequest(event->{
            ByteNetwork.getInstance().getCurrentChannel().close();
            Platform.exit();
            System.exit(0);
        });




    }
    public static void main(String[] args) {
        launch(args);
    }
}
