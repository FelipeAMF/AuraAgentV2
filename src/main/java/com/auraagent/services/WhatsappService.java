package com.auraagent.services;

import com.auraagent.models.WhatsappAccount;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

public class WhatsappService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String API_URL = "http://localhost:3000";

    // O padrão Observer pode ser implementado em JavaFX com Properties e Listeners
    // Por simplicidade, deixaremos a lógica de polling no ViewModel por agora.

    public CompletableFuture<List<WhatsappAccount>> getStatusAsync() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/status"))
                .GET()
                .build();
        
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    try {
                        return objectMapper.readValue(response.body(), new TypeReference<List<WhatsappAccount>>() {});
                    } catch (Exception e) {
                        e.printStackTrace();
                        return List.of(); // Retorna lista vazia em caso de erro
                    }
                });
    }

    // Adicione os métodos connectAsync, disconnectAsync, sendMessageAsync aqui,
    // seguindo o padrão de requisições HTTP do código C#.
}