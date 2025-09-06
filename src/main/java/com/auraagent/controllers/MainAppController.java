package com.auraagent.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

public class MainAppController {

    @FXML
    private BorderPane mainPane;
    @FXML
    private ToggleGroup navGroup;

    private String userId;
    // Cache para armazenar as telas já carregadas e evitar recarregá-las
    private final Map<String, Pane> viewCache = new HashMap<>();

    public void initialize(String userId) {
        this.userId = userId;
        // Carrega a primeira tela (Campanhas) por padrão
        loadView("Campaign");

        // Lógica para trocar de tela ao clicar nos botões de navegação
        navGroup.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
            if (newToggle != null) {
                RadioButton selectedRadio = (RadioButton) newToggle;
                loadView(selectedRadio.getId());
            }
        });
    }

    /**
     * Carrega a view FXML correspondente ao nome fornecido e a exibe no painel
     * central.
     * Utiliza um cache para não recarregar a mesma view múltiplas vezes.
     *
     * @param viewName O nome da view a ser carregada (ex: "Campaign", "Contacts").
     */
    private void loadView(String viewName) {
        try {
            // Se a tela já foi carregada antes, apenas a exibe novamente
            if (viewCache.containsKey(viewName)) {
                mainPane.setCenter(viewCache.get(viewName));
            } else {
                // Se for a primeira vez, carrega o arquivo FXML
                String fxmlPath = "/com/auraagent/views/" + viewName + "View.fxml";
                URL resourceUrl = getClass().getResource(fxmlPath);

                // LÓGICA DE VERIFICAÇÃO: Se o arquivo FXML não for encontrado, exibe um erro
                // claro.
                if (resourceUrl == null) {
                    System.err
                            .println("Erro Crítico: Não foi possível encontrar o arquivo FXML no caminho: " + fxmlPath);
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erro ao Carregar a Interface");
                    alert.setHeaderText("O arquivo " + viewName + "View.fxml não foi encontrado.");
                    alert.setContentText(
                            "Verifique se o arquivo está na pasta correta e se o projeto está configurado para incluir recursos FXML na compilação.");
                    alert.showAndWait();
                    return;
                }

                FXMLLoader loader = new FXMLLoader(resourceUrl);
                Pane view = loader.load();

                // Passa o ID do usuário para o controller da nova tela, se ele precisar
                Object controller = loader.getController();
                if (controller instanceof InitializableController) {
                    ((InitializableController) controller).initialize(userId);
                }

                // Armazena a tela carregada no cache e a exibe
                viewCache.put(viewName, view);
                mainPane.setCenter(view);
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Em caso de outro erro de carregamento, mostra uma exceção no console.
        }
    }

    // Interface para garantir que todos os controllers de "sub-telas" possam ser
    // inicializados
    public interface InitializableController {
        void initialize(String userId);
    }
}