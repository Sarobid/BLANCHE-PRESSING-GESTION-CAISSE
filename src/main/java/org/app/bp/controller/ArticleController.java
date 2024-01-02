package org.app.bp.controller;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import org.app.bp.models.Articles;
import org.app.bp.services.MarchandisesServices;
import org.app.bp.utils.Utils;
import org.controlsfx.control.MaskerPane;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class ArticleController implements Initializable {

    @FXML
    private TextField txt_type_article;
    @FXML
    private TextField txt_prix_article;
    @FXML
    private TextField txt_code_article;
    @FXML
    private TableView<Articles> table_article;
    @FXML
    private TableColumn<Articles, Integer> COL_NUMERO_ARTICLE;
    @FXML
    private TableColumn<Articles, TextField> COL_TYPE_ARTICLE;
    @FXML
    private TableColumn<Articles, TextField> COL_PRIX_ARTICLE;
    @FXML
    private TableColumn<Articles, TextField> COL_CODE_ARTICLE;

    @FXML
    private TableColumn<Articles, Button> COL_MODIFIER;

    @FXML
    private TableColumn<Articles, Button> COL_SUPPRIMER;
    @FXML
    private Button btn_enregistrer_article;
    @FXML
    private MaskerPane table_article_loading;
    @FXML
    private Label lbl_type_article;
    @FXML
    private Label lbl_prix_article;
    @FXML
    private Label lbl_code_article;

    Utils appUtils = new Utils();

    MarchandisesServices marchandisesServices = new MarchandisesServices();

    /**
     * MENU ARTICLE
     * Ajout article
     * */
    public boolean handleValidateForm(){
        if (txt_type_article.getText().isEmpty() || txt_prix_article.getText().isEmpty() || txt_code_article.getText().isEmpty() ){
            appUtils.warningAlertDialog("AVERTISSEMENT","VEUILLEZ COMPLETEZ TOUS LES CHAMPS");
            return false;
        }
        return true;
    }

    public void clearForm(){
        txt_type_article.clear();
        txt_prix_article.clear();
        txt_code_article.clear();
    }
    private Articles articleSelected;

    @FXML
    private void handleSelectedClient(){
        if(articleSelected != null){
            articleSelected.deselectionner();
        }
        articleSelected  = table_article.getSelectionModel().getSelectedItem();
        if(articleSelected != null){
            articleSelected.selectionneer(this);
        }
    }

    @FXML
    private void handleSaveArticleButton(ActionEvent actionEvent){
        if (handleValidateForm()){
            Articles nouvelle_article = new Articles(
                    txt_type_article.getText().trim(),
                    Integer.parseInt(txt_prix_article.getText().trim()),
                    txt_code_article.getText().trim()
            );

            Service<Boolean> addNewArticleService = marchandisesServices.addNewArticle(nouvelle_article);
            addNewArticleService.setOnSucceeded(onSucceededEvent -> {
                if (addNewArticleService.getValue()){
                    Service<Integer> getLastIdArticle = marchandisesServices.getLastIdFromArticleTable();
                    getLastIdArticle.setOnSucceeded((t) -> {
                        nouvelle_article.setId_article(getLastIdArticle.getValue());
                        table_article.getItems().add(nouvelle_article);
                        appUtils.successAlertDialog("SUCCESS",nouvelle_article.getNom_article() + " Ajouter ");
                        clearForm();
                    });
                    getLastIdArticle.start();
                } else {
                    appUtils.erreurAlertDialog("ERREUR","Erreur lors de l'ajout de nouveau club");
                    clearForm();
                }
            });
            addNewArticleService.start();
        }
    }
    public void afficheERREUR(String erreur){
           appUtils.erreurAlertDialog("ERREUR",erreur);
            
    }

    @FXML
    private void handleDeleteMenu(){
        Articles article_effacer = table_article.getSelectionModel().getSelectedItem();
        Service<Boolean> deleteArticle = marchandisesServices.deleteArticle(article_effacer);
        deleteArticle.setOnSucceeded((onSucceededEvent) -> {
            if(deleteArticle.getValue()){
                table_article.getItems().remove(article_effacer);
                appUtils.successAlertDialog("SUCCESS","Club " + article_effacer.getNom_article() + " Effacer");
            } else {
                appUtils.erreurAlertDialog("ERREUR","Erreur lors de la suppression du club " + article_effacer.getNom_article());
            }
        });
        deleteArticle.setOnFailed(setOnFailedEvent -> {
            deleteArticle.getException().getMessage();
        });
        deleteArticle.start();
    }

    /**
     * Bouton Retour vers scene dashboard
     * */
    @FXML
    private void backToDashboard(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home/dashboard.fxml"));
        Parent premiereSceneParent = loader.load();
        Scene premiereScene = new Scene(premiereSceneParent);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(premiereScene);
        stage.show();
    }
    @FXML
    private void backACCEUIL(ActionEvent actionEvent) throws IOException {
        Node node_source = (Node) actionEvent.getSource();
        Stage stage = (Stage) node_source.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home/dashboard.fxml"));
        Parent premiereSceneParent = loader.load();
        Scene premiereScene = new Scene(premiereSceneParent);
        stage.setScene(premiereScene);
        stage.show();       
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        lbl_type_article.setVisible(false);
        lbl_prix_article.setVisible(false);
        lbl_code_article.setVisible(false);

        MarchandisesServices marchandisesServices = new MarchandisesServices();

        COL_NUMERO_ARTICLE.setCellValueFactory(new PropertyValueFactory<>("id_article"));
        COL_TYPE_ARTICLE.setCellValueFactory(new PropertyValueFactory<>("txt_nom"));
        COL_PRIX_ARTICLE.setCellValueFactory(new PropertyValueFactory<>("txt_prix"));
        COL_CODE_ARTICLE.setCellValueFactory(new PropertyValueFactory<>("txt_prefix_code"));
        COL_MODIFIER.setCellValueFactory(new PropertyValueFactory<>("modifier"));
        COL_SUPPRIMER.setCellValueFactory(new PropertyValueFactory<>("delete"));

        ContextMenu contextMenu = new ContextMenu();
        MenuItem edit_menu = new MenuItem("EDITER");
        MenuItem delete_menu = new MenuItem("EFFACER");
        contextMenu.getItems().addAll(edit_menu, delete_menu);
        table_article.setContextMenu(contextMenu);
        
        delete_menu.setOnAction((event) -> {
            handleDeleteMenu();
        });

        Platform.runLater(() -> {
            table_article_loading.setDisable(false);
            table_article_loading.toFront();
            Service<List<Articles>> getArticleDataService = marchandisesServices.getAllArticleData();
            getArticleDataService.setOnSucceeded(onSucceededEvent -> {
                table_article.getItems().setAll(getArticleDataService.getValue());
                table_article_loading.setDisable(true);
                table_article_loading.toBack();
            });
            getArticleDataService.start();
        });
    }

    public void reinitialisation(){
                Platform.runLater(() -> {
            table_article_loading.setDisable(false);
            table_article_loading.toFront();
            Service<List<Articles>> getArticleDataService = marchandisesServices.getAllArticleData();
            getArticleDataService.setOnSucceeded(onSucceededEvent -> {
                table_article.getItems().setAll(getArticleDataService.getValue());
                table_article_loading.setDisable(true);
                table_article_loading.toBack();
            });
            getArticleDataService.start();
        });

    }
}
