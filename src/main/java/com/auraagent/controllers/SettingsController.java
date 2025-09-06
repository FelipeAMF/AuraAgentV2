package com.auraagent.controllers;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.Optional;

import com.auraagent.models.WhatsappAccount;
import com.auraagent.services.WhatsappService;
import com.auraagent.utils.JavaFxUtils;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class SettingsController implements MainAppController.InitializableController {

    @FXML
    private ListView<WhatsappAccount> accountsListView;
    @FXML
    private ImageView qrCodeImageView;
    @FXML
    private Text qrStatusText;

    private String userId;
    private final WhatsappService whatsappService = new WhatsappService();
    private final ObservableList<WhatsappAccount> accounts = FXCollections.observableArrayList();
    private Timeline statusTimer;

    @Override
    public void initialize(String userId) {
        this.userId = userId;
        accountsListView.setItems(accounts);

        // Define como cada conta é exibida na lista
        accountsListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(WhatsappAccount item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String phone = item.getPhoneNumber() != null ? " (" + item.getPhoneNumber() + ")" : "";
                    setText(String.format("%s%s - [%s]", item.getSessionId(), phone, item.getStatus()));
                }
            }
        });

        // Atualiza o QR Code quando uma conta é selecionada
        accountsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updateQrCodeDisplay(newVal);
        });

        // Inicia o timer para atualização automática
        setupStatusTimer();
        refreshData(); // Faz a primeira chamada imediatamente
    }

    // O MainAppController chamará este método quando a vista ficar visível
    public void onShown() {
        if (statusTimer != null && statusTimer.getStatus() != Timeline.Status.RUNNING) {
            statusTimer.play();
            refreshData();
        }
    }

    // O MainAppController chamará este método quando a vista ficar oculta
    public void onHidden() {
        if (statusTimer != null) {
            statusTimer.stop();
        }
    }

    private void setupStatusTimer() {
        statusTimer = new Timeline(new KeyFrame(Duration.seconds(3), event -> refreshData()));
        statusTimer.setCycleCount(Timeline.INDEFINITE);
    }

    private void refreshData() {
        WhatsappAccount currentSelection = accountsListView.getSelectionModel().getSelectedItem();

        whatsappService.getStatusAsync().thenAcceptAsync(sessions -> {
            Platform.runLater(() -> {
                accounts.setAll(sessions);
                // Tenta manter a seleção anterior
                if (currentSelection != null) {
                    // Procura pela conta com o mesmo sessionId na nova lista
                    Optional<WhatsappAccount> reselect = accounts.stream()
                            .filter(acc -> acc.getSessionId().equals(currentSelection.getSessionId()))
                            .findFirst();
                    reselect.ifPresent(acc -> accountsListView.getSelectionModel().select(acc));
                }
                // Atualiza o QR code caso o status da conta selecionada tenha mudado
                updateQrCodeDisplay(accountsListView.getSelectionModel().getSelectedItem());
            });
        });
    }

    private void updateQrCodeDisplay(WhatsappAccount account) {
        if (account == null) {
            qrCodeImageView.setImage(null);
            qrStatusText.setText("Selecione ou adicione uma conta.");
            qrCodeImageView.setVisible(false);
            qrStatusText.setVisible(true);
            return;
        }

        if ("QR Code".equals(account.getStatus()) && account.getQrCode() != null && !account.getQrCode().isBlank()) {
            try {
                String base64Data = account.getQrCode().split(",")[1];
                byte[] imageBytes = Base64.getDecoder().decode(base64Data);
                Image image = new Image(new ByteArrayInputStream(imageBytes));
                qrCodeImageView.setImage(image);
                qrCodeImageView.setVisible(true);
                qrStatusText.setVisible(false);
            } catch (Exception e) {
                qrStatusText.setText("Erro ao renderizar QR Code.");
                qrCodeImageView.setVisible(false);
                qrStatusText.setVisible(true);
            }
        } else if ("Conectado".equals(account.getStatus())) {
            qrCodeImageView.setImage(null);
            qrStatusText.setText("✅ Conta conectada com sucesso!");
            qrCodeImageView.setVisible(false);
            qrStatusText.setVisible(true);
        } else {
            qrCodeImageView.setImage(null);
            qrStatusText.setText("A inicializar...\nIsto pode demorar alguns segundos.");
            qrCodeImageView.setVisible(false);
            qrStatusText.setVisible(true);
        }
    }

    @FXML
    private void handleAddAccount() {
        TextInputDialog dialog = new TextInputDialog("Nova_Conta");
        dialog.setTitle("Nova Conta");
        dialog.setHeaderText("Digite um nome para a nova conta (ex: Vendas):");
        dialog.setContentText("ID da Sessão:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(sessionId -> {
            if (!sessionId.isBlank()) {
                String sanitizedSessionId = sessionId.replaceAll("[^a-zA-Z0-9_-]", "-");

                boolean exists = accounts.stream()
                        .anyMatch(acc -> acc.getSessionId().equalsIgnoreCase(sanitizedSessionId));
                if (exists) {
                    JavaFxUtils.showAlert(Alert.AlertType.WARNING, "Aviso", "Uma conta com este nome já existe.");
                    return;
                }

                // whatsappService.connectAsync(sanitizedSessionId); // Chamada real ao serviço
                System.out.println("A adicionar conta: " + sanitizedSessionId);
                // Dá um tempo para o servidor gerar o QR code antes de atualizar
                new Timeline(new KeyFrame(Duration.seconds(1.5), e -> refreshData())).play();
            }
        });
    }

    @FXML
    private void handleDisconnectAccount() {
        WhatsappAccount selected = accountsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            JavaFxUtils.showAlert(Alert.AlertType.WARNING, "Aviso", "Selecione uma conta da lista para desconectar.");
            return;
        }

        // whatsappService.disconnectAsync(selected.getSessionId()); // Chamada real
        System.out.println("A desconectar conta: " + selected.getSessionId());
        new Timeline(new KeyFrame(Duration.seconds(1.5), e -> refreshData())).play();
    }
}