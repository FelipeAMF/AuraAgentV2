package com.auraagent.controllers;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.auraagent.services.AIService;
import com.auraagent.utils.JavaFxUtils;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class AITrainerController implements MainAppController.InitializableController {

    @FXML
    private VBox settingsPane, chatPane;
    @FXML
    private Button toggleServerButton, savePromptButton, sendTestMessageButton;
    @FXML
    private ComboBox<String> modelComboBox, accountComboBox;
    @FXML
    private TextArea personalityPromptArea, chatHistoryArea;
    @FXML
    private TextField userTestInput;
    @FXML
    private Label aiStatusLabel;

    private String userId;
    private String userToken;

    private final SimpleBooleanProperty isServerRunning = new SimpleBooleanProperty(false);
    private final ObservableList<String> availableModels = FXCollections.observableArrayList();
    private final ObservableList<String> availableAccounts = FXCollections.observableArrayList();
    private List<Object> chatTestHistory = new ArrayList<>();

    @Override
    public void initialize(String userId) {
        this.userId = userId;
        setupBindings();

        modelComboBox.setItems(availableModels);
        accountComboBox.setItems(availableAccounts);

        accountComboBox.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> onAccountSelected(newVal));

        loadAvailableModels();
        refreshData();
    }

    private void setupBindings() {
        settingsPane.disableProperty().bind(isServerRunning);
        chatPane.disableProperty().bind(isServerRunning.not());

        aiStatusLabel.textProperty().bind(
                Bindings.when(isServerRunning).then("Ativo").otherwise("Inativo"));
        toggleServerButton.textProperty().bind(
                Bindings.when(isServerRunning).then("Desativar Servidor de IA").otherwise("Ativar Servidor de IA"));

        isServerRunning.addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                aiStatusLabel.getStyleClass().remove("danger-label");
                aiStatusLabel.getStyleClass().add("success-label");
            } else {
                aiStatusLabel.getStyleClass().remove("success-label");
                aiStatusLabel.getStyleClass().add("danger-label");
            }
        });
    }

    public void refreshData() {
        availableAccounts.setAll("Selecione uma conta...", "Conta Principal (Simulada)");
        accountComboBox.getSelectionModel().selectFirst();
    }

    private void loadAvailableModels() {
        File modelDir = Paths.get(System.getProperty("user.dir"), "model").toFile();
        if (modelDir.exists() && modelDir.isDirectory()) {
            File[] ggufFiles = modelDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".gguf"));
            if (ggufFiles != null && ggufFiles.length > 0) {
                availableModels.setAll(Stream.of(ggufFiles)
                        .map(File::getName)
                        .sorted()
                        .collect(Collectors.toList()));
                modelComboBox.getSelectionModel().selectFirst();
            } else {
                availableModels.setAll("Nenhum modelo .gguf encontrado.");
            }
        } else {
            availableModels.setAll("Pasta 'model' não encontrada.");
        }
    }

    private void onAccountSelected(String accountName) {
        chatTestHistory.clear();
        if (accountName == null || accountName.equals("Selecione uma conta...")) {
            personalityPromptArea.clear();
            chatHistoryArea.clear();
            return;
        }

        chatHistoryArea.setText("--- Conversa de Teste com " + accountName + " ---\n");
    }

    @FXML
    private void handleToggleServer() {
        if (!isServerRunning.get()) {
            if (availableModels.get(0).contains("não encontrada")) {
                JavaFxUtils.showAlert(Alert.AlertType.WARNING, "Erro",
                        "Nenhum modelo de IA (.gguf) encontrado na pasta 'model'.");
                return;
            }
        }
        isServerRunning.set(!isServerRunning.get());
    }

    @FXML
    private void handleSavePrompt() {
        String account = accountComboBox.getSelectionModel().getSelectedItem();
        if (account == null || account.equals("Selecione uma conta...")) {
            JavaFxUtils.showAlert(Alert.AlertType.WARNING, "Aviso", "Selecione uma conta antes de salvar.");
            return;
        }
        JavaFxUtils.showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Personalidade salva (simulado).");
    }

    @FXML
    private void handleSendTestMessage() {
        String message = userTestInput.getText();
        if (message == null || message.isBlank())
            return;

        String userMessage = message;
        userTestInput.clear();
        chatHistoryArea.appendText("Você: " + userMessage + "\n");
        chatTestHistory.add(new java.util.HashMap<String, String>() {
            {
                put("role", "user");
                put("content", userMessage);
            }
        });

        sendTestMessageButton.setDisable(true);
        chatHistoryArea.appendText("IA a pensar...\n");

        new Thread(() -> {
            AIService.generateResponseAsync(chatTestHistory, personalityPromptArea.getText())
                    .thenAcceptAsync(response -> {
                        Platform.runLater(() -> {
                            int lastLineStart = chatHistoryArea.getText().lastIndexOf("IA a pensar...");
                            if (lastLineStart != -1) {
                                chatHistoryArea.deleteText(lastLineStart, chatHistoryArea.getLength());
                            }

                            chatHistoryArea.appendText("IA: " + response + "\n\n");
                            chatTestHistory.add(new java.util.HashMap<String, String>() {
                                {
                                    put("role", "assistant");
                                    put("content", response);
                                }
                            });
                            sendTestMessageButton.setDisable(false);
                        });
                    });
        }).start();
    }
}