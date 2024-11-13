package client.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import server.DatabaseConnection;
import client.model.User;
import java.io.IOException;
import java.sql.*;
import java.net.Socket;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    private Socket socket;
    
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Login Error", "Please enter both username and password");
            return;
        }

        try {
            // Establish socket connection
            socket = new Socket("localhost", 9999);
            

            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "SELECT * FROM users WHERE username = ? AND password = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    User user = new User(rs.getInt("id"), username, password);
                    updateUserOnlineStatus(user.getId(), true);
                    showSuccessAlert("Login Successful", "Welcome back, " + username + "!");
                    openChatWindow(user);
                } else {
                    showAlert("Login Failed", "Invalid username or password");
                }
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Could not verify credentials");
        } catch (IOException e) {
            showAlert("Connection Error", "Could not connect to chat server");
        }
    }

    private void updateUserOnlineStatus(int userId, boolean status) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "UPDATE users SET online_status = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setBoolean(1, status);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {

            System.err.println("Could not update online status: " + e.getMessage());
        }
    }

    private void openChatWindow(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/view/chat.fxml"));
            Parent root = loader.load();
            
            ChatController chatController = loader.getController();
            chatController.initData(user, socket);
            
            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/client/css/style.css").toExternalForm());
            
            stage.setTitle("Mini Zalo - Chat");
            stage.setScene(scene);
            stage.setMaximized(true);
            
            // Handle window closing
            stage.setOnCloseRequest(event -> {
                updateUserOnlineStatus(user.getId(), false);
                try {
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                    }
                } catch (IOException e) {
                    System.err.println("Error closing socket: " + e.getMessage());
                }
            });
            
            stage.show();
            
        } catch (IOException e) {
            showAlert("Error", "Could not open chat window");
        }
    }

    
    @FXML
    private void handleRegisterNavigation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/view/register.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/client/css/style.css").toExternalForm());
            
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert("Navigation Error", "Could not load register page");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void showSuccessAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

}
