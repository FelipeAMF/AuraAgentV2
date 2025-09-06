package com.auraagent.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class ProcessManager {

    private static Process nodeProcess;

    public static void startNodeServer() {
        try {
            String serverDir = new File(System.getProperty("user.dir"), "servidor-node").getAbsolutePath();
            String scriptPath = new File(serverDir, "index.js").getAbsolutePath();

            if (!new File(scriptPath).exists()) {
                System.err.println("Erro: 'servidor-node/index.js' não encontrado.");
                return;
            }

            ProcessBuilder pb = new ProcessBuilder("node", scriptPath);
            pb.directory(new File(serverDir));
            nodeProcess = pb.start();

            // Threads para consumir a saída do processo e evitar bloqueios
            new Thread(() -> logProcessOutput(new BufferedReader(new InputStreamReader(nodeProcess.getInputStream())), "NODE_OUTPUT")).start();
            new Thread(() -> logProcessOutput(new BufferedReader(new InputStreamReader(nodeProcess.getErrorStream())), "NODE_ERROR")).start();

            System.out.println("Servidor Node.js iniciado com PID: " + nodeProcess.pid());
        } catch (IOException e) {
            System.err.println("Erro crítico ao iniciar Node.js: " + e.getMessage());
        }
    }

    public static void stopNodeServer() {
        if (nodeProcess != null && nodeProcess.isAlive()) {
            nodeProcess.destroyForcibly();
            System.out.println("Servidor Node.js parado.");
        }
    }

    private static void logProcessOutput(BufferedReader reader, String prefix) {
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[" + prefix + "] " + line);
            }
        } catch (IOException e) {
            // O processo foi terminado, a stream foi fechada.
        }
    }
}