package com.auraagent;

import com.auraagent.controllers.LoginController;
import com.auraagent.controllers.MainAppController;
import com.auraagent.services.FirebaseService;
import com.auraagent.services.ProcessManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        this.primaryStage = stage;
        FirebaseService.initialize(); // Inicializa a conexão com o Firebase
        ProcessManager.startNodeServer(); // Inicia o servidor Node.js

        primaryStage.setTitle("Aura Agent");
        showLoginView();
        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> {
            ProcessManager.stopNodeServer(); // Garante que o processo Node.js é encerrado
        });
    }

    private void showLoginView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auraagent/views/LoginView.fxml"));
            Parent root = loader.load();

            LoginController controller = loader.getController();
            controller.setOnLoginSuccess(this::showMainAppView);

            primaryStage.setScene(new Scene(root, 1200, 800));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showMainAppView(String userId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auraagent/views/MainAppView.fxml"));
            Parent root = loader.load();
            
            MainAppController controller = loader.getController();
            controller.initialize(userId); // Passa o ID do utilizador para o controller principal

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