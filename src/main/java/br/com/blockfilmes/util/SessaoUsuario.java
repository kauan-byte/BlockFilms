package br.com.blockfilmes.util;

public class SessaoUsuario {
    private static String sessionToken;
    private static String objectId;

    public static String getSessionToken() { return sessionToken; }
    public static void setSessionToken(String token) { sessionToken = token; }

    public static String getObjectId() { return objectId; }
    public static void setObjectId(String id) { objectId = id; }
}