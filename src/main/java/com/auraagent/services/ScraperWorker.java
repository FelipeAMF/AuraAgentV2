package com.auraagent.services;

<<<<<<< HEAD
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Paths; // Import necessário
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Importações para encontrar a janela (específico para Windows)
import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;
=======
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
>>>>>>> edf476c85c54429cd2c4a02aa6712b1e42808e3f

public class ScraperWorker {

    private final int stopThreshold;
    private final int scrollPauseMs;
<<<<<<< HEAD
    private final ITesseract tesseract;
    private final Set<String> alreadyFoundNumbers = new HashSet<>();
=======
>>>>>>> edf476c85c54429cd2c4a02aa6712b1e42808e3f

    public ScraperWorker(int stopThreshold, double scrollPause) {
        this.stopThreshold = stopThreshold;
        this.scrollPauseMs = (int) (scrollPause * 1000);
<<<<<<< HEAD

        // Inicializa o motor de OCR
        this.tesseract = new Tesseract();

        // --- ALTERAÇÃO PRINCIPAL AQUI ---
        // Constrói o caminho para a pasta 'tessdata' dentro do diretório
        // 'vendor/tesseract'
        // Isto torna a aplicação portátil, não dependendo de uma instalação do
        // Tesseract no sistema.
        String tessDataPath = Paths.get(System.getProperty("user.dir"), "vendor", "tesseract", "tessdata").toString();

        tesseract.setDatapath(tessDataPath);
        tesseract.setLanguage("por"); // Define o idioma para Português
    }

    /**
     * Executa o processo de extração com OCR real.
     */
    public void run(Consumer<String> progress, AtomicBoolean cancellationToken) {
        int scrollsWithoutFinding = 0;

        try {
            // Passo 1: Encontrar a janela do scrcpy
            progress.accept("A localizar a janela do telemóvel...");
            HWND windowHandle = User32.INSTANCE.FindWindow(null, "AuraAgent-SCRCPY");
            if (windowHandle == null) {
                progress.accept("ERRO: Janela do scrcpy não encontrada. Verifique se a conexão foi bem-sucedida.");
                return;
            }
            User32.INSTANCE.SetForegroundWindow(windowHandle);
            Thread.sleep(500); // Dar tempo para a janela focar

            RECT rect = new RECT();
            User32.INSTANCE.GetWindowRect(windowHandle, rect);
            Rectangle windowRect = rect.toRectangle();

            Robot robot = new Robot();

            while (scrollsWithoutFinding < stopThreshold && !cancellationToken.get()) {

                // Passo 2: Capturar o ecrã da janela
                progress.accept("A analisar o ecrã...");
                BufferedImage screenshot = robot.createScreenCapture(windowRect);

                // Passo 3: Executar o OCR na imagem capturada
                String extractedText;
                try {
                    extractedText = tesseract.doOCR(screenshot);
                } catch (TesseractException e) {
                    progress.accept("Erro de OCR: " + e.getMessage());
                    continue; // Pula para a próxima iteração
                }

                // Passo 4: Encontrar números no texto extraído
                Set<String> numbersInThisScreen = findPhoneNumbers(extractedText);

                boolean newNumberFound = false;
                for (String number : numbersInThisScreen) {
                    if (alreadyFoundNumbers.add(number)) { // .add() retorna true se o item for novo
                        progress.accept("Número encontrado: " + number);
                        newNumberFound = true;
                    }
                }

                if (newNumberFound) {
                    scrollsWithoutFinding = 0; // Reinicia a contagem
                } else {
                    scrollsWithoutFinding++;
                    progress.accept(String.format("Nenhum número novo detetado (%d/%d)...", scrollsWithoutFinding,
                            stopThreshold));
                }

                // Passo 5: Simular a rolagem (scroll)
                if (scrollsWithoutFinding < stopThreshold) {
                    progress.accept("A rolar o ecrã para baixo...");
                    // Move o rato para o meio da janela e usa a roda de rolagem
                    int centerX = windowRect.x + windowRect.width / 2;
                    int centerY = windowRect.y + windowRect.height / 2;
                    robot.mouseMove(centerX, centerY);
                    robot.mouseWheel(-5); // O valor negativo representa rolar para baixo
                }

                Thread.sleep(scrollPauseMs);
            }

            // Mensagem final
=======
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

>>>>>>> edf476c85c54429cd2c4a02aa6712b1e42808e3f
            if (cancellationToken.get()) {
                progress.accept("Extração parada pelo utilizador.");
            } else {
                progress.accept("Extração concluída (limite de tentativas atingido).");
            }
<<<<<<< HEAD

        } catch (Exception e) {
            Thread.currentThread().interrupt();
            progress.accept("ERRO CRÍTICO no worker: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Usa expressões regulares para encontrar padrões de números de telefone
     * brasileiros.
     * 
     * @param text O texto extraído pelo OCR.
     * @return Um conjunto (Set) de números de telefone únicos encontrados.
     */
    private Set<String> findPhoneNumbers(String text) {
        Set<String> numbers = new HashSet<>();
        // RegEx para encontrar números no formato: +55 (DD) 9XXXX-XXXX ou variações
        Pattern pattern = Pattern.compile("(?:\\+55\\s?)?(?:\\(?\\d{2}\\)?\\s?)?9\\d{4}[-.\\s]?\\d{4}");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            // Limpa o número encontrado, deixando apenas os dígitos
            String sanitizedNumber = matcher.group().replaceAll("[^0-9]", "");
            if (sanitizedNumber.length() >= 10) { // Validação mínima
                numbers.add(sanitizedNumber);
            }
        }
        return numbers;
    }
=======
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            progress.accept("Extração interrompida.");
        }
    }
>>>>>>> edf476c85c54429cd2c4a02aa6712b1e42808e3f
}