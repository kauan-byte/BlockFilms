package br.com.blockfilmes.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.File;
import java.nio.file.Files;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Back4AppService {

    private static final String BASE_URL = "https://parseapi.back4app.com";
    private static final String APP_ID = "QVDmGGbnR0V4SK7GEJVhNfE5xMvsBlPKL10YEkoe";
    private static final String REST_API_KEY = "VzSQNh3y5Patgl6Lm0XPxiZnuLIYUxRdTHj3pBuU";

    private final java.net.http.HttpClient client;
    private final Gson gson;

    public Back4AppService() {
        this.client = java.net.http.HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    // 1. Login
    public JsonObject login(String username, String password) throws Exception {
        String url = BASE_URL + "/login?username=" + java.net.URLEncoder.encode(username, "UTF-8")
                + "&password=" + java.net.URLEncoder.encode(password, "UTF-8");

        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(url))
                .header("X-Parse-Application-Id", APP_ID)
                .header("X-Parse-REST-API-Key", REST_API_KEY)
                .GET()
                .build();

        java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return gson.fromJson(response.body(), JsonObject.class);
        } else {
            throw new Exception("Erro no login: " + response.body());
        }
    }

    // 2. Listar Filmes
    public JsonObject listarFilmes() throws Exception {
        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(BASE_URL + "/classes/Filme"))
                .header("X-Parse-Application-Id", APP_ID)
                .header("X-Parse-REST-API-Key", REST_API_KEY)
                .GET()
                .build();

        java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
        return gson.fromJson(response.body(), JsonObject.class);
    }

    // 3. Cadastrar Filme (ATUALIZADO COM ANO)
    public JsonObject cadastrarFilme(String titulo, String categoria, String classificacao, String sinopse, String fotoFilmeUrl, String ano) throws Exception {
        JsonObject dados = new JsonObject();
        dados.addProperty("titulo", titulo);
        dados.addProperty("categoria", categoria);
        dados.addProperty("classificacao", classificacao);
        dados.addProperty("sinopse", sinopse);
        dados.addProperty("fotoFilmeUrl", fotoFilmeUrl);
        dados.addProperty("ano", ano); // Adiciona o ano no banco

        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(BASE_URL + "/classes/Filme"))
                .header("X-Parse-Application-Id", APP_ID)
                .header("X-Parse-REST-API-Key", REST_API_KEY)
                .header("Content-Type", "application/json")
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(gson.toJson(dados)))
                .build();

        java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 201 || response.statusCode() == 200) {
            return gson.fromJson(response.body(), JsonObject.class);
        } else {
            throw new Exception("Erro ao cadastrar filme: " + response.body());
        }
    }

    // NOVO MÉTODO 3b. Atualizar Filme Existente (O que estava faltando!)
    public JsonObject atualizarFilme(String objectId, String titulo, String categoria, String classificacao, String sinopse, String fotoFilmeUrl, String ano) throws Exception {
        JsonObject dados = new JsonObject();
        dados.addProperty("titulo", titulo);
        dados.addProperty("categoria", categoria);
        dados.addProperty("classificacao", classificacao);
        dados.addProperty("sinopse", sinopse);
        dados.addProperty("fotoFilmeUrl", fotoFilmeUrl);
        dados.addProperty("ano", ano); // Sincroniza a alteração do ano no banco

        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(BASE_URL + "/classes/Filme/" + objectId))
                .header("X-Parse-Application-Id", APP_ID)
                .header("X-Parse-REST-API-Key", REST_API_KEY)
                .header("Content-Type", "application/json")
                .PUT(java.net.http.HttpRequest.BodyPublishers.ofString(gson.toJson(dados))) // Requisição PUT para editar
                .build();

        java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return gson.fromJson(response.body(), JsonObject.class);
        } else {
            throw new Exception("Erro ao atualizar filme no Back4App: " + response.body());
        }
    }

    // 4. Cadastrar Novo Usuário
    public JsonObject cadastrarUsuario(String username, String email, String password, String telefone, String fotoPerfilUrl) throws Exception {
        JsonObject dados = new JsonObject();
        dados.addProperty("username", username);
        dados.addProperty("email", email);
        dados.addProperty("password", password);
        dados.addProperty("telefone", telefone);
        dados.addProperty("fotoPerfilUrl", fotoPerfilUrl);

        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(BASE_URL + "/users"))
                .header("X-Parse-Application-Id", APP_ID)
                .header("X-Parse-REST-API-Key", REST_API_KEY)
                .header("Content-Type", "application/json")
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(gson.toJson(dados)))
                .build();

        java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 201 || response.statusCode() == 200) {
            return gson.fromJson(response.body(), JsonObject.class);
        } else {
            throw new Exception("Erro ao cadastrar usuário: " + response.body());
        }
    }

    // 5a. Método de Upload que aceita APENAS o arquivo
    public JsonObject uploadImagem(File arquivo) throws Exception {
        String urlString = executarUpload(arquivo);
        JsonObject jsonFake = new JsonObject();
        jsonFake.addProperty("url", urlString);
        return jsonFake;
    }

    // 5b. Método de Upload que aceita Arquivo E uma String
    public JsonObject uploadImagem(File arquivo, String parametroAdicional) throws Exception {
        String urlString = executingUpload(arquivo);
        JsonObject jsonFake = new JsonObject();
        jsonFake.addProperty("url", urlString);
        return jsonFake;
    }

    private String executingUpload(File arquivo) throws Exception {
         return executarUpload(arquivo);
    }

    // 5b. MÉTODO EXCLUSIVO PARA O PERFIL
    public String uploadImagemPerfil(File arquivo, String sessionToken) throws Exception {
        String nomeArquivo = arquivo.getName();
        byte[] dadosArquivo = Files.readAllBytes(arquivo.toPath());
        String contentType = Files.probeContentType(arquivo.toPath());

        if (contentType == null) {
            contentType = "image/jpeg";
        }

        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(BASE_URL + "/files/" + nomeArquivo))
                .header("X-Parse-Application-Id", APP_ID)
                .header("X-Parse-REST-API-Key", REST_API_KEY)
                .header("X-Parse-Session-Token", sessionToken)
                .header("Content-Type", contentType)
                .POST(java.net.http.HttpRequest.BodyPublishers.ofByteArray(dadosArquivo))
                .build();

        java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 201 || response.statusCode() == 200) {
            JsonObject json = gson.fromJson(response.body(), JsonObject.class);
            return json.get("url").getAsString();
        } else {
            throw new Exception("Erro no upload de perfil autenticado: " + response.body());
        }
    }

    // Motor interno de upload compartilhado por todos os métodos acima
    private String executarUpload(File arquivo) throws Exception {
        String nomeArquivo = arquivo.getName();
        byte[] dadosArquivo = Files.readAllBytes(arquivo.toPath());
        String contentType = Files.probeContentType(arquivo.toPath());

        if (contentType == null) {
            contentType = "image/jpeg";
        }

        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(BASE_URL + "/files/" + nomeArquivo))
                .header("X-Parse-Application-Id", APP_ID)
                .header("X-Parse-REST-API-Key", REST_API_KEY)
                .header("Content-Type", contentType)
                .POST(java.net.http.HttpRequest.BodyPublishers.ofByteArray(dadosArquivo))
                .build();

        java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 201 || response.statusCode() == 200) {
            JsonObject json = gson.fromJson(response.body(), JsonObject.class);
            return json.get("url").getAsString();
        } else {
            throw new Exception("Erro no upload do arquivo: " + response.body());
        }
    }

    // 6. Buscar Usuário Atual
    public JsonObject buscarUsuarioAtual(String sessionToken) throws Exception {
        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(BASE_URL + "/users/me"))
                .header("X-Parse-Application-Id", APP_ID)
                .header("X-Parse-REST-API-Key", REST_API_KEY)
                .header("X-Parse-Session-Token", sessionToken)
                .GET()
                .build();

        java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return gson.fromJson(response.body(), JsonObject.class);
        } else {
            throw new Exception("Erro ao buscar dados do usuário: " + response.body());
        }
    }

    // 7. Atualizar Usuário
    public void atualizarUsuario(String objectId, String sessionToken, JsonObject dadosAtualizados) throws Exception {
        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(BASE_URL + "/users/" + objectId))
                .header("X-Parse-Application-Id", APP_ID)
                .header("X-Parse-REST-API-Key", REST_API_KEY)
                .header("X-Parse-Session-Token", sessionToken)
                .header("Content-Type", "application/json")
                .PUT(java.net.http.HttpRequest.BodyPublishers.ofString(gson.toJson(dadosAtualizados)))
                .build();

        java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new Exception("Erro ao atualizar usuário: " + response.body());
        }
    }
    
    // Método temporário para simular as avaliações
    public JsonObject buscarAvaliacoesPorFilmeSimulado(String idFilme) {
        JsonObject respostaFake = new JsonObject();
        com.google.gson.JsonArray listaComentarios = new com.google.gson.JsonArray();

        JsonObject c1 = new JsonObject();
        c1.addProperty("nomeUsuario", "Gabriel Silva (Web)");
        c1.addProperty("nota", 5);
        c1.addProperty("comentario", "Filme espetacular! Uma obra-prima da ficção científica.");
        
        JsonObject c2 = new JsonObject();
        c2.addProperty("nomeUsuario", "Amanda Souza (Mobile)");
        c2.addProperty("nota", 4);
        c2.addProperty("comentario", "Muito bom, mas achei o final um pouco confuso.");

        JsonObject c3 = new JsonObject();
        c3.addProperty("nomeUsuario", "Lucas Ramos (Web)");
        c3.addProperty("nota", 2);
        c3.addProperty("comentario", "Não gostei muito. Ritmo muito lento.");

        listaComentarios.add(c1);
        listaComentarios.add(c2);
        listaComentarios.add(c3);

        respostaFake.add("results", listaComentarios);
        return respostaFake;
    }

    public boolean excluirUsuario(String objectId, String sessionToken) throws Exception {
        String url = "https://parseapi.back4app.com/users/" + objectId;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("X-Parse-Application-Id", APP_ID)
                .header("X-Parse-REST-API-Key", REST_API_KEY)
                .header("X-Parse-Session-Token", sessionToken)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode() == 200;
    }
    
    // Novo método para deletar o filme do banco de dados
public boolean excluirFilme(String objectId) throws Exception {
    java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
            .uri(java.net.URI.create(BASE_URL + "/classes/Filme/" + objectId))
            .header("X-Parse-Application-Id", APP_ID)
            .header("X-Parse-REST-API-Key", REST_API_KEY)
            .DELETE() // Requisição HTTP DELETE
            .build();

    java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

    // O Back4App retorna status 200 OK quando o objeto é deletado com sucesso
    return response.statusCode() == 200;
}
}