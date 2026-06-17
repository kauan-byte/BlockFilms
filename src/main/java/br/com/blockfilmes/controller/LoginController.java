package br.com.blockfilmes.controller;

import br.com.blockfilmes.service.Back4AppService;
import br.com.blockfilmes.util.Navegacao;
import br.com.blockfilmes.util.SessaoUsuario;
import com.google.gson.JsonObject;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController implements Initializable {

    @FXML
    private TextField txtUsuario;
    @FXML
    private PasswordField txtSenha;
    @FXML
    private Button btnEntrar;
    @FXML
    private Button btnCadastrarSe;
    @FXML
    private Label lblErro;

    private Back4AppService back4AppService;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Inicializa o serviço de conexão com o Back4App
        this.back4AppService = new Back4AppService();
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String usuario = txtUsuario.getText().trim();
        String senha = txtSenha.getText().trim();

        // Validação básica de campos vazios
        if (usuario.isEmpty() || senha.isEmpty()) {
            lblErro.setText("Por favor, preencha todos os campos.");
            return;
        }

        lblErro.setText("Autenticando...");

        // Executa a chamada de rede em uma Thread separada para não travar a interface
        new Thread(() -> {
            try {
                JsonObject userJson = back4AppService.login(usuario, senha);

                // Extrai o sessionToken se ele existir na resposta do Back4App
                String sessionToken = "";
                if (userJson != null && userJson.has("sessionToken")) {
                    sessionToken = userJson.get("sessionToken").getAsString();
                }

                final String tokenFinal = sessionToken;

                // Salva as credenciais na sessão global para a tela de Edição de Usuário usar
                SessaoUsuario.setSessionToken(tokenFinal);
                if (userJson != null && userJson.has("objectId")) {
                    SessaoUsuario.setObjectId(userJson.get("objectId").getAsString());
                }

                // Modificações visuais e carregamento de novas cenas precisam rodar na Thread principal
                javafx.application.Platform.runLater(() -> {
                    try {
                        lblErro.setText("");
                        System.out.println("Login efetuado com sucesso! Token: " + tokenFinal);

                        // 1. Carrega o arquivo do Catálogo primeiro (em vez da tela de cadastro direto)
                        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/CatalogoFilmes.fxml"));
                        javafx.scene.Parent root = loader.load();

                        // 2. Transiciona a janela reaproveitando o stage do botão (sem redeclarar a variável)
                        Stage stageActive = (Stage) btnEntrar.getScene().getWindow();
                        stageActive.setScene(new javafx.scene.Scene(root));
                        stageActive.setTitle("BlockFilmes - Catálogo de Filmes");
                        stageActive.centerOnScreen();
                        stageActive.show();

                    } catch (Exception e) {
                        lblErro.setText("Erro ao carregar a tela de catálogo.");
                        System.err.println("Erro ao mudar de cena: " + e.getMessage());
                        e.printStackTrace();
                    }
                });

            } catch (Exception e) {
                // Em caso de erro (senha errada, usuário inexistente, sem internet, etc)
                javafx.application.Platform.runLater(() -> {
                    lblErro.setText("Erro: Usuário ou senha incorretos.");
                    System.err.println("Erro no login: " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void handleIrParaCadastro(ActionEvent event) {
        // Pega o Stage (janela) atual através do próprio botão clicado
        Stage stage = (Stage) btnCadastrarSe.getScene().getWindow();
        // Troca para a tela de cadastro
        Navegacao.mudarTela(stage, "cadastro_usuario.fxml", "BlockFilmes - Cadastro");
    }
}