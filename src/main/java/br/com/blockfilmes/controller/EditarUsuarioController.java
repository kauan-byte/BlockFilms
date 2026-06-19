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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.ButtonType;

public class EditarUsuarioController implements Initializable {

    @FXML private ImageView imgPerfil;
    @FXML private Button btnAlterarFoto;
    @FXML private TextField txtNome;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtSenha;
    @FXML private Button btnSalvar;
    @FXML private Button btnVoltar;
    @FXML private Label lblMensagem;
    @FXML private Button btnExcluirConta;

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
                
                if (!novaSenha.isEmpty()) {
                    dados.addProperty("password", novaSenha);
                }

                back4AppService.atualizarUsuario(id, token, dados);

                Platform.runLater(() -> {
                    lblMensagem.setStyle("-fx-text-fill: #00FF00;");
                    lblMensagem.setText("Perfil updated com sucesso!");
                    txtSenha.clear();
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
    
    @FXML
    private void handleExcluirConta(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Excluir Conta");
        alert.setHeaderText("ESTA AÇÃO É IRREVERSÍVEL!");
        alert.setContentText("Você perderá o acesso ao BlockFilmes e todos os seus dados serão apagados. Deseja continuar?");

        alert.showAndWait().ifPresent(resposta -> {
            if (resposta == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        // USO RECORRENTE DOS SEUS MÉTODOS EXISTENTES NA SESSÃO
                        String objectId = SessaoUsuario.getObjectId();
                        String sessionToken = SessaoUsuario.getSessionToken();

                        boolean deletado = back4AppService.excluirUsuario(objectId, sessionToken);

                        if (deletado) {
                            System.out.println("Conta excluída com sucesso do banco de dados.");

                            // LIMPEZA DA SESSÃO ADAPTADA AO SEU DESIGN ATUAL
                            SessaoUsuario.setSessionToken(null);
                            SessaoUsuario.setObjectId(null);

                            Platform.runLater(() -> {
                                Stage stage = (Stage) btnExcluirConta.getScene().getWindow();
                                Navegacao.mudarTela(stage, "login.fxml", "BlockFilmes - Login");
                                
                                Alert info = new Alert(Alert.AlertType.INFORMATION);
                                info.setTitle("Conta Removida");
                                info.setHeaderText(null);
                                info.setContentText("Sua conta foi permanentemente removida do sistema.");
                                info.showAndWait();
                            });
                        } else {
                            mostrarErro("Não foi possível excluir a conta. Tente novamente mais tarde.");
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        mostrarErro("Erro ao conectar com o servidor: " + e.getMessage());
                    }
                }).start();
            }
        });
    }

    private void mostrarErro(String msg) {
        Platform.runLater(() -> {
            Alert alertErro = new Alert(Alert.AlertType.ERROR);
            alertErro.setTitle("Erro");
            alertErro.setContentText(msg);
            alertErro.showAndWait();
        });
    }
}