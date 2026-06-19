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

    @FXML private Label lblNomeFilme;
    @FXML private ListView<JsonObject> listViewAvaliacoes;
    @FXML private Button btnVoltar;

    private Back4AppService back4AppService;
    private ObservableList<JsonObject> listaComentarios = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.back4AppService = new Back4AppService();

        // 1. Recupera o filme que foi selecionado na tela de Catálogo
        Filme filmeSelecionado = SessaoUsuario.getFilmeSelecionado();

        if (filmeSelecionado != null) {
            // Atualiza o texto do topo com o título do filme clicado
            lblNomeFilme.setText(filmeSelecionado.getTitulo());
            
            // Carrega os comentários fictícios
            carregarAvaliacoesSimuladas(filmeSelecionado.getObjectId());
        } else {
            lblNomeFilme.setText("Nenhum filme selecionado.");
        }

        // 2. Customiza a aparência das linhas da ListView
        configurarAparenciaDaLista();
    }

    private void carregarAvaliacoesSimuladas(String idFilme) {
        try {
            // Busca o JSON fake que criamos no Back4AppService
            JsonObject resultado = back4AppService.buscarAvaliacoesPorFilmeSimulado(idFilme);
            
            if (resultado != null && resultado.has("results")) {
                JsonArray array = resultado.getAsJsonArray("results");
                listaComentarios.clear();

                for (JsonElement elem : array) {
                    listaComentarios.add(elem.getAsJsonObject());
                }

                listViewAvaliacoes.setItems(listaComentarios);
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar avaliações simuladas: " + e.getMessage());
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
                    // Extrai os dados do JSON simulado
                    String usuario = avaliacao.has("nomeUsuario") ? avaliacao.get("nomeUsuario").getAsString() : "Anônimo";
                    int nota = avaliacao.has("nota") ? avaliacao.get("nota").getAsInt() : 0;
                    String textoComentario = avaliacao.has("comentario") ? avaliacao.get("comentario").getAsString() : "";

                    // Cria os Labels com estilizações básicas para o modo escuro
                    Label lblUser = new Label(usuario);
                    lblUser.setStyle("-fx-font-weight: bold; -fx-text-fill: #e50914; -fx-font-size: 14px;");

                    // Monta as estrelinhas ou número da nota
                    String estrelas = "⭐".repeat(Math.max(0, Math.min(nota, 5)));
                    Label lblNota = new Label("Nota: " + estrelas + " (" + nota + "/5)");
                    lblNota.setStyle("-fx-text-fill: #ffc107; -fx-font-size: 12px;");

                    Label lblComentario = new Label(textoComentario);
                    lblComentario.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
                    lblComentario.setWrapText(true); // Força o texto a quebrar linha se for muito longo

                    // Organiza tudo verticalmente em um container invisível
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
        // CORRIGIDO: Agora usando o nome correto do método em português
        SessaoUsuario.setFilmeSelecionado(null); 
        
        // Retorna para o Catálogo de Filmes
        Stage stage = (Stage) btnVoltar.getScene().getWindow();
        Navegacao.mudarTela(stage, "CatalogoFilmes.fxml", "BlockFilmes - Catálogo de Filmes");
    }
}