package com.vybhav.ecko;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;

    @FXML
    private Label welcomeLabel;

    @FXML
    private HBox loginBox;

    @FXML
    private void initialize() {
        // Fade-in for welcome message
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(2), welcomeLabel);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.setCycleCount(1);
        fadeIn.setAutoReverse(false);

        welcomeLabel.setOpacity(0.0);
        fadeIn.play();

        // Slide-in for login form
        TranslateTransition slideIn = new TranslateTransition(Duration.seconds(2), loginBox);
        slideIn.setFromX(-200);
        slideIn.setToX(0);
        slideIn.setCycleCount(1);
        slideIn.setAutoReverse(false);

        loginBox.setTranslateX(-200);
        slideIn.play();
    }

    public void login(ActionEvent actionEvent) throws IOException {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // In a real-world application, you would check the username and password
        // against a database or a user authentication service here.
        if ("admin".equals(username) && "password".equals(password)) {
            // Login successful
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/file_manager.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root,800,650));
            stage.setMaximized(true);
        } else {
            // Login failed
            errorLabel.setText("Invalid username or password.");
        }
    }
}
