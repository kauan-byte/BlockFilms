package br.com.blockfilmes.util;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Navegacao {

    /**
     * Altera a tela atual do sistema para uma nova tela FXML.
     * @param stage O palco atual (janela) do JavaFX
     * @param fxmlName O nome do arquivo FXML dentro de resources/view/ (Ex: "login.fxml")
     * @param titulo O título que aparecerá na barra da janela
     */
    public static void mudarTela(Stage stage, String fxmlName, String titulo) {
        try {
            // Busca o arquivo FXML dentro da pasta view em resources
            String caminho = "/view/" + fxmlName;
            FXMLLoader loader = new FXMLLoader(Navegacao.class.getResource(caminho));
            Parent root = loader.load();
            
            // Define a nova cena mantendo o tamanho ou aplicando um padrão
            Scene scene = new Scene(root, 1000, 700);
            stage.setScene(scene);
            stage.setTitle(titulo);
            stage.centerOnScreen(); // Centraliza a nova janela na tela do PC
            stage.show();
        } catch (IOException e) {
            System.err.println("Erro ao carregar a tela: " + fxmlName);
            e.printStackTrace();
        }
    }
}