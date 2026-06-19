package br.com.blockfilmes.util;

public class SessaoUsuario {

    private static String sessionToken;
    private static String objectId;

    public static String getSessionToken() {
        return sessionToken;
    }

    public static void setSessionToken(String token) {
        sessionToken = token;
    }

    public static String getObjectId() {
        return objectId;
    }

    public static void setObjectId(String id) {
        objectId = id;
    }

    private static br.com.blockfilmes.model.Filme filmeSelecionadoParaAvaliacao;

    public static void setFilmeSelecionado(br.com.blockfilmes.model.Filme filme) {
        filmeSelecionadoParaAvaliacao = filme;
    }

    public static br.com.blockfilmes.model.Filme getFilmeSelecionado() {
        return filmeSelecionadoParaAvaliacao;
    }
}
