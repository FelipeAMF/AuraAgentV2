package com.auraagent.controllers;

import com.auraagent.services.FirebaseService;
import com.auraagent.utils.JavaFxUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import java.util.function.Consumer;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Text statusMessage;

    private final SimpleStringProperty email = new SimpleStringProperty("");
    private final SimpleStringProperty statusMessageText = new SimpleStringProperty("");
    private final SimpleStringProperty loginButtonText = new SimpleStringProperty("Entrar");
    private final SimpleBooleanProperty isLoginEnabled = new SimpleBooleanProperty(true);

    // Callback para notificar a janela principal sobre o sucesso do login
    private Consumer<String> onLoginSuccess;

    @FXML
    public void initialize() {
        emailField.textProperty().bindBidirectional(email);
        statusMessage.textProperty().bind(statusMessageText);
        loginButton.textProperty().bind(loginButtonText);
        loginButton.disableProperty().bind(isLoginEnabled.not());
    }

    public void setOnLoginSuccess(Consumer<String> onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        statusMessageText.set("");
        String password = passwordField.getText();

        if (email.get() == null || email.get().isBlank() || password.isBlank()) {
            statusMessageText.set("Por favor, preencha e-mail e senha.");
            return;
        }

        updateUIForLogin(true);

        FirebaseService.signInAsync(email.get(), password)
            .thenAcceptAsync(user -> {
                if (user != null && user.getLocalId() != null) {
                    if (onLoginSuccess != null) {
                        // Garante que a atualização da UI principal ocorra no thread do JavaFX
                        Platform.runLater(() -> onLoginSuccess.accept(user.getLocalId()));
                    }
                } else {
                    Platform.runLater(() -> {
                        statusMessageText.set("E-mail ou senha inválidos.");
                        updateUIForLogin(false);
                    });
                }
            }).exceptionally(ex -> {
                Platform.runLater(() -> {
                    statusMessageText.set("Erro ao conectar: " + ex.getMessage());
                    updateUIForLogin(false);
                });
                return null;
            });
    }

    private void updateUIForLogin(boolean isLoggingIn) {
        isLoginEnabled.set(!isLoggingIn);
        loginButtonText.set(isLoggingIn ? "A entrar..." : "Entrar");
    }
}