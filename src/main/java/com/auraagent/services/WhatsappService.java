package com.auraagent.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.auraagent.models.WhatsappAccount;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WhatsappService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    // A URL base da API do servidor Node.js que está rodando localmente.
    private static final String API_URL = "http://localhost:3000";

    /**
     * Busca o status de todas as sessões de WhatsApp ativas no servidor Node.
     * 
     * @return Uma lista de objetos WhatsappAccount com os dados de cada sessão.
     */
    public CompletableFuture<List<WhatsappAccount>> getStatusAsync() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/status"))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    try {
                        // Converte a resposta JSON em uma lista de WhatsappAccount.
                        return objectMapper.readValue(response.body(), new TypeReference<List<WhatsappAccount>>() {
                        });
                    } catch (Exception e) {
                        System.err.println("Erro ao obter status do WhatsApp: " + e.getMessage());
                        return List.of(); // Retorna lista vazia em caso de erro.
                    }
                });
    }

    /**
     * Envia uma requisição para o servidor Node para iniciar uma nova sessão de
     * WhatsApp.
     * 
     * @param sessionId O nome/ID da sessão a ser iniciada.
     * @return Um CompletableFuture que indica se a requisição foi bem-sucedida
     *         (status 200).
     */
    public CompletableFuture<Boolean> connectAsync(String sessionId) {
        // Cria um corpo JSON simples para a requisição. Ex: {"sessionId": "Nova_Conta"}
        String jsonPayload = "{\"sessionId\": \"" + sessionId + "\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/start")) // Endpoint para iniciar a sessão.
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> response.statusCode() == 200); // Retorna true se a requisição teve sucesso.
    }

    /**
     * Envia uma requisição para o servidor Node para desconectar e remover uma
     * sessão.
     * 
     * @param sessionId O nome/ID da sessão a ser desconectada.
     * @return Um CompletableFuture que indica se a requisição foi bem-sucedida.
     */
    public CompletableFuture<Boolean> disconnectAsync(String sessionId) {
        String jsonPayload = "{\"sessionId\": \"" + sessionId + "\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/logout")) // Endpoint para encerrar a sessão.
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> response.statusCode() == 200);
    }

    /**
     * Envia uma mensagem de texto para um número específico através de uma sessão
     * ativa.
     * 
     * @param sessionId A sessão que será usada para enviar a mensagem.
     * @param number    O número de telefone do destinatário (ex: "5561999998888").
     * @param message   O conteúdo da mensagem a ser enviada.
     * @return Um CompletableFuture que indica se a requisição foi bem-sucedida.
     */
    public CompletableFuture<Boolean> sendMessageAsync(String sessionId, String number, String message) {
        try {
            // Cria um mapa para facilitar a conversão para JSON.
            Map<String, String> payload = Map.of(
                    "sessionId", sessionId,
                    "number", number,
                    "message", message);

            String jsonPayload = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + "/send-message")) // Endpoint para enviar mensagens.
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> response.statusCode() == 200);

        } catch (Exception e) {
            System.err.println("Erro ao construir a requisição de envio de mensagem: " + e.getMessage());
            return CompletableFuture.completedFuture(false);
        }
    }
}