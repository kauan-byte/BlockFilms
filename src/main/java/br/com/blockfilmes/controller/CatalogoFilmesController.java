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
import javafx.scene.control.Control;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class CatalogoFilmesController implements Initializable {

    @FXML
    private TableView<Filme> tabelaFilmes;
    @FXML
    private TableColumn<Filme, String> colCapa;
    @FXML
    private TableColumn<Filme, String> colTitulo;
    @FXML
    private TableColumn<Filme, String> colCategoria;
    @FXML
    private TableColumn<Filme, String> colAno; // Nova injeção
    @FXML
    private TableColumn<Filme, String> colClassificacao;
    @FXML
    private TableColumn<Filme, String> colSinopse;
    @FXML
    private TableColumn<Filme, Void> colAcoes;
    @FXML
    private Button btnPerfil;

    private Back4AppService back4AppService;
    private ObservableList<Filme> listaFilmes = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.back4AppService = new Back4AppService();

        tabelaFilmes.setSelectionModel(null);

        // Configuração das colunas de texto
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colAno.setCellValueFactory(new PropertyValueFactory<>("ano")); // Mapeado
        colClassificacao.setCellValueFactory(new PropertyValueFactory<>("classificacao"));
        colSinopse.setCellValueFactory(new PropertyValueFactory<>("sinopse"));
        colCapa.setCellValueFactory(new PropertyValueFactory<>("fotoFilmeUrl"));

        colSinopse.setCellFactory(tc -> {
            TableCell<Filme, String> cell = new TableCell<>();
            Text text = new Text();
            cell.setGraphic(text);
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            text.wrappingWidthProperty().bind(colSinopse.widthProperty().subtract(10));
            text.styleProperty().bind(cell.styleProperty());
            text.setFill(javafx.scene.paint.Color.WHITE);
            cell.itemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == null) {
                    text.setText("");
                } else {
                    text.setText(newVal);
                }
            });
            return cell;
        });

        colCapa.setCellFactory(tc -> new TableCell<Filme, String>() {
            private final ImageView imageView = new ImageView();

            @Override
            protected void updateItem(String urlImagem, boolean empty) {
                super.updateItem(urlImagem, empty);
                if (empty || urlImagem == null || urlImagem.trim().isEmpty()) {
                    setGraphic(null);
                } else {
                    try {
                        imageView.setFitWidth(65);
                        imageView.setFitHeight(90);
                        imageView.setPreserveRatio(true);

                        Image img = new Image(urlImagem, true);
                        imageView.setImage(img);
                        setGraphic(imageView);
                    } catch (Exception e) {
                        setGraphic(null);
                    }
                }
            }
        });

        colAcoes.setCellFactory(tc -> new TableCell<Filme, Void>() {
            private final Button btnAvaliacoes = new Button("👁️");
            private final Button btnEditar = new Button("✏️");
            private final Button btnExcluir = new Button("🗑️");
            private final HBox containerBotoes = new HBox(8, btnAvaliacoes, btnEditar, btnExcluir);

            {
                btnAvaliacoes.setStyle("-fx-background-color: #444444; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
                btnEditar.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black; -fx-cursor: hand; -fx-font-weight: bold;");
                btnExcluir.setStyle("-fx-background-color: #e50914; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");

                containerBotoes.setAlignment(javafx.geometry.Pos.CENTER);

                btnAvaliacoes.setOnAction(event -> {
                    Filme filmeSelecionado = getTableView().getItems().get(getIndex());
                    SessaoUsuario.setFilmeSelecionado(filmeSelecionado);
                    Stage stage = (Stage) tabelaFilmes.getScene().getWindow();
                    Navegacao.mudarTela(stage, "AvaliacoesFilme.fxml", "BlockFilmes - Avaliações");
                });

                btnEditar.setOnAction(event -> {
                    Filme filmeSelecionado = getTableView().getItems().get(getIndex());
                    SessaoUsuario.setFilmeSelecionado(filmeSelecionado);
                    Stage stage = (Stage) tabelaFilmes.getScene().getWindow();
                    Navegacao.mudarTela(stage, "CadastroFilme.fxml", "BlockFilmes - Editar Filme");
                });

                // 3. AÇÃO DO BOTÃO EXCLUIR REAL NO BACK4APP
                btnExcluir.setOnAction(event -> {
                    Filme filmeSelecionado = getTableView().getItems().get(getIndex());

                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Excluir Filme");
                    alert.setHeaderText("Tem certeza que deseja excluir o filme?");
                    alert.setContentText("Filme: " + filmeSelecionado.getTitulo());

                    alert.showAndWait().ifPresent(resposta -> {
                        if (resposta == javafx.scene.control.ButtonType.OK) {
                            // Criamos uma thread para não travar a tela enquanto deleta da internet
                            new Thread(() -> {
                                try {
                                    boolean deletado = back4AppService.excluirFilme(filmeSelecionado.getObjectId());

                                    if (deletado) {
                                        // Se deletou do banco, removemos da tabela na tela (UI Thread)
                                        javafx.application.Platform.runLater(() -> {
                                            getTableView().getItems().remove(filmeSelecionado);
                                        });
                                        System.out.println("Filme excluído com sucesso do Back4App: " + filmeSelecionado.getTitulo());
                                    } else {
                                        System.err.println("Não foi possível excluir o filme do Back4App.");
                                    }
                                } catch (Exception e) {
                                    System.err.println("Erro ao excluir filme: " + e.getMessage());
                                    e.printStackTrace();
                                }
                            }).start();
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(containerBotoes);
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

                    // CORREÇÃO: Limpamos os dados locais na UI Thread antes de repopular para forçar a atualização visual imediata
                    javafx.application.Platform.runLater(() -> listaFilmes.clear());

                    for (JsonElement elem : array) {
                        JsonObject obj = elem.getAsJsonObject();

                        String titulo = obj.has("titulo") ? obj.get("titulo").getAsString() : "";
                        String categoria = obj.has("categoria") ? obj.get("categoria").getAsString() : "";
                        String classificacao = obj.has("classificacao") ? obj.get("classificacao").getAsString() : "";
                        String sinopse = obj.has("sinopse") ? obj.get("sinopse").getAsString() : "";
                        String fotoFilmeUrl = obj.has("fotoFilmeUrl") ? obj.get("fotoFilmeUrl").getAsString() : "";
                        String objectId = obj.has("objectId") ? obj.get("objectId").getAsString() : "";
                        String ano = obj.has("ano") ? obj.get("ano").getAsString() : ""; // Capturando do JSON

                        // Repassando o ano ao Construtor atualizado
                        Filme filme = new Filme(titulo, categoria, classificacao, sinopse, fotoFilmeUrl, ano);
                        filme.setObjectId(objectId);

                        javafx.application.Platform.runLater(() -> listaFilmes.add(filme));
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
        SessaoUsuario.setFilmeSelecionado(null);
        Stage stage = (Stage) tabelaFilmes.getScene().getWindow();
        Navegacao.mudarTela(stage, "CadastroFilme.fxml", "BlockFilmes - Cadastrar Filme");
    }

    @FXML
    private void handleSair(ActionEvent event) {
        Stage stage = (Stage) tabelaFilmes.getScene().getWindow();
        Navegacao.mudarTela(stage, "login.fxml", "BlockFilmes - Login");
    }
}
