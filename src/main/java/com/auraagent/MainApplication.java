package com.auraagent;

import java.io.IOException;

import com.auraagent.controllers.LoginController;
import com.auraagent.controllers.MainAppController;
import com.auraagent.services.FirebaseService;
import com.auraagent.services.ProcessManager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MainApplication extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        this.primaryStage = stage;

        // --- LÓGICA DE INICIALIZAÇÃO ---
        // 1. Conecta-se aos serviços do Firebase para autenticação e banco de dados.
        FirebaseService.initialize();
        // 2. Inicia o servidor Node.js que controla a API do WhatsApp em segundo plano.
        ProcessManager.startNodeServer();

        // --- CONFIGURAÇÃO DA JANELA PRINCIPAL ---
        primaryStage.setTitle("Aura Agent");
        // Adiciona o ícone da aplicação à janela
        try {
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo_aura.ico")));
        } catch (Exception e) {
            System.err.println("Erro ao carregar o ícone da aplicação: " + e.getMessage());
        }

        // 3. Exibe a tela de login como a primeira visão.
        showLoginView();
        primaryStage.show();

        // 4. Garante que, ao fechar a janela, o processo do Node.js seja finalizado.
        primaryStage.setOnCloseRequest(event -> {
            ProcessManager.stopNodeServer();
        });
    }

    /**
     * Carrega e exibe a tela de login (LoginView.fxml).
     * Configura o callback que será chamado em caso de login bem-sucedido.
     */
    private void showLoginView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auraagent/views/LoginView.fxml"));
            Parent root = loader.load();

            // Obtém o controller da tela de login para definir a ação de sucesso
            LoginController controller = loader.getController();
            // Quando o login for bem-sucedido, chama o método showMainAppView
            controller.setOnLoginSuccess(this::showMainAppView);

            primaryStage.setScene(new Scene(root, 1200, 800));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Carrega e exibe a tela principal da aplicação (MainAppView.fxml) após o
     * login.
     * 
     * @param userId O ID do usuário autenticado, vindo do Firebase.
     */
    private void showMainAppView(String userId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auraagent/views/MainAppView.fxml"));
            Parent root = loader.load();

            // Obtém o controller principal para inicializá-lo com os dados do usuário
            MainAppController controller = loader.getController();
            controller.initialize(userId); // Passa o ID do usuário para o controller principal

            primaryStage.setScene(new Scene(root, 1200, 800));
            primaryStage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}