package com.auraagent;

<<<<<<< HEAD
import java.io.IOException;

=======
>>>>>>> edf476c85c54429cd2c4a02aa6712b1e42808e3f
import com.auraagent.controllers.LoginController;
import com.auraagent.controllers.MainAppController;
import com.auraagent.services.FirebaseService;
import com.auraagent.services.ProcessManager;
<<<<<<< HEAD

=======
>>>>>>> edf476c85c54429cd2c4a02aa6712b1e42808e3f
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
<<<<<<< HEAD
import javafx.scene.image.Image;
import javafx.stage.Stage;

=======
import javafx.stage.Stage;

import java.io.IOException;

>>>>>>> edf476c85c54429cd2c4a02aa6712b1e42808e3f
public class MainApplication extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        this.primaryStage = stage;
<<<<<<< HEAD

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
=======
        FirebaseService.initialize(); // Inicializa a conexão com o Firebase
        ProcessManager.startNodeServer(); // Inicia o servidor Node.js

        primaryStage.setTitle("Aura Agent");
        showLoginView();
        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> {
            ProcessManager.stopNodeServer(); // Garante que o processo Node.js é encerrado
        });
    }

>>>>>>> edf476c85c54429cd2c4a02aa6712b1e42808e3f
    private void showLoginView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auraagent/views/LoginView.fxml"));
            Parent root = loader.load();

<<<<<<< HEAD
            // Obtém o controller da tela de login para definir a ação de sucesso
            LoginController controller = loader.getController();
            // Quando o login for bem-sucedido, chama o método showMainAppView
=======
            LoginController controller = loader.getController();
>>>>>>> edf476c85c54429cd2c4a02aa6712b1e42808e3f
            controller.setOnLoginSuccess(this::showMainAppView);

            primaryStage.setScene(new Scene(root, 1200, 800));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

<<<<<<< HEAD
    /**
     * Carrega e exibe a tela principal da aplicação (MainAppView.fxml) após o
     * login.
     * 
     * @param userId O ID do usuário autenticado, vindo do Firebase.
     */
=======
>>>>>>> edf476c85c54429cd2c4a02aa6712b1e42808e3f
    private void showMainAppView(String userId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auraagent/views/MainAppView.fxml"));
            Parent root = loader.load();
<<<<<<< HEAD

            // Obtém o controller principal para inicializá-lo com os dados do usuário
            MainAppController controller = loader.getController();
            controller.initialize(userId); // Passa o ID do usuário para o controller principal
=======
            
            MainAppController controller = loader.getController();
            controller.initialize(userId); // Passa o ID do utilizador para o controller principal
>>>>>>> edf476c85c54429cd2c4a02aa6712b1e42808e3f

            primaryStage.setScene(new Scene(root, 1200, 800));
            primaryStage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

<<<<<<< HEAD
=======

>>>>>>> edf476c85c54429cd2c4a02aa6712b1e42808e3f
    public static void main(String[] args) {
        launch(args);
    }
}