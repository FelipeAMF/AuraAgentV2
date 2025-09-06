package com.auraagent.controllers;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

import com.auraagent.services.ScraperWorker;
import com.auraagent.utils.JavaFxUtils;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

public class ExtractorController implements MainAppController.InitializableController {

    @FXML
    private Button connectButton, disconnectButton, startButton, stopButton, exportButton;
    @FXML
    private TextField stopThresholdField, scrollPauseField;
    @FXML
    private ListView<String> foundNumbersListView;
    @FXML
    private Label statusText;
    @FXML
    private VBox settingsFrame;

    private String userId;
    private final SimpleBooleanProperty isConnected = new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty isExtracting = new SimpleBooleanProperty(false);
    private final ObservableList<String> foundNumbers = FXCollections.observableArrayList();
    private final AtomicBoolean cancellationToken = new AtomicBoolean(false);
    private Thread workerThread;

    private Process scrcpyProcess;

    @Override
    public void initialize(String userId) {
        this.userId = userId;
        foundNumbersListView.setItems(foundNumbers);

        connectButton.disableProperty().bind(isConnected);
        disconnectButton.disableProperty().bind(isConnected.not());
        startButton.disableProperty().bind(isConnected.not().or(isExtracting));
        stopButton.disableProperty().bind(isExtracting.not());
        exportButton.disableProperty().bind(isExtracting);
        settingsFrame.disableProperty().bind(isExtracting.or(isConnected.not()));
    }

    @FXML
    private void handleConnect() {
        statusText.setText("A conectar ao telemóvel...");
        String scrcpyPath = Paths.get(System.getProperty("user.dir"), "vendor", "scrcpy", "scrcpy.exe").toString();
        ProcessBuilder pb = new ProcessBuilder(scrcpyPath, "--window-title", "AuraAgent-SCRCPY");
        try {
            scrcpyProcess = pb.start();
            isConnected.set(true);
            statusText.setText("Telemóvel conectado. Pode iniciar a extração.");
        } catch (Exception e) {
            JavaFxUtils.showAlert(Alert.AlertType.ERROR, "Erro",
                    "scrcpy.exe não encontrado! Verifique a pasta 'vendor'.");
            statusText.setText("Falha na conexão.");
        }
    }

    @FXML
    private void handleDisconnect() {
        handleStop();
        if (scrcpyProcess != null && scrcpyProcess.isAlive()) {
            scrcpyProcess.destroy();
        }
        isConnected.set(false);
        statusText.setText("Desconectado.");
    }

    @FXML
    private void handleStart() {
        isExtracting.set(true);
        foundNumbers.clear();
        cancellationToken.set(false);

        try {
            int threshold = Integer.parseInt(stopThresholdField.getText());
            double pause = Double.parseDouble(scrollPauseField.getText());

            ScraperWorker worker = new ScraperWorker(threshold, pause);

            workerThread = new Thread(() -> {
                worker.run(update -> {
                    Platform.runLater(() -> {
                        if (update.startsWith("Número encontrado:")) {
                            foundNumbers.add(update.replace("Número encontrado: ", "").trim());
                        }
                        statusText.setText(update);
                    });
                }, cancellationToken);

                Platform.runLater(() -> isExtracting.set(false));
            });

            workerThread.setDaemon(true);
            workerThread.start();

        } catch (NumberFormatException e) {
            JavaFxUtils.showAlert(Alert.AlertType.ERROR, "Erro de Configuração",
                    "Os valores de 'Parar Após' e 'Pausa de Rolagem' devem ser números válidos.");
            isExtracting.set(false);
        }
    }

    @FXML
    private void handleStop() {
        if (workerThread != null && workerThread.isAlive()) {
            cancellationToken.set(true);
        }
        isExtracting.set(false);
    }

    @FXML
    private void handleExportCsv() {
        if (foundNumbers.isEmpty()) {
            JavaFxUtils.showAlert(Alert.AlertType.WARNING, "Aviso", "Não há números para exportar.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvar como CSV");
        fileChooser.setInitialFileName("contatos_extraidos.csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
        File file = fileChooser.showSaveDialog(exportButton.getScene().getWindow());

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println("Nome,Telefone");
                foundNumbers.forEach(phone -> {
                    String formattedPhone = phone.replaceAll("[^0-9]", "");
                    writer.printf("Contato_%s,%s%n", formattedPhone.substring(Math.max(0, formattedPhone.length() - 8)),
                            formattedPhone);
                });
                JavaFxUtils.showAlert(Alert.AlertType.INFORMATION, "Sucesso",
                        foundNumbers.size() + " contatos foram salvos.");
            } catch (Exception e) {
                JavaFxUtils.showAlert(Alert.AlertType.ERROR, "Erro",
                        "Não foi possível salvar o ficheiro.\n\nErro: " + e.getMessage());
            }
        }
    }
}