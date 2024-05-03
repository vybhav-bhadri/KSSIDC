package com.vybhav.ecko;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.Objects;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

public class HelloApplication extends Application {
    private static Stage primaryStage;
    @Override
    public void start(Stage primaryStage) {
        HelloApplication.primaryStage = primaryStage;
        Image applicationIcon = new Image(Objects.requireNonNull(HelloApplication.class.getResourceAsStream("governmentOfKarnatakaIcon.png")));
        primaryStage.getIcons().add(applicationIcon);
        primaryStage.setTitle("Karnataka State Small Industries Development Corporation");
        showLogin();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static void showLogin() {
        try {
            InputStream serviceAccount = HelloApplication.class.getResourceAsStream("/serviceAccountKey.json");
            assert serviceAccount != null;
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
            Parent root = FXMLLoader.load(Objects.requireNonNull(HelloApplication.class.getResource("/login.fxml")));
            primaryStage.setScene(new Scene(root,800,650));
//            primaryStage.setMaximized(true);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}