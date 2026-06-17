package br.com.blockfilmes.controller;

import br.com.blockfilmes.model.Filme;
import br.com.blockfilmes.service.Back4AppService;
import br.com.blockfilmes.util.Navegacao;
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
import javafx.scene.control.Control;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class CatalogoFilmesController implements Initializable {

    @FXML private TableView<Filme> tabelaFilmes;
    @FXML private TableColumn<Filme, String> colCapa; // Alterado para mapear a String da URL
    @FXML private TableColumn<Filme, String> colTitulo;
    @FXML private TableColumn<Filme, String> colCategoria;
    @FXML private TableColumn<Filme, String> colClassificacao;
    @FXML private TableColumn<Filme, String> colSinopse;
    @FXML private Button btnPerfil;

    private Back4AppService back4AppService;
    private ObservableList<Filme> listaFilmes = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.back4AppService = new Back4AppService();

        // Configuração das colunas de texto
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colClassificacao.setCellValueFactory(new PropertyValueFactory<>("classificacao"));
        colSinopse.setCellValueFactory(new PropertyValueFactory<>("sinopse"));
        colCapa.setCellValueFactory(new PropertyValueFactory<>("fotoFilmeUrl"));

        // CORREÇÃO DA SINOPSE: Força o texto a quebrar linhas dentro da célula da tabela
        colSinopse.setCellFactory(tc -> {
            TableCell<Filme, String> cell = new TableCell<>();
            Text text = new Text();
            cell.setGraphic(text);
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            text.wrappingWidthProperty().bind(colSinopse.widthProperty().subtract(10));
            text.styleProperty().bind(cell.styleProperty());
            text.setFill(javafx.scene.paint.Color.WHITE); // Mantém o texto visível no escuro
            cell.itemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == null) {
                    text.setText("");
                } else {
                    text.setText(newVal);
                }
            });
            return cell;
        });

        // ADIÇÃO DA CAPA: Renderiza dinamicamente a foto da URL enviada ao Back4App
        colCapa.setCellFactory(tc -> new TableCell<Filme, String>() {
            private final ImageView imageView = new ImageView();
            @Override
            protected void updateItem(String urlImagem, boolean empty) {
                super.updateItem(urlImagem, empty);
                if (empty || urlImagem == null || urlImagem.trim().isEmpty()) {
                    setGraphic(null);
                } else {
                    try {
                        // Define dimensões fixas no padrão miniatura vertical
                        imageView.setFitWidth(65);
                        imageView.setFitHeight(90);
                        imageView.setPreserveRatio(true);
                        
                        // Carrega em background para o app não travar esperando baixar a imagem
                        Image img = new Image(urlImagem, true);
                        imageView.setImage(img);
                        setGraphic(imageView);
                    } catch (Exception e) {
                        setGraphic(null);
                    }
                }
            }
        });

        carregarFilmes();
    }

    private void carregarFilmes() {
        new Thread(() -> {
            try {
                JsonObject resultado = back4AppService.listarFilmes();
                if (resultado != null && resultado.has("results")) {
                    JsonArray array = resultado.getAsJsonArray("results");
                    listaFilmes.clear(); // Limpa para evitar duplicados
                    
                    for (JsonElement elem : array) {
                        JsonObject obj = elem.getAsJsonObject();

                        String titulo = obj.has("titulo") ? obj.get("titulo").getAsString() : "";
                        String categoria = obj.has("categoria") ? obj.get("categoria").getAsString() : "";
                        String classificacao = obj.has("classificacao") ? obj.get("classificacao").getAsString() : "";
                        String sinopse = obj.has("sinopse") ? obj.get("sinopse").getAsString() : "";
                        String fotoFilmeUrl = obj.has("fotoFilmeUrl") ? obj.get("fotoFilmeUrl").getAsString() : "";
                        String objectId = obj.has("objectId") ? obj.get("objectId").getAsString() : "";

                        Filme filme = new Filme(titulo, categoria, classificacao, sinopse, fotoFilmeUrl);
                        filme.setObjectId(objectId);

                        listaFilmes.add(filme);
                    }

                    javafx.application.Platform.runLater(() -> tabelaFilmes.setItems(listaFilmes));
                }
            } catch (Exception e) {
                System.err.println("Erro ao carregar lista de filmes: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleIrParaPerfil(ActionEvent event) {
        Stage stage = (Stage) tabelaFilmes.getScene().getWindow();
        Navegacao.mudarTela(stage, "EditarUsuario.fxml", "BlockFilmes - Meu Perfil");
    }

    @FXML
    private void handleIrParaCadastro(ActionEvent event) {
        Stage stage = (Stage) tabelaFilmes.getScene().getWindow();
        Navegacao.mudarTela(stage, "CadastroFilme.fxml", "BlockFilmes - Cadastrar Filme");
    }

    @FXML
    private void handleSair(ActionEvent event) {
        Stage stage = (Stage) tabelaFilmes.getScene().getWindow();
        Navegacao.mudarTela(stage, "login.fxml", "BlockFilmes - Login");
    }
}