package com.vybhav.ecko;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class HelloApplication extends Application {
    private static Stage primaryStage;
    @Override
    public void start(Stage primaryStage) {
        HelloApplication.primaryStage = primaryStage;
        Image applicationIcon = new Image(Objects.requireNonNull(HelloApplication.class.getResourceAsStream("govofkar.png")));
        primaryStage.getIcons().add(applicationIcon);
        primaryStage.setTitle("ECKO File Manager");
        showLogin();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static void showLogin() {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(HelloApplication.class.getResource("/login.fxml")));
            primaryStage.setScene(new Scene(root,800,650));
//            primaryStage.setMaximized(true);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showFileManager() {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(HelloApplication.class.getResource("/file_manager.fxml")));
            primaryStage.setScene(new Scene(root,600,550));
            primaryStage.setFullScreen(true);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}