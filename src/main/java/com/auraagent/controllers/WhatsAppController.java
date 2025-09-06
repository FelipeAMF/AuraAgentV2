package com.auraagent.controllers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import com.auraagent.models.WhatsappAccount;
import com.auraagent.services.WhatsappService;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty; // <-- IMPORT ADICIONADO
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class WhatsAppController implements MainAppController.InitializableController {

    @FXML
    private TextField intervalSecondsField;
    @FXML
    private ListView<CheckBox> accountsListView;
    @FXML
    private TextArea logOutputArea;
    @FXML
    private Button startButton, stopButton;

    private String userId;
    private final WhatsappService whatsappService = new WhatsappService();
    private final ObservableList<CheckBox> accounts = FXCollections.observableArrayList();
    private final SimpleBooleanProperty isWarmerRunning = new SimpleBooleanProperty(false);

    @Override
    public void initialize(String userId) {
        this.userId = userId;
        accountsListView.setItems(accounts);

        startButton.disableProperty().bind(isWarmerRunning);
        stopButton.disableProperty().bind(isWarmerRunning.not());
        intervalSecondsField.disableProperty().bind(isWarmerRunning);
        accountsListView.disableProperty().bind(isWarmerRunning);

        loadAccountsAsync();
    }

    private void loadAccountsAsync() {
        whatsappService.getStatusAsync().thenAcceptAsync(accountsData -> {
            Platform.runLater(() -> {
                accounts.clear();
                for (WhatsappAccount acc : accountsData) {
                    accounts.add(new CheckBox(acc.getSessionId() + " (" + acc.getPhoneNumber() + ")"));
                }
            });
        });
    }

    @FXML
    private void handleStartWarmer() {
        List<CheckBox> selectedAccounts = accounts.stream()
                .filter(CheckBox::isSelected)
                .collect(Collectors.toList());

        if (selectedAccounts.size() < 2) {
            return;
        }
        isWarmerRunning.set(true);
        logMessage("--- Aquecedor Iniciado ---");
    }

    @FXML
    private void handleStopWarmer() {
        isWarmerRunning.set(false);
        logMessage("--- Aquecedor Parado pelo utilizador ---");
    }

    private void logMessage(String message) {
        Platform.runLater(() -> {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            logOutputArea.appendText(String.format("[%s] %s\n", timestamp, message));
        });
    }
}