package com.auraagent.controllers;

import java.util.ArrayList;
import java.util.List;
<<<<<<< HEAD
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
=======
import java.util.Map;
>>>>>>> edf476c85c54429cd2c4a02aa6712b1e42808e3f
import java.util.stream.Collectors;

import com.auraagent.models.ContactModel;
import com.auraagent.services.FirebaseService;
<<<<<<< HEAD
import com.auraagent.services.WhatsappService;
import com.auraagent.utils.JavaFxUtils;
=======
>>>>>>> edf476c85c54429cd2c4a02aa6712b1e42808e3f

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
<<<<<<< HEAD
import javafx.scene.control.Alert;
=======
>>>>>>> edf476c85c54429cd2c4a02aa6712b1e42808e3f
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
<<<<<<< HEAD
import javafx.scene.control.TextInputDialog;
=======
>>>>>>> edf476c85c54429cd2c4a02aa6712b1e42808e3f

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
<<<<<<< HEAD
    private final WhatsappService whatsappService = new WhatsappService();
    private Thread campaignThread;
=======
>>>>>>> edf476c85c54429cd2c4a02aa6712b1e42808e3f

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
<<<<<<< HEAD
        // Lógica para carregar listas de contatos e modelos do Firebase...
        // (O código original aqui já está correto)
=======
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
>>>>>>> edf476c85c54429cd2c4a02aa6712b1e42808e3f
    }

    @SuppressWarnings("unchecked")
    private void loadSelectedContactList(String listName) {
<<<<<<< HEAD
        // Lógica para carregar os contatos da lista selecionada...
        // (O código original aqui já está correto)
=======
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
>>>>>>> edf476c85c54429cd2c4a02aa6712b1e42808e3f
    }

    @SuppressWarnings("unchecked")
    private void loadSelectedTemplate(String templateName) {
<<<<<<< HEAD
        // Lógica para carregar o conteúdo de um modelo salvo...
        // (O código original aqui já está correto)
    }

    private void updateContactsDisplay() {
        // Lógica para exibir os contatos carregados na tela...
        // (O código original aqui já está correto)
    }

    /**
     * Processa uma string com Spintax e retorna uma variação aleatória.
     * Ex: "Olá, {tudo bem|como vai}?" pode retornar "Olá, tudo bem?" ou "Olá, como
     * vai?".
     */
    private String parseSpintax(String text) {
        Random random = new Random();
        Pattern pattern = Pattern.compile("\\{([^\\{\\}]+)\\}");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String[] options = matcher.group(1).split("\\|");
            text = text.replace(matcher.group(0), options[random.nextInt(options.length)]);
            matcher = pattern.matcher(text);
        }
        return text;
=======
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
>>>>>>> edf476c85c54429cd2c4a02aa6712b1e42808e3f
    }

    @FXML
    private void handleStartSending() {
<<<<<<< HEAD
        List<String> selectedSenders = senderAccounts.stream()
                .filter(CheckBox::isSelected)
                .map(cb -> cb.getText().split(" ")[0]) // Pega apenas o ID da sessão
                .collect(Collectors.toList());

        if (contactsInList.isEmpty()) {
            JavaFxUtils.showAlert(Alert.AlertType.WARNING, "Aviso", "A lista de contatos está vazia.");
            return;
        }
        if (selectedSenders.isEmpty()) {
            JavaFxUtils.showAlert(Alert.AlertType.WARNING, "Aviso", "Selecione pelo menos uma conta para envio.");
            return;
        }

        isSending.set(true);
        isPaused.set(false);

        // --- MOTOR DE ENVIO EM BACKGROUND ---
        campaignThread = new Thread(() -> {
            AtomicInteger sentCount = new AtomicInteger(0);
            int totalContacts = contactsInList.size();

            for (int i = 0; i < totalContacts; i++) {
                // Pausa a thread se o botão de pausa foi clicado
                while (isPaused.get()) {
                    try {
                        Thread.sleep(1000); // Aguarda 1 segundo antes de checar novamente
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                // Para a campanha se o botão de parar foi clicado
                if (!isSending.get()) {
                    Platform.runLater(() -> statusLabel.setText("Campanha interrompida pelo usuário."));
                    break;
                }

                ContactModel contact = contactsInList.get(i);
                // Faz um rodízio entre as contas de envio
                String sender = selectedSenders.get(i % selectedSenders.size());
                String messageToSend = parseSpintax(spintaxMessage.getText()).replace("{nome}", contact.getName());

                final int currentProgress = i + 1;
                Platform.runLater(() -> {
                    statusLabel.setText(String.format("Enviando %d de %d: %s para %s", currentProgress, totalContacts,
                            sender, contact.getPhone()));
                    progressBar.setProgress((double) currentProgress / totalContacts);
                });

                // Envia a mensagem de verdade
                whatsappService.sendMessageAsync(sender, contact.getPhone(), messageToSend);

                // Aguarda o delay definido
                try {
                    int delayMs = Integer.parseInt(delayComboBox.getValue().replace("s", "")) * 1000;
                    Thread.sleep(delayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            Platform.runLater(() -> {
                isSending.set(false);
                statusLabel.setText("Campanha finalizada.");
            });
        });

        campaignThread.setDaemon(true);
        campaignThread.start();
=======
        isSending.set(true);
>>>>>>> edf476c85c54429cd2c4a02aa6712b1e42808e3f
    }

    @FXML
    private void handleTogglePause() {
        isPaused.set(!isPaused.get());
<<<<<<< HEAD
        statusLabel.setText(isPaused.get() ? "Campanha pausada." : "Campanha retomada.");
=======
>>>>>>> edf476c85c54429cd2c4a02aa6712b1e42808e3f
    }

    @FXML
    private void handleStopSending() {
<<<<<<< HEAD
        if (JavaFxUtils.showConfirmation(Alert.AlertType.CONFIRMATION, "Parar Campanha",
                "Tem certeza que deseja parar o envio?")) {
            isSending.set(false);
            isPaused.set(false);
        }
=======
        isSending.set(false);
>>>>>>> edf476c85c54429cd2c4a02aa6712b1e42808e3f
    }

    @FXML
    private void handleSendTestMessage() {
<<<<<<< HEAD
        List<String> selectedSenders = senderAccounts.stream()
                .filter(CheckBox::isSelected)
                .map(cb -> cb.getText().split(" ")[0])
                .collect(Collectors.toList());

        if (selectedSenders.isEmpty()) {
            JavaFxUtils.showAlert(Alert.AlertType.WARNING, "Aviso", "Selecione uma conta de envio para o teste.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Enviar Teste");
        dialog.setHeaderText("Digite o número de telefone para o teste (formato internacional, ex: 55619...):");
        dialog.setContentText("Número:");
        Optional<String> result = dialog.showAndWait();

        result.ifPresent(number -> {
            if (!number.isBlank()) {
                String testMessage = parseSpintax(spintaxMessage.getText()).replace("{nome}", "Teste");
                statusLabel.setText("Enviando mensagem de teste para " + number);
                whatsappService.sendMessageAsync(selectedSenders.get(0), number, testMessage)
                        .thenAccept(success -> Platform.runLater(() -> {
                            statusLabel.setText(
                                    success ? "Mensagem de teste enviada!" : "Falha ao enviar mensagem de teste.");
                        }));
            }
        });
=======
        isSending.set(true);

>>>>>>> edf476c85c54429cd2c4a02aa6712b1e42808e3f
    }

    @FXML
    private void handleSaveTemplate() {
<<<<<<< HEAD
        // Usa o editor de texto do ComboBox para permitir que o usuário digite um novo
        // nome
        String templateName = templateSelector.getEditor().getText();
=======
        isSending.set(false);
        String templateName = templateSelector.getValue();
>>>>>>> edf476c85c54429cd2c4a02aa6712b1e42808e3f
        String message = spintaxMessage.getText();
        String delay = delayComboBox.getValue();

        if (templateName == null || templateName.isBlank()) {
<<<<<<< HEAD
            JavaFxUtils.showAlert(Alert.AlertType.WARNING, "Nome Inválido",
                    "Digite um nome para o modelo antes de salvar.");
            return;
        }

        // --- LÓGICA REAL ---
=======
            statusLabel.setText("Nome do modelo não pode estar vazio.");
            return;
        }

>>>>>>> edf476c85c54429cd2c4a02aa6712b1e42808e3f
        FirebaseService.saveTemplate(userId, userToken, templateName, message, delay).thenAccept(success -> {
            if (success) {
                Platform.runLater(() -> {
                    JavaFxUtils.showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Modelo salvo com sucesso.");
<<<<<<< HEAD
                    refreshData(); // Atualiza a lista de modelos
=======
                    refreshData();
>>>>>>> edf476c85c54429cd2c4a02aa6712b1e42808e3f
                });
            } else {
                Platform.runLater(() -> JavaFxUtils.showAlert(Alert.AlertType.ERROR, "Erro",
                        "Não foi possível salvar o modelo."));
            }
        });
    }
}