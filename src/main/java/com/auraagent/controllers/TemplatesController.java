package com.auraagent.controllers;

import com.auraagent.models.TemplateModel;
<<<<<<< HEAD
import com.auraagent.services.FirebaseService;
import com.auraagent.utils.JavaFxUtils;

import javafx.application.Platform;
=======
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
>>>>>>> edf476c85c54429cd2c4a02aa6712b1e42808e3f
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
<<<<<<< HEAD
import javafx.scene.layout.VBox;

import java.util.Map;

public class TemplatesController implements MainAppController.InitializableController {

    @FXML
    private ListView<TemplateModel> templatesListView;
    @FXML
    private VBox editorPane;
    @FXML
    private TextField nameField;
    @FXML
    private TextArea spintaxContentArea;
    @FXML
    private TextField delayField;
    @FXML
    private Button saveButton, deleteButton;

    private String userId;
    private String userToken;

    private final ObservableList<TemplateModel> templates = FXCollections.observableArrayList();
    // A propriedade a seguir rastreia o modelo atualmente selecionado na lista
    private final SimpleObjectProperty<TemplateModel> selectedTemplate = new SimpleObjectProperty<>();
=======
import javafx.scene.layout.Pane;

public class TemplatesController implements MainAppController.InitializableController {

    @FXML private ListView<TemplateModel> templatesListView;
    @FXML private Pane editorPane;
    @FXML private TextField nameField;
    @FXML private TextArea spintaxContentArea;
    @FXML private TextField delayField;
    @FXML private Button saveButton;

    private String userId;

    private final ObservableList<TemplateModel> templates = FXCollections.observableArrayList();
    private final SimpleObjectProperty<TemplateModel> selectedTemplate = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<TemplateModel> editingTemplate = new SimpleObjectProperty<>();
    private final SimpleBooleanProperty isNewTemplate = new SimpleBooleanProperty(false);
>>>>>>> edf476c85c54429cd2c4a02aa6712b1e42808e3f

    @Override
    public void initialize(String userId) {
        this.userId = userId;

        templatesListView.setItems(templates);
<<<<<<< HEAD
        // Define como o nome do modelo será exibido na lista
=======
>>>>>>> edf476c85c54429cd2c4a02aa6712b1e42808e3f
        templatesListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(TemplateModel item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getName());
            }
        });
<<<<<<< HEAD

        // --- BINDINGS (Ligações) ---
        // O painel de edição só fica visível se um modelo estiver selecionado ou se um
        // novo for criado
        editorPane.visibleProperty().bind(selectedTemplate.isNotNull());
        // O botão de apagar só fica ativo se um modelo estiver selecionado na lista
        deleteButton.disableProperty().bind(templatesListView.getSelectionModel().selectedItemProperty().isNull());

        // Liga a propriedade selectedTemplate ao item selecionado na ListView
        selectedTemplate.bind(templatesListView.getSelectionModel().selectedItemProperty());

        // Quando um item é selecionado na lista, atualiza os campos do editor
        selectedTemplate.addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // Preenche o painel de edição com os dados do modelo selecionado
                nameField.setText(newSelection.getName());
                spintaxContentArea.setText(newSelection.getSpintaxContent());
                delayField.setText(String.valueOf(newSelection.getDelayInSeconds()));
            } else {
                // Limpa o painel se nada estiver selecionado
                nameField.clear();
                spintaxContentArea.clear();
                delayField.clear();
            }
        });

        // Carrega os dados iniciais do Firebase
        loadTemplatesAsync();
    }

    /**
     * Carrega os modelos de campanha do Firebase e atualiza a lista na interface.
     */
    @SuppressWarnings("unchecked")
    private void loadTemplatesAsync() {
        FirebaseService.getCampaignTemplates(userId, userToken).thenAcceptAsync(templatesData -> {
            Platform.runLater(() -> {
                templates.clear();
                if (templatesData != null) {
                    templatesData.forEach((name, data) -> {
                        if (data instanceof Map) {
                            Map<String, Object> settings = (Map<String, Object>) ((Map<?, ?>) data).get("settings");
                            if (settings != null) {
                                TemplateModel t = new TemplateModel();
                                t.setName(name);
                                t.setSpintaxContent((String) settings.getOrDefault("spintax_template", ""));
                                // Converte o delay para inteiro, com um valor padrão de 5
                                int delay = Integer.parseInt(settings.getOrDefault("delay", "5").toString());
                                t.setDelayInSeconds(delay);
                                templates.add(t);
                            }
                        }
                    });
                }
            });
        });
    }

    /**
     * Ação do botão "Novo Modelo". Limpa a seleção e o painel de edição para um
     * novo registo.
     */
    @FXML
    private void handleNewTemplate() {
        // Cria um modelo "fantasma" para ativar o painel de edição
        TemplateModel newTemplate = new TemplateModel();
        newTemplate.setName("NovoModelo"); // Nome temporário
        selectedTemplate.set(newTemplate); // Ativa o painel

        // Limpa os campos para o utilizador preencher
        nameField.setText("");
        spintaxContentArea.setText("Olá, {tudo bem|como vai}? Visite nosso site!");
        delayField.setText("5");
        nameField.requestFocus(); // Foca no campo de nome
        templatesListView.getSelectionModel().clearSelection(); // Desseleciona item da lista
    }

    /**
     * Ação do botão "Salvar Alterações". Salva o modelo (novo ou editado) no
     * Firebase.
     */
    @FXML
    private void handleSaveTemplate() {
        String name = nameField.getText();
        String content = spintaxContentArea.getText();
        String delay = delayField.getText();

        if (name == null || name.isBlank()) {
            JavaFxUtils.showAlert(Alert.AlertType.ERROR, "Erro", "O nome do modelo não pode estar vazio.");
            return;
        }

        // Converte o delay para um formato de string "Xs" para compatibilidade com a
        // tela de campanha
        String delayString = delay.replaceAll("[^0-9]", "") + "s";

        FirebaseService.saveTemplate(userId, userToken, name, content, delayString).thenAccept(success -> {
            if (success) {
                Platform.runLater(() -> {
                    JavaFxUtils.showAlert(Alert.AlertType.INFORMATION, "Sucesso",
                            "Modelo '" + name + "' salvo com sucesso.");
                    loadTemplatesAsync(); // Recarrega a lista
                    editorPane.visibleProperty().unbind(); // Desliga o bind temporariamente
                    editorPane.setVisible(false); // Esconde o painel
                    selectedTemplate.set(null); // Limpa a seleção
                });
            } else {
                Platform.runLater(() -> JavaFxUtils.showAlert(Alert.AlertType.ERROR, "Erro",
                        "Não foi possível salvar o modelo."));
            }
        });
    }

    /**
     * Ação do botão "Excluir". Remove o modelo selecionado do Firebase.
     */
    @FXML
    private void handleDeleteTemplate() {
        TemplateModel toDelete = selectedTemplate.get();
        if (toDelete == null) {
            JavaFxUtils.showAlert(Alert.AlertType.WARNING, "Aviso", "Nenhum modelo selecionado para apagar.");
            return;
        }

        if (JavaFxUtils.showConfirmation(Alert.AlertType.CONFIRMATION, "Confirmar Exclusão",
                "Tem certeza que deseja apagar o modelo '" + toDelete.getName() + "'?")) {
            FirebaseService.deleteTemplate(userId, userToken, toDelete.getName()).thenAccept(success -> {
                if (success) {
                    Platform.runLater(() -> {
                        JavaFxUtils.showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Modelo apagado.");
                        loadTemplatesAsync();
                    });
                }
            });
        }
=======
        
        // BINDINGS
        editorPane.visibleProperty().bind(editingTemplate.isNotNull());
        nameField.disableProperty().bind(isNewTemplate.not());
        
        // Ligação do formulário ao objeto 'editingTemplate'
        editingTemplate.addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                nameField.setText(newVal.getName());
                spintaxContentArea.setText(newVal.getSpintaxContent());
                delayField.setText(String.valueOf(newVal.getDelayInSeconds()));
            }
        });
        
        // Listener para a seleção na lista
        selectedTemplate.bind(templatesListView.getSelectionModel().selectedItemProperty());
        selectedTemplate.addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                isNewTemplate.set(false);
                // Cria uma cópia para edição para não alterar o original
                TemplateModel copy = new TemplateModel();
                copy.setName(newSelection.getName());
                copy.setSpintaxContent(newSelection.getSpintaxContent());
                copy.setDelayInSeconds(newSelection.getDelayInSeconds());
                editingTemplate.set(copy);
            }
        });
        
        loadTemplatesAsync();
    }
    
    private void loadTemplatesAsync() {
        // Simulação do carregamento
        templates.clear();
        templates.add(createTemplate("Venda Rápida", "Olá, {tudo bem|como vai}?", 10));
        templates.add(createTemplate("Follow-up Cliente", "Oi, passando para saber...", 15));
    }

    @FXML
    private void handleNewTemplate() {
        templatesListView.getSelectionModel().clearSelection();
        isNewTemplate.set(true);
        editingTemplate.set(new TemplateModel());
        nameField.requestFocus();
    }
    
    @FXML
    private void handleSaveTemplate() {
        // Lógica para guardar o 'editingTemplate' no Firebase
        System.out.println("A guardar modelo: " + nameField.getText());
        loadTemplatesAsync();
        editingTemplate.set(null); // Fecha o editor
    }

    @FXML
    private void handleDeleteTemplate() {
        // Lógica de exclusão
        loadTemplatesAsync();
        editingTemplate.set(null);
    }
    
    private TemplateModel createTemplate(String name, String content, int delay) {
        TemplateModel t = new TemplateModel();
        t.setName(name);
        t.setSpintaxContent(content);
        t.setDelayInSeconds(delay);
        return t;
>>>>>>> edf476c85c54429cd2c4a02aa6712b1e42808e3f
    }
}