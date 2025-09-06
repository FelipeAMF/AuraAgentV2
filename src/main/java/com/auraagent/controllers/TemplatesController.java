package com.auraagent.controllers;

import com.auraagent.models.TemplateModel;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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

    @Override
    public void initialize(String userId) {
        this.userId = userId;

        templatesListView.setItems(templates);
        templatesListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(TemplateModel item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getName());
            }
        });
        
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
    }
}