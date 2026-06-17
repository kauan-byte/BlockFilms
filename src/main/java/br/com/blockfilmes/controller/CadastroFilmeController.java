package br.com.blockfilmes.controller;

import br.com.blockfilmes.service.Back4AppService;
import br.com.blockfilmes.util.Navegacao;
import com.google.gson.JsonObject;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class CadastroFilmeController implements Initializable {

    @FXML
    private TextField txtTitulo;
    @FXML
    private ComboBox<String> comboCategoria;
    @FXML
    private ComboBox<String> comboClassificacao;
    @FXML
    private TextArea txtSinopse;
    @FXML
    private Button btnSelecionarCapa;
    @FXML
    private Label lblNomeCapa;
    @FXML
    private Button btnSalvar;
    @FXML
    private Button btnSair;
    @FXML
    private Label lblMensagem;

    private Back4AppService back4AppService;
    private File capaSelecionada;
    private String sessionToken;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.back4AppService = new Back4AppService();

        // Popula as Categorias passadas por você
        comboCategoria.setItems(FXCollections.observableArrayList(
                "Ação", "Comédia", "Terror", "Ficção Científica", "Drama", "Mistério"
        ));

        // Popula as Classificações Indicativas do Brasil
        comboClassificacao.setItems(FXCollections.observableArrayList(
                "Livre", "10 anos", "12 anos", "14 anos", "16 anos", "18 anos"
        ));
    }

    public void setSessionToken(String token) {
        this.sessionToken = token;
        System.out.println("Token do usuário recebido na tela de filmes: " + token);
    }

    @FXML
    private void handleSelecionarCapa(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar Capa do Filme");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg")
        );

        Stage stage = (Stage) btnSelecionarCapa.getScene().getWindow();
        File arquivo = fileChooser.showOpenDialog(stage);

        if (arquivo != null) {
            this.capaSelecionada = arquivo;
            lblNomeCapa.setText(arquivo.getName());
        }
    }

    @FXML
    private void handleSalvarFilme(ActionEvent event) {
        String titulo = txtTitulo.getText().trim();
        String categoria = comboCategoria.getValue();
        String classificacao = comboClassificacao.getValue();
        String sinopse = txtSinopse.getText().trim();

        if (titulo.isEmpty() || categoria == null || classificacao == null || sinopse.isEmpty()) {
            lblMensagem.setStyle("-fx-text-fill: #E50914;");
            lblMensagem.setText("Por favor, preencha todos os campos do filme.");
            return;
        }

        lblMensagem.setStyle("-fx-text-fill: #FFFFFF;");
        lblMensagem.setText("Salvando filme...");

        new Thread(() -> {
            try {
                String urlCapa = "";

                // 1. Faz upload da capa do filme se houver, injetando o sessionToken de segurança
                if (capaSelecionada != null) {
                    javafx.application.Platform.runLater(() -> lblMensagem.setText("Enviando imagem da capa..."));
                    JsonObject uploadResult = back4AppService.uploadImagem(capaSelecionada, sessionToken);
                    if (uploadResult != null && uploadResult.has("url")) {
                        urlCapa = uploadResult.get("url").getAsString();
                    }
                }

                // 2. Envia os dados estruturados para a tabela "Filme" do Back4App
                javafx.application.Platform.runLater(() -> lblMensagem.setText("Registrando no banco de dados..."));
                back4AppService.cadastrarFilme(titulo, categoria, classificacao, sinopse, urlCapa);

                javafx.application.Platform.runLater(() -> {
                    lblMensagem.setStyle("-fx-text-fill: #00FF00;");
                    lblMensagem.setText("Filme cadastrado com sucesso!");

                    // Limpa os campos para o próximo cadastro
                    txtTitulo.clear();
                    txtSinopse.clear();
                    comboCategoria.setValue(null);
                    comboClassificacao.setValue(null);
                    capaSelecionada = null;
                    lblNomeCapa.setText("Nenhuma imagem selecionada");
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    lblMensagem.setStyle("-fx-text-fill: #E50914;");
                    lblMensagem.setText("Erro ao salvar filme: " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void handleSair(ActionEvent event) {
        // Em vez de ir para o login, volta para a lista de filmes salvos
        Stage stage = (Stage) btnSair.getScene().getWindow();
        Navegacao.mudarTela(stage, "CatalogoFilmes.fxml", "BlockFilmes - Catálogo de Filmes");
    }
}
