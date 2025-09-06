package com.auraagent.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AIService {

    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String API_URL = "http://localhost:8080/completion"; // URL do servidor de IA local

    /**
     * Envia o histórico da conversa e o prompt do sistema para o servidor de IA
     * local
     * e retorna a resposta gerada.
     * 
     * @param history      O histórico da conversa (lista de mapas com "role" e
     *                     "content").
     * @param systemPrompt A personalidade da IA.
     * @return Um CompletableFuture que conterá a resposta gerada pela IA.
     */
    public static CompletableFuture<String> generateResponseAsync(List<Object> history, String systemPrompt) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Monta o corpo da requisição JSON
                Map<String, Object> payload = new HashMap<>();
                payload.put("messages", history);
                payload.put("system_prompt", systemPrompt); // Envia a personalidade como prompt de sistema

                String jsonPayload = objectMapper.writeValueAsString(payload);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_URL))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                        .build();

                // Envia a requisição e espera pela resposta
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    // Extrai o conteúdo da resposta JSON
                    Map<String, Object> responseMap = objectMapper.readValue(response.body(), HashMap.class);
                    return (String) responseMap.get("content");
                } else {
                    return "Erro: O servidor de IA respondeu com o código " + response.statusCode();
                }

            } catch (Exception e) {
                Thread.currentThread().interrupt();
                System.err.println("Erro ao comunicar com o servidor de IA: " + e.getMessage());
                return "Não foi possível conectar ao servidor de IA local. Verifique se ele está ativo.";
            }
        });
    }
}