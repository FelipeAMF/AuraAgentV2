package com.auraagent.controllers;

import java.util.List;
import java.util.Optional;

import com.auraagent.services.FirebaseService;
import com.auraagent.utils.JavaFxUtils;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleGroup;

public class ContactsController implements MainAppController.InitializableController {

    @FXML
    private ListView<RadioButton> contactListsView;
    @FXML
    private ListView<String> blacklistNumbersView;
    @FXML
    private Button deleteListButton;

    private String userId;
    private String userToken; // Mantido para uso futuro com regras de segurança

    private final ToggleGroup contactListToggleGroup = new ToggleGroup();
    private final ObservableList<RadioButton> contactLists = FXCollections.observableArrayList();
    private final ObservableList<String> blacklistNumbers = FXCollections.observableArrayList();

    @Override
    public void initialize(String userId) {
        this.userId = userId;
        contactListsView.setItems(contactLists);
        blacklistNumbersView.setItems(blacklistNumbers);
        // Permite que o utilizador selecione múltiplos números na blacklist para
        // remoção
        blacklistNumbersView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        deleteListButton.disableProperty().bind(contactListToggleGroup.selectedToggleProperty().isNull());

        refreshData();
    }

    public void refreshData() {
        // --- LÓGICA PARA CARREGAR LISTAS (sem alterações) ---
        FirebaseService.getContactListsAsync(userId, userToken).thenAcceptAsync(lists -> {
            Platform.runLater(() -> {
                contactLists.clear();
                contactListToggleGroup.getToggles().clear();
                if (lists != null) {
                    lists.keySet().stream().sorted().forEach(listName -> {
                        RadioButton rb = new RadioButton(listName);
                        rb.setToggleGroup(contactListToggleGroup);
                        contactLists.add(rb);
                    });
                }
            });
        });

        // --- LÓGICA PARA CARREGAR BLACKLIST (sem alterações) ---
        FirebaseService.getBlacklist(userId, userToken).thenAcceptAsync(blacklist -> {
            Platform.runLater(() -> {
                blacklistNumbers.clear();
                if (blacklist != null) {
                    blacklistNumbers.addAll(blacklist.keySet());
                }
            });
        });
    }

    // --- MÉTODOS DE GESTÃO DE LISTAS (sem alterações) ---
    @FXML
    private void handleCreateNewList() {
        /* ...código original... */ }

    @FXML
    private void handleImportCsv() {
        /* ...código original... */ }

    @FXML
    private void handleDeleteSelectedList() {
        /* ...código original... */ }

    // --- NOVOS MÉTODOS PARA A BLACKLIST ---

    /**
     * Abre uma janela para o utilizador digitar um novo número a ser adicionado na
     * blacklist.
     */
    @FXML
    private void handleAddToBlacklist() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Adicionar à Blacklist");
        dialog.setHeaderText("Digite o número de telefone a ser bloqueado (apenas números):");
        dialog.setContentText("Número:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(number -> {
            String sanitizedNumber = number.replaceAll("[^0-9]", "");
            if (!sanitizedNumber.isBlank()) {
                FirebaseService.addToBlacklist(userId, userToken, List.of(sanitizedNumber))
                        .thenAccept(success -> {
                            if (success)
                                Platform.runLater(this::refreshData);
                        });
            }
        });
    }

    /**
     * Remove todos os números que foram selecionados na lista da blacklist.
     */
    @FXML
    private void handleRemoveFromBlacklist() {
        List<String> selectedNumbers = blacklistNumbersView.getSelectionModel().getSelectedItems();

        if (selectedNumbers.isEmpty()) {
            JavaFxUtils.showAlert(Alert.AlertType.WARNING, "Aviso", "Nenhum número selecionado para remover.");
            return;
        }

        if (JavaFxUtils.showConfirmation(Alert.AlertType.CONFIRMATION, "Confirmar Remoção",
                "Tem certeza que deseja remover os " + selectedNumbers.size()
                        + " números selecionados da blacklist?")) {
            FirebaseService.removeFromBlacklist(userId, userToken, selectedNumbers)
                    .thenAccept(success -> {
                        if (success)
                            Platform.runLater(this::refreshData);
                    });
        }
    }

    /**
     * Remove TODOS os números da blacklist após uma confirmação.
     */
    @FXML
    private void handleClearBlacklist() {
        if (blacklistNumbers.isEmpty()) {
            JavaFxUtils.showAlert(Alert.AlertType.INFORMATION, "Aviso", "A blacklist já está vazia.");
            return;
        }

        if (JavaFxUtils.showConfirmation(Alert.AlertType.CONFIRMATION, "Confirmar Limpeza Total",
                "TEM CERTEZA?\n\nEsta ação irá remover TODOS os " + blacklistNumbers.size()
                        + " números da blacklist. Esta ação não pode ser desfeita.")) {
            FirebaseService.clearBlacklist(userId, userToken)
                    .thenAccept(success -> {
                        if (success)
                            Platform.runLater(this::refreshData);
                    });
        }
    }
}