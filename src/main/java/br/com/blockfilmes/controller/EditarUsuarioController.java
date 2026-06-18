package br.com.blockfilmes.controller;

import br.com.blockfilmes.service.Back4AppService;
import br.com.blockfilmes.util.Navegacao;
import br.com.blockfilmes.util.SessaoUsuario;
import com.google.gson.JsonObject;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class EditarUsuarioController implements Initializable {

    @FXML private ImageView imgPerfil;
    @FXML private Button btnAlterarFoto;
    @FXML private TextField txtNome;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtSenha; // Novo campo injetado do FXML
    @FXML private Button btnSalvar;
    @FXML private Button btnVoltar;
    @FXML private Label lblMensagem;

    private Back4AppService back4AppService;
    private File arquivoFotoSelecionada;
    private String urlFotoAtual = "";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.back4AppService = new Back4AppService();
        imgPerfil.setImage(new Image("https://www.w3schools.com/howto/img_avatar.png", true));
        carregarDadosPerfil();
    }    

    private void carregarDadosPerfil() {
        String token = SessaoUsuario.getSessionToken();
        if (token == null || token.isEmpty()) return;

        new Thread(() -> {
            try {
                JsonObject user = back4AppService.buscarUsuarioAtual(token);
                
                String nome = user.has("username") ? user.get("username").getAsString() : "";
                String email = user.has("email") ? user.get("email").getAsString() : "";
                urlFotoAtual = user.has("fotoPerfilUrl") ? user.get("fotoPerfilUrl").getAsString() : "";

                Platform.runLater(() -> {
                    txtNome.setText(nome);
                    txtEmail.setText(email);
                    if (!urlFotoAtual.isEmpty()) {
                        imgPerfil.setImage(new Image(urlFotoAtual, true));
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> lblMensagem.setText("Erro ao carregar perfil."));
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleAlterarFoto(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar Foto de Perfil");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg")
        );
        
        Stage stage = (Stage) btnAlterarFoto.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        
        if (file != null) {
            this.arquivoFotoSelecionada = file;
            imgPerfil.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    private void handleSalvarPerfil(ActionEvent event) {
        String novoNome = txtNome.getText().trim();
        String novoEmail = txtEmail.getText().trim();
        String novaSenha = txtSenha.getText().trim();
        String token = SessaoUsuario.getSessionToken();
        String id = SessaoUsuario.getObjectId();

        if (novoNome.isEmpty() || novoEmail.isEmpty()) {
            lblMensagem.setStyle("-fx-text-fill: #E50914;");
            lblMensagem.setText("Nome e E-mail não podem ficar vazios.");
            return;
        }

        lblMensagem.setStyle("-fx-text-fill: #FFFFFF;");
        lblMensagem.setText("Salvando alterações...");

        new Thread(() -> {
            try {
                String urlNovaFoto = urlFotoAtual;

                if (arquivoFotoSelecionada != null) {
                    urlNovaFoto = back4AppService.uploadImagemPerfil(arquivoFotoSelecionada, token);
                }

                JsonObject dados = new JsonObject();
                dados.addProperty("username", novoNome);
                dados.addProperty("email", novoEmail);
                dados.addProperty("fotoPerfilUrl", urlNovaFoto);
                
                // Se o usuário digitou algo no campo senha, atualiza ela também
                if (!novaSenha.isEmpty()) {
                    dados.addProperty("password", novaSenha);
                }

                back4AppService.atualizarUsuario(id, token, dados);

                Platform.runLater(() -> {
                    lblMensagem.setStyle("-fx-text-fill: #00FF00;");
                    lblMensagem.setText("Perfil atualizado com sucesso!");
                    txtSenha.clear(); // Limpa o campo de senha após salvar
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    lblMensagem.setStyle("-fx-text-fill: #E50914;");
                    lblMensagem.setText("Erro ao salvar alterações.");
                });
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleVoltar(ActionEvent event) {
        Stage stage = (Stage) btnVoltar.getScene().getWindow();
        Navegacao.mudarTela(stage, "CatalogoFilmes.fxml", "BlockFilmes - Catálogo de Filmes");
    }
}