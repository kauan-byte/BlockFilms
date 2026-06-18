package br.com.blockfilmes.controller;

import br.com.blockfilmes.service.Back4AppService;
import br.com.blockfilmes.util.Navegacao;
import com.google.gson.JsonObject;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class CadastroFilmeController implements Initializable {

    @FXML private TextField txtTitulo;
    @FXML private TextField txtAno;
    @FXML private ComboBox<String> comboCategoria;
    @FXML private ComboBox<String> comboClassificacao;
    @FXML private TextArea txtSinopse;
    @FXML private ImageView imgPreviewCapa;
    @FXML private Button btnSelecionarCapa;
    @FXML private Label lblNomeCapa;
    @FXML private Label lblMensagem;
    @FXML private Button btnSalvar;

    private Back4AppService back4AppService;
    private File arquivoCapa;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.back4AppService = new Back4AppService();
        
        // Popula os ComboBoxes
        comboCategoria.setItems(FXCollections.observableArrayList("Ação", "Comédia", "Drama", "Ficção Científica", "Terror", "Animação"));
        comboClassificacao.setItems(FXCollections.observableArrayList("L", "10", "12", "14", "16", "18"));
    }

    @FXML
    private void handleSelecionarCapa(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar Capa do Filme");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg"));
        
        File file = fileChooser.showOpenDialog(btnSelecionarCapa.getScene().getWindow());
        if (file != null) {
            this.arquivoCapa = file;
            lblNomeCapa.setText(file.getName());
            imgPreviewCapa.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    private void handleSalvarFilme(ActionEvent event) {
        String titulo = txtTitulo.getText().trim();
        String categoria = comboCategoria.getValue();
        String classificacao = comboClassificacao.getValue();
        String sinopse = txtSinopse.getText().trim();
        String ano = txtAno.getText().trim();

        if (titulo.isEmpty() || categoria == null || classificacao == null || ano.isEmpty() || arquivoCapa == null) {
            lblMensagem.setText("Erro: Preencha todos os campos e selecione uma capa.");
            return;
        }

        lblMensagem.setStyle("-fx-text-fill: white;");
        lblMensagem.setText("Enviando filme...");

        new Thread(() -> {
            try {
                // 1. RECUPERAÇÃO DO TOKEN DA SUA CLASSE REAL: 
                String sessionToken = br.com.blockfilmes.util.SessaoUsuario.getSessionToken();
                
                if (sessionToken == null || sessionToken.isEmpty()) {
                    Platform.runLater(() -> lblMensagem.setText("Erro: Sessão expirada. Faça login novamente."));
                    return;
                }
                
                // 2. UPLOAD AUTENTICADO:
                // Passa o arquivo e o token ativo para o Back4App saber quem é você!
                String urlCapa = back4AppService.uploadImagemPerfil(arquivoCapa, sessionToken);

                // 3. CADASTRO DO FILME
                back4AppService.cadastrarFilme(titulo, categoria, classificacao, sinopse, urlCapa, ano);

                Platform.runLater(() -> {
                    lblMensagem.setStyle("-fx-text-fill: #00FF00;");
                    lblMensagem.setText("Filme cadastrado com sucesso!");
                    limparCampos();
                });
            } catch (Exception e) {
                Platform.runLater(() -> lblMensagem.setText("Erro ao salvar: " + e.getMessage()));
                e.printStackTrace();
            }
        }).start();
    }

    private void limparCampos() {
        txtTitulo.clear();
        txtAno.clear();
        txtSinopse.clear();
        comboCategoria.setValue(null);
        comboClassificacao.setValue(null);
        lblNomeCapa.setText("Nenhuma imagem selecionada");
        imgPreviewCapa.setImage(new Image("https://via.placeholder.com/110x150?text=Sem+Capa"));
    }

    @FXML
    private void handleSair(ActionEvent event) {
        Navegacao.mudarTela((Stage) txtTitulo.getScene().getWindow(), "CatalogoFilmes.fxml", "Catálogo");
    }
}