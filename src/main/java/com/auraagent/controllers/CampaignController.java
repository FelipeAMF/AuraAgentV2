package com.auraagent.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.auraagent.models.ContactModel;
import com.auraagent.services.FirebaseService;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;

public class CampaignController implements MainAppController.InitializableController {

    @FXML
    private ComboBox<String> contactListComboBox;
    @FXML
    private TextArea contactsDisplay;
    @FXML
    private ListView<CheckBox> sendersListView;
    @FXML
    private TextArea spintaxMessage;
    @FXML
    private ComboBox<String> templateSelector;
    @FXML
    private ComboBox<String> delayComboBox;
    @FXML
    private Button startButton, pauseButton, stopButton, testSendButton, saveTemplateButton;
    @FXML
    private Label statusLabel;
    @FXML
    private ProgressBar progressBar;

    private String userId;
    private String userToken;

    private final SimpleBooleanProperty isSending = new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty isPaused = new SimpleBooleanProperty(false);

    private final ObservableList<String> contactListNames = FXCollections.observableArrayList();
    private final ObservableList<CheckBox> senderAccounts = FXCollections.observableArrayList();
    private final ObservableList<String> templateNames = FXCollections.observableArrayList();
    private final List<ContactModel> contactsInList = new ArrayList<>();

    @Override
    public void initialize(String userId) {
        this.userId = userId;
        setupBindings();
        setupUI();
        refreshData();
    }

    private void setupBindings() {
        contactListComboBox.disableProperty().bind(isSending);
        templateSelector.disableProperty().bind(isSending);
        sendersListView.disableProperty().bind(isSending);
        spintaxMessage.disableProperty().bind(isSending);
        delayComboBox.disableProperty().bind(isSending);
        testSendButton.disableProperty().bind(isSending);
        saveTemplateButton.disableProperty().bind(isSending);
        startButton.disableProperty().bind(isSending);
        pauseButton.disableProperty().bind(isSending.not());
        stopButton.disableProperty().bind(isSending.not());
        pauseButton.textProperty().bind(
                Bindings.when(isPaused).then("Retomar").otherwise("Pausar"));
    }

    private void setupUI() {
        contactListComboBox.setItems(contactListNames);
        sendersListView.setItems(senderAccounts);
        templateSelector.setItems(templateNames);
        delayComboBox.setItems(FXCollections.observableArrayList("5s", "10s", "15s", "30s", "60s"));
        delayComboBox.setValue("5s");
        contactListComboBox.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> loadSelectedContactList(newVal));
        templateSelector.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> loadSelectedTemplate(newVal));
    }

    public void refreshData() {
        contactListNames.setAll("Selecione uma lista");
        FirebaseService.getContactListsAsync(userId, userToken).thenAcceptAsync(lists -> {
            Platform.runLater(() -> {
                if (lists != null)
                    contactListNames.addAll(lists.keySet());
            });
        });

        templateNames.setAll("Selecionar Modelo");
        FirebaseService.getCampaignTemplates(userId, userToken).thenAcceptAsync(templates -> {
            Platform.runLater(() -> {
                if (templates != null)
                    templateNames.addAll(templates.keySet());
            });
        });

        senderAccounts.clear();
        senderAccounts.add(new CheckBox("Conta Principal (Simulada)"));
        senderAccounts.get(0).setSelected(true);
    }

    @SuppressWarnings("unchecked")
    private void loadSelectedContactList(String listName) {
        contactsDisplay.clear();
        contactsInList.clear();
        if (listName == null || listName.equals("Selecione uma lista")) {
            statusLabel.setText("Nenhuma lista selecionada.");
            return;
        }

        statusLabel.setText("A carregar contatos...");
        FirebaseService.getContactsFromListAsync(userId, userToken, listName).thenAcceptAsync(contacts -> {
            Platform.runLater(() -> {
                if (contacts != null) {
                    contacts.forEach((phone, details) -> {
                        if (details instanceof Map) {
                            String name = ((Map<String, String>) details).getOrDefault("name", "Sem Nome");
                            ContactModel contact = new ContactModel();
                            contact.setName(name);
                            contact.setPhone(phone);
                            contactsInList.add(contact);
                        }
                    });
                    updateContactsDisplay();
                } else {
                    statusLabel.setText("Lista de contatos vazia.");
                }
            });
        });
    }

    @SuppressWarnings("unchecked")
    private void loadSelectedTemplate(String templateName) {
        if (templateName == null || templateName.equals("Selecionar Modelo"))
            return;

        FirebaseService.getTemplateData(userId, userToken, templateName).thenAcceptAsync(data -> {
            Platform.runLater(() -> {
                if (data != null && data.containsKey("settings")) {
                    Map<String, Object> settings = (Map<String, Object>) data.get("settings");
                    spintaxMessage.setText((String) settings.getOrDefault("spintax_template", ""));
                    String delay = settings.getOrDefault("delay", "5") + "s";
                    delayComboBox.setValue(delay);
                    statusLabel.setText("Modelo '" + templateName + "' carregado.");
                }
            });
        });
    }

    private void updateContactsDisplay() {
        String text = contactsInList.stream()
                .map(c -> c.getName() + ": " + c.getPhone())
                .collect(Collectors.joining("\n"));
        contactsDisplay.setText(text);
        statusLabel.setText(contactsInList.size() + " contatos carregados.");
    }

    @FXML
    private void handleStartSending() {
        isSending.set(true);
    }

    @FXML
    private void handleTogglePause() {
        isPaused.set(!isPaused.get());
    }

    @FXML
    private void handleStopSending() {
        isSending.set(false);
    }

    @FXML
    private void handleSendTestMessage() {
        isSending.set(true);

    }

    @FXML
    private void handleSaveTemplate() {
        isSending.set(false);
        String templateName = templateSelector.getValue();
        String message = spintaxMessage.getText();
        String delay = delayComboBox.getValue();

        if (templateName == null || templateName.isBlank()) {
            statusLabel.setText("Nome do modelo não pode estar vazio.");
            return;
        }

        FirebaseService.saveTemplate(userId, userToken, templateName, message, delay).thenAccept(success -> {
            if (success) {
                Platform.runLater(() -> {
                    JavaFxUtils.showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Modelo salvo com sucesso.");
                    refreshData();
                });
            } else {
                Platform.runLater(() -> JavaFxUtils.showAlert(Alert.AlertType.ERROR, "Erro",
                        "Não foi possível salvar o modelo."));
            }
        });
    }
}