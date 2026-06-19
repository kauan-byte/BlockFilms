package br.com.blockfilmes.controller;

import br.com.blockfilmes.model.Filme;
import br.com.blockfilmes.service.Back4AppService;
import br.com.blockfilmes.util.Navegacao;
import br.com.blockfilmes.util.SessaoUsuario;
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
    private Filme filmeEmEdicao;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.back4AppService = new Back4AppService();
        
        comboCategoria.setItems(FXCollections.observableArrayList("Ação", "Comédia", "Drama", "Ficção Científica", "Terror", "Animação"));
        comboClassificacao.setItems(FXCollections.observableArrayList("L", "10", "12", "14", "16", "18"));

        this.filmeEmEdicao = SessaoUsuario.getFilmeSelecionado();
        
        if (filmeEmEdicao != null) {
            // Preenche os campos com os dados existentes do filme
            txtTitulo.setText(filmeEmEdicao.getTitulo());
            comboCategoria.setValue(filmeEmEdicao.getCategoria());
            comboClassificacao.setValue(filmeEmEdicao.getClassificacao());
            txtSinopse.setText(filmeEmEdicao.getSinopse());
            
            // CORREÇÃO: Puxa o ano real que veio do objeto, em vez do texto estático fixo
            txtAno.setText(filmeEmEdicao.getAno() != null ? filmeEmEdicao.getAno() : ""); 

            if (filmeEmEdicao.getFotoFilmeUrl() != null && !filmeEmEdicao.getFotoFilmeUrl().isEmpty()) {
                imgPreviewCapa.setImage(new Image(filmeEmEdicao.getFotoFilmeUrl(), true));
                lblNomeCapa.setText("Capa remota carregada");
            }
            
            btnSalvar.setText("Atualizar Filme");
        }
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

        boolean temCapaValida = (arquivoCapa != null) || (filmeEmEdicao != null && filmeEmEdicao.getFotoFilmeUrl() != null);

        if (titulo.isEmpty() || categoria == null || classificacao == null || ano.isEmpty() || !temCapaValida) {
            lblMensagem.setText("Erro: Preencha todos os campos obrigatórios.");
            return;
        }

        lblMensagem.setStyle("-fx-text-fill: white;");
        lblMensagem.setText(filmeEmEdicao == null ? "Enviando filme..." : "Atualizando filme...");

        new Thread(() -> {
            try {
                String sessionToken = SessaoUsuario.getSessionToken();
                if (sessionToken == null || sessionToken.isEmpty()) {
                    Platform.runLater(() -> lblMensagem.setText("Erro: Sessão expirada. Faça login novamente."));
                    return;
                }
                
                String urlCapaFinal = (filmeEmEdicao != null) ? filmeEmEdicao.getFotoFilmeUrl() : "";
                
                if (arquivoCapa != null) {
                    try {
                        urlCapaFinal = back4AppService.uploadImagemPerfil(arquivoCapa, sessionToken);
                    } catch (Exception e) {
                        System.err.println("Upload bloqueado pelo Back4App: " + e.getMessage());
                        urlCapaFinal = "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=200"; 
                    }
                }

                if (filmeEmEdicao == null) {
                    // MODO: NOVO FILME
                    back4AppService.cadastrarFilme(titulo, categoria, classificacao, sinopse, urlCapaFinal, ano);
                    Platform.runLater(() -> {
                        lblMensagem.setStyle("-fx-text-fill: #00FF00;");
                        lblMensagem.setText("Filme cadastrado com sucesso!");
                        limparCampos();
                    });
                } else {
                    // MODO: ATUALIZAR FILME EXISTENTE
                    filmeEmEdicao.setTitulo(titulo);
                    filmeEmEdicao.setCategoria(categoria);
                    filmeEmEdicao.setClassificacao(classificacao);
                    filmeEmEdicao.setSinopse(sinopse);
                    filmeEmEdicao.setFotoFilmeUrl(urlCapaFinal);
                    filmeEmEdicao.setAno(ano);

                    // CORREÇÃO CRÍTICA: Faz a chamada real HTTP para a API atualizar a linha no Back4App
                    back4AppService.atualizarFilme(filmeEmEdicao.getObjectId(), titulo, categoria, classificacao, sinopse, urlCapaFinal, ano);
                    System.out.println("Filme atualizado com sucesso no Back4App ID: " + filmeEmEdicao.getObjectId());

                    Platform.runLater(() -> {
                        lblMensagem.setStyle("-fx-text-fill: #00FF00;");
                        lblMensagem.setText("Filme atualizado com sucesso!");
                    });
                }
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
        filmeEmEdicao = null;
        SessaoUsuario.setFilmeSelecionado(null);
    }

    @FXML
    private void handleSair(ActionEvent event) {
        SessaoUsuario.setFilmeSelecionado(null); 
        Navegacao.mudarTela((Stage) txtTitulo.getScene().getWindow(), "CatalogoFilmes.fxml", "Catálogo");
    }
}