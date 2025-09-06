package com.auraagent.services;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class AIService {

    /**
     * Simula a geração de uma resposta pela IA.
     * @param history O histórico da conversa (não utilizado nesta simulação).
     * @param systemPrompt A personalidade da IA.
     * @return Um CompletableFuture que conterá a resposta gerada pela IA.
     */
    public static CompletableFuture<String> generateResponseAsync(Object history, String systemPrompt) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Simula o tempo de processamento da IA
                TimeUnit.MILLISECONDS.sleep(1500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            if (systemPrompt.toLowerCase().contains("feliz")) {
                return "Olá! Que dia maravilhoso para conversar! 😄";
            }
            if (systemPrompt.toLowerCase().contains("formal")) {
                return "Prezado usuário, como posso ser útil na data de hoje?";
            }

            return "Olá, eu sou uma IA. Como posso te ajudar?";
        });
    }
}