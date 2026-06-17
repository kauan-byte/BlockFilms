package br.com.blockfilmes.blockfilmes;

import br.com.blockfilmes.util.Navegacao;
import javafx.application.Application;
import javafx.stage.Stage;

public class BlockFilmes extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Inicializa o sistema abrindo a tela de login.fxml
        Navegacao.mudarTela(stage, "login.fxml", "BlockFilmes - Login");
    }
}