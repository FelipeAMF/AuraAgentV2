package com.auraagent.controllers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.auraagent.models.WhatsappAccount;
import com.auraagent.services.WhatsappService;
import com.auraagent.utils.JavaFxUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
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
    private Thread warmerThread;

    // Lista de mensagens para simular uma conversa natural
    private static final List<String> WARMER_MESSAGES = List.of(
            "Olá, tudo bem?", "Oi, tudo certo por aqui. E contigo?",
            "Estou bem, obrigado por perguntar!", "Que bom!",
            "Vi aquele filme que recomendaste. Muito bom!", "Sério? Fico feliz que tenhas gostado.",
            "Qual a programação para hoje?", "Acho que vou apenas relaxar em casa.",
            "Ótima ideia. Um bom descanso!", "Para ti também!");

    @Override
    public void initialize(String userId) {
        this.userId = userId;
        accountsListView.setItems(accounts);

        // Associa a visibilidade dos botões ao estado do aquecedor (a correr ou parado)
        startButton.disableProperty().bind(isWarmerRunning);
        stopButton.disableProperty().bind(isWarmerRunning.not());
        intervalSecondsField.disableProperty().bind(isWarmerRunning);
        accountsListView.disableProperty().bind(isWarmerRunning);

        loadAccountsAsync();
    }

    /**
     * Carrega as contas de WhatsApp conectadas usando o WhatsappService e as exibe
     * na lista.
     */
    private void loadAccountsAsync() {
        whatsappService.getStatusAsync().thenAcceptAsync(accountsData -> {
            Platform.runLater(() -> {
                accounts.clear();
                // Filtra apenas as contas que estão de facto conectadas
                List<WhatsappAccount> connectedAccounts = accountsData.stream()
                        .filter(acc -> "Conectado".equals(acc.getStatus()))
                        .collect(Collectors.toList());

                for (WhatsappAccount acc : connectedAccounts) {
                    // Adiciona o ID da sessão e o número de telefone para fácil identificação
                    accounts.add(new CheckBox(acc.getSessionId() + " (" + acc.getPhoneNumber() + ")"));
                }
            });
        });
    }

    /**
     * Ação do botão "Iniciar". Valida as seleções e inicia a thread de aquecimento.
     */
    @FXML
    private void handleStartWarmer() {
        List<String> selectedAccounts = accounts.stream()
                .filter(CheckBox::isSelected)
                .map(cb -> cb.getText().split(" ")[0]) // Pega apenas o ID da sessão
                .collect(Collectors.toList());

        if (selectedAccounts.size() < 2) {
            JavaFxUtils.showAlert(Alert.AlertType.WARNING, "Aviso",
                    "São necessárias pelo menos 2 contas selecionadas para iniciar o aquecedor.");
            return;
        }

        try {
            Integer.parseInt(intervalSecondsField.getText());
        } catch (NumberFormatException e) {
            JavaFxUtils.showAlert(Alert.AlertType.ERROR, "Erro", "O intervalo deve ser um número válido.");
            return;
        }

        isWarmerRunning.set(true);
        logMessage("--- Aquecedor Iniciado ---");

        // --- MOTOR DE AQUECIMENTO EM BACKGROUND ---
        warmerThread = new Thread(() -> {
            Random random = new Random();
            List<String> conversationPartners = new ArrayList<>(selectedAccounts);

            while (isWarmerRunning.get()) {
                try {
                    // Embaralha a lista para que a ordem da conversa mude a cada iteração
                    Collections.shuffle(conversationPartners);

                    String senderId = conversationPartners.get(0);
                    String receiverId = conversationPartners.get(1);

                    // Obtém o número de telefone do destinatário a partir dos dados carregados
                    String receiverPhone = getPhoneFromSessionId(receiverId);

                    if (receiverPhone == null) {
                        logMessage("ERRO: Não foi possível encontrar o número de telefone para a sessão " + receiverId);
                        continue; // Pula para a próxima iteração
                    }

                    String message = WARMER_MESSAGES.get(random.nextInt(WARMER_MESSAGES.size()));

                    logMessage(String.format("A enviar de '%s' para '%s': \"%s\"", senderId, receiverId, message));

                    // Envia a mensagem de verdade
                    whatsappService.sendMessageAsync(senderId, receiverPhone, message);

                    // Aguarda o intervalo definido pelo utilizador
                    int delayMs = Integer.parseInt(intervalSecondsField.getText()) * 1000;
                    Thread.sleep(delayMs);

                } catch (InterruptedException e) {
                    // A thread foi interrompida, provavelmente pelo botão de parar
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logMessage("ERRO inesperado no ciclo de aquecimento: " + e.getMessage());
                }
            }
            logMessage("--- Ciclo de aquecimento finalizado. ---");
        });

        warmerThread.setDaemon(true);
        warmerThread.start();
    }

    /**
     * Ação do botão "Parar". Interrompe a thread de aquecimento.
     */
    @FXML
    private void handleStopWarmer() {
        isWarmerRunning.set(false);
        if (warmerThread != null) {
            warmerThread.interrupt(); // Interrompe a thread se ela estiver a dormir (Thread.sleep)
        }
        logMessage("--- Aquecedor Parado pelo utilizador ---");
    }

    /**
     * Adiciona uma mensagem formatada com data/hora na área de log.
     * 
     * @param message A mensagem a ser exibida.
     */
    private void logMessage(String message) {
        Platform.runLater(() -> {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            logOutputArea.appendText(String.format("[%s] %s\n", timestamp, message));
        });
    }

    /**
     * Método auxiliar para encontrar o número de telefone correspondente a um ID de
     * sessão.
     */
    private String getPhoneFromSessionId(String sessionId) {
        return accounts.stream()
                .map(CheckBox::getText)
                .filter(text -> text.startsWith(sessionId + " ("))
                .findFirst()
                .map(text -> text.substring(text.indexOf('(') + 1, text.indexOf(')')))
                .orElse(null);
    }

    public static CompletableFuture<Boolean> saveAIPersonality(String userId, String accountName, String prompt) {
        String path = "users/" + userId + "/ai_settings/" + accountName;
        Map<String, Object> personality = Map.of("personality_prompt", prompt);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path);
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        com.google.api.core.ApiFuture<Void> apiFuture = ref.updateChildrenAsync(personality);
        com.google.api.core.ApiFutures.addCallback(apiFuture, new com.google.api.core.ApiFutureCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                future.complete(true);
            }

            @Override
            public void onFailure(Throwable t) {
                future.completeExceptionally(t);
            }
        }, Runnable::run);
        return future;
    }

    /**
     * Carrega o prompt de personalidade da IA para uma conta específica.
     * 
     * @param userId      ID do utilizador.
     * @param accountName O nome da conta.
     * @return Um CompletableFuture contendo o prompt de personalidade.
     */
    public static CompletableFuture<String> getAIPersonality(String userId, String accountName) {
        CompletableFuture<String> future = new CompletableFuture<>();
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users/" + userId + "/ai_settings/" + accountName + "/personality_prompt");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                future.complete(dataSnapshot.exists() ? dataSnapshot.getValue(String.class) : "");
            }

            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(error.toException());
            }
        });
        return future;
    }
}