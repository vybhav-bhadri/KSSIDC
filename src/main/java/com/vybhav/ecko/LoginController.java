package com.vybhav.ecko;

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
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

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
    private UserRole userRole;

    private static final String FIREBASE_API_KEY = "<api-key>";

    @FXML
    private void initialize() {

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
        String email = usernameField.getText();
        String password = passwordField.getText();
        try{
            if (email.contains("@gmail.com")) {
                String url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + FIREBASE_API_KEY;

                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

                // Setting Request Method
                connection.setRequestMethod("POST");

                // Setting Request Property
                connection.setRequestProperty("Content-Type", "application/json");

                // Enabling Input and Output Stream
                connection.setDoOutput(true);

                // JSON object for the email and password
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("email", email);
                jsonParam.put("password", password);
                jsonParam.put("returnSecureToken", true);

                // Writing JSON object to the output stream
                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                wr.writeBytes(jsonParam.toString());
                wr.flush();
                wr.close();

                // Get the response code
                int responseCode = connection.getResponseCode();

                // If response is 200, login was successful
                if (responseCode == 200) {
                    // Read the input stream
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder content = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                    in.close();

                    // Parse the response to JSON
                    JSONObject response = new JSONObject(content.toString());

                    // Get the idToken from the response
                    String idToken = response.getString("idToken");
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/file_manager.fxml"));
                    Parent root = loader.load();
                    Stage stage = (Stage) usernameField.getScene().getWindow();
                    stage.setScene(new Scene(root,800,650));
                    stage.setMaximized(true);
                    // Now you can use this idToken to authenticate the user with your backend server
//                    System.out.println("Login was successful, idToken: " + idToken);
                } else {
//                    System.out.println("Login failed, response code: " + responseCode);
                    errorLabel.setText("Invalid email or password.");
                }
            } else if (email.equals("admin") && password.equals("Admin@123$")) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/file_manager.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(new Scene(root,800,650));
                stage.setMaximized(true);
            } else if (email.equals("kssidc") && password.equals("kssidc12#")) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/file_manager.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(new Scene(root,800,650));
                stage.setMaximized(true);
            } else {
                System.out.println("No user found with the provided email.");
                errorLabel.setText("Invalid username or password.");
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void setUserRole(UserRole userRole) {
        this.userRole = userRole;
    }
}
