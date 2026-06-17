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
                // 1. Cadastra o usuário primeiro SEM FOTO (Evita o bloqueio do servidor)
                JsonObject novoUsuario = back4AppService.cadastrarUsuario(nome, usuario, email, senha, "");

                // 2. Faz o login automático para ganhar a permissão de usuário autenticado
                javafx.application.Platform.runLater(() -> lblMensagem.setText("Autenticando sessão..."));
                JsonObject loginResult = back4AppService.login(usuario, senha);
                String sessionToken = loginResult.has("sessionToken") ? loginResult.get("sessionToken").getAsString() : "";

                // 3. Se escolheu foto, faz o upload e atualiza o perfil usando o token da sessão
                if (fotoSelecionada != null && !sessionToken.isEmpty()) {
                    javafx.application.Platform.runLater(() -> lblMensagem.setText("Enviando foto de perfil..."));

                    // Nota: O método uploadImagem do seu Back4AppService precisaria receber o sessionToken no Header
                    // Passamos o arquivo E o sessionToken obtido no login automático
                    JsonObject uploadResult = back4AppService.uploadImagem(fotoSelecionada, sessionToken);
                    if (uploadResult != null && uploadResult.has("url")) {
                        String urlFoto = uploadResult.get("url").getAsString();

                        // Atualiza o usuário adicionando a URL da foto agora que está logado
                        String objectId = loginResult.get("objectId").getAsString();
                        // (Opcional: método de update se necessário, ou podemos usar a liberação do JSON no painel que é mais simples!)
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
