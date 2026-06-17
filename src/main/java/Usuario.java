package br.com.blockfilmes.model;

public class Usuario {
    private String objectId; // ID gerado automaticamente pelo Back4App
    private String nomeCompleto;
    private String username;
    private String email;
    private String fotoPerfilUrl;

    // Construtor vazio (necessário para o Gson)
    public Usuario() {}

    public Usuario(String nomeCompleto, String username, String email, String fotoPerfilUrl) {
        this.nomeCompleto = nomeCompleto;
        this.username = username;
        this.email = email;
        this.fotoPerfilUrl = fotoPerfilUrl;
    }

    // Getters e Setters
    public String getObjectId() { return objectId; }
    public void setObjectId(String objectId) { this.objectId = objectId; }

    public String getNomeCompleto() { return nomeCompleto; }
    public void setNomeCompleto(String nomeCompleto) { this.nomeCompleto = nomeCompleto; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFotoPerfilUrl() { return fotoPerfilUrl; }
    public void setFotoPerfilUrl(String fotoPerfilUrl) { this.fotoPerfilUrl = fotoPerfilUrl; }
}