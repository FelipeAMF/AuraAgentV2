package com.auraagent.services;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class ScraperWorker {

    private final int stopThreshold;
    private final int scrollPauseMs;

    public ScraperWorker(int stopThreshold, double scrollPause) {
        this.stopThreshold = stopThreshold;
        this.scrollPauseMs = (int) (scrollPause * 1000);
    }

    public void run(Consumer<String> progress, AtomicBoolean cancellationToken) {
        int scrollsWithoutFinding = 0;
        Random random = new Random();

        try {
            while (scrollsWithoutFinding < stopThreshold && !cancellationToken.get()) {
                progress.accept("A rolar o ecrã e a procurar números...");
                Thread.sleep(scrollPauseMs);

                // SIMULAÇÃO: 20% de chance de encontrar um número
                if (random.nextInt(5) == 0) {
                    String fakeNumber = String.format("+55 61 9%04d-%04d", random.nextInt(10000), random.nextInt(10000));
                    progress.accept("Número encontrado: " + fakeNumber);
                    scrollsWithoutFinding = 0; // Reseta a contagem
                } else {
                    scrollsWithoutFinding++;
                    progress.accept(String.format("Nenhum número novo encontrado (%d/%d)...", scrollsWithoutFinding, stopThreshold));
                }
            }

            if (cancellationToken.get()) {
                progress.accept("Extração parada pelo utilizador.");
            } else {
                progress.accept("Extração concluída (limite de tentativas atingido).");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            progress.accept("Extração interrompida.");
        }
    }
}