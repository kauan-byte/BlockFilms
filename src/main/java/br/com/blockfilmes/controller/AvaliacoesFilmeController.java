package br.com.blockfilmes.controller;

import br.com.blockfilmes.model.Filme;
import br.com.blockfilmes.service.Back4AppService;
import br.com.blockfilmes.util.Navegacao;
import br.com.blockfilmes.util.SessaoUsuario;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AvaliacoesFilmeController implements Initializable {

    @FXML
    private Label lblNomeFilme;
    @FXML
    private ListView<JsonObject> listViewAvaliacoes;
    @FXML
    private Button btnVoltar;

    private Back4AppService back4AppService;
    private ObservableList<JsonObject> listaComentarios = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.back4AppService = new Back4AppService();

        Filme filmeSelecionado = SessaoUsuario.getFilmeSelecionado();

        if (filmeSelecionado != null) {
            lblNomeFilme.setText(filmeSelecionado.getTitulo());
            carregarAvaliacoesSimuladas(filmeSelecionado.getTitulo());
        } else {
            lblNomeFilme.setText("Nenhum filme selecionado.");
        }

        configurarAparenciaDaLista();
    }

    private void carregarAvaliacoesSimuladas(String nomeFilme) {
        try {
            JsonObject resultado = back4AppService.buscarAvaliacoesPorFilmeSimulado(nomeFilme);

            if (resultado != null && resultado.has("results")) {
                JsonArray array = resultado.getAsJsonArray("results");
                listaComentarios.clear();

                for (JsonElement elem : array) {
                    listaComentarios.add(elem.getAsJsonObject());
                }

                listViewAvaliacoes.setItems(listaComentarios);
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar avaliações: " + e.getMessage());
        }
    }

    private void configurarAparenciaDaLista() {
        listViewAvaliacoes.setCellFactory(param -> new ListCell<JsonObject>() {
            @Override
            protected void updateItem(JsonObject avaliacao, boolean empty) {
                super.updateItem(avaliacao, empty);

                if (empty || avaliacao == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String usuario = "Anônimo";
                    
                    // CORREÇÃO: Alterado de "usuario" para "usuario_" para bater com o banco de dados
                    if (avaliacao.has("usuario_") && !avaliacao.get("usuario_").isJsonNull()) {
                        JsonElement userElement = avaliacao.get("usuario_");
                        
                        if (userElement.isJsonObject()) {
                            JsonObject userObj = userElement.getAsJsonObject();
                            
                            if (userObj.has("username")) {
                                usuario = userObj.get("username").getAsString();
                            } else if (userObj.has("nome")) {
                                usuario = userObj.get("nome").getAsString();
                            } else if (userObj.has("objectId")) {
                                usuario = userObj.get("objectId").getAsString();
                            }
                        } else {
                            usuario = userElement.getAsString();
                        }
                    }

                    int nota = avaliacao.has("nota") ? avaliacao.get("nota").getAsInt() : 0;
                    String textoComentario = avaliacao.has("comentario") ? avaliacao.get("comentario").getAsString() : "";

                    Label lblUser = new Label("Usuário: " + usuario);
                    lblUser.setStyle("-fx-font-weight: bold; -fx-text-fill: #e50914; -fx-font-size: 14px;");

                    Label lblNota = new Label("Nota: " + nota + " / 10  ⭐");
                    lblNota.setStyle("-fx-text-fill: #ffc107; -fx-font-size: 13px; -fx-font-weight: bold;");

                    Label lblComentario = new Label(textoComentario);
                    lblComentario.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
                    lblComentario.setWrapText(true);

                    VBox containerFila = new VBox(5, lblUser, lblNota, lblComentario);
                    containerFila.setPadding(new javafx.geometry.Insets(8));
                    containerFila.setStyle("-fx-background-color: #2b2b2b; -fx-background-radius: 4px;");

                    setGraphic(containerFila);
                }
            }
        });
    }

    @FXML
    private void handleVoltar(ActionEvent event) {
        SessaoUsuario.setFilmeSelecionado(null);
        Stage stage = (Stage) btnVoltar.getScene().getWindow();
        Navegacao.mudarTela(stage, "CatalogoFilmes.fxml", "BlockFilmes - Catálogo de Filmes");
    }
}