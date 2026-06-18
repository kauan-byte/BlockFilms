package br.com.blockfilmes.controller;

import br.com.blockfilmes.service.Back4AppService;
import br.com.blockfilmes.util.Navegacao;
import com.google.gson.JsonObject;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class CadastroUsuarioController implements Initializable {

    @FXML
    private TextField txtNomeCompleto;
    @FXML
    private TextField txtUsuario;
    @FXML
    private TextField txtEmail;
    @FXML
    private PasswordField txtSenha;
    @FXML
    private Button btnSelecionarFoto;
    @FXML
    private Label lblNomeFoto;
    @FXML
    private Button btnCadastrar;
    @FXML
    private Button btnVoltar;
    @FXML
    private Label lblMensagem;
    @FXML
    private ImageView imgPreviewCadastro;

    private Back4AppService back4AppService;
    private File fotoSelecionada;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.back4AppService = new Back4AppService();
    }

    @FXML
    private void handleSelecionarFoto(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar Foto de Perfil");

        // Filtro para aceitar apenas imagens
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg")
        );

        // Pega a janela atual para exibir o seletor
        Stage stage = (Stage) btnSelecionarFoto.getScene().getWindow();
        File arquivo = fileChooser.showOpenDialog(stage);

        if (arquivo != null) {
            this.fotoSelecionada = arquivo;
            lblNomeFoto.setText(arquivo.getName());

            // MÁGICA AQUI: Carrega instantaneamente a foto na tela para o usuário ver
            javafx.scene.image.Image imagemLocal = new javafx.scene.image.Image(arquivo.toURI().toString());
            imgPreviewCadastro.setImage(imagemLocal);
        }
    }

    @FXML
    private void handleCadastrar(ActionEvent event) {
        String nome = txtNomeCompleto.getText().trim();
        String usuario = txtUsuario.getText().trim();
        String email = txtEmail.getText().trim();
        String senha = txtSenha.getText().trim();

        if (nome.isEmpty() || usuario.isEmpty() || email.isEmpty() || senha.isEmpty()) {
            lblMensagem.setStyle("-fx-text-fill: #E50914;");
            lblMensagem.setText("Por favor, preencha todos os campos obrigatórios.");
            return;
        }

        lblMensagem.setStyle("-fx-text-fill: #FFFFFF;");
        lblMensagem.setText("Criando conta no BlockFilmes...");

        new Thread(() -> {
            try {
                // CORREÇÃO DA ORDEM: 1º usuario, 2º email, 3º senha, 4º telefone (vazio ""), 5º foto (vazio "")
                JsonObject novoUsuario = back4AppService.cadastrarUsuario(usuario, email, senha, "", "");

                // 2. Faz o login automático para ganhar a permissão de usuário autenticado
                javafx.application.Platform.runLater(() -> lblMensagem.setText("Autenticando sessão..."));
                JsonObject loginResult = back4AppService.login(usuario, senha);
                String sessionToken = loginResult.has("sessionToken") ? loginResult.get("sessionToken").getAsString() : "";
                String objectId = loginResult.has("objectId") ? loginResult.get("objectId").getAsString() : "";

                // 3. Se escolheu foto, faz o upload usando o método exclusivo de perfil (que já passa o token correto)
                if (fotoSelecionada != null && !sessionToken.isEmpty() && !objectId.isEmpty()) {
                    javafx.application.Platform.runLater(() -> lblMensagem.setText("Enviando foto de perfil..."));

                    // Usamos o método corrigido ontem que retorna a String da URL direto
                    String urlFoto = back4AppService.uploadImagemPerfil(fotoSelecionada, sessionToken);

                    if (urlFoto != null && !urlFoto.isEmpty()) {
                        // Monta o JSON para vincular a foto criada ao usuário recém-cadastrado
                        JsonObject dadosAtualizacao = new JsonObject();
                        dadosAtualizacao.addProperty("fotoPerfilUrl", urlFoto);

                        // Executa a atualização
                        back4AppService.atualizarUsuario(objectId, sessionToken, dadosAtualizacao);
                    }
                }

                javafx.application.Platform.runLater(() -> {
                    lblMensagem.setStyle("-fx-text-fill: #00FF00;");
                    lblMensagem.setText("Usuário cadastrado com sucesso!");
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    lblMensagem.setStyle("-fx-text-fill: #E50914;");
                    lblMensagem.setText("Erro ao cadastrar: " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void handleVoltar(ActionEvent event) {
        Stage stage = (Stage) btnVoltar.getScene().getWindow();
        Navegacao.mudarTela(stage, "login.fxml", "BlockFilmes - Login");
    }
}
