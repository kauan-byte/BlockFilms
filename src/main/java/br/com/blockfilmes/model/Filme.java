package br.com.blockfilmes.model;

public class Filme {
    private String objectId;
    private String titulo;
    private String categoria; 
    private String classificacao; 
    private String sinopse;
    private String fotoFilmeUrl;
    private String ano; // Novo atributo adicionado

    public Filme() {}

    public Filme(String titulo, String categoria, String classificacao, String sinopse, String fotoFilmeUrl, String ano) {
        this.titulo = titulo;
        this.categoria = categoria;
        this.classificacao = classificacao;
        this.sinopse = sinopse;
        this.fotoFilmeUrl = fotoFilmeUrl;
        this.ano = ano;
    }

    public String getObjectId() { return objectId; }
    public void setObjectId(String objectId) { this.objectId = objectId; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getClassificacao() { return classificacao; }
    public void setClassificacao(String classificacao) { this.classificacao = classificacao; }

    public String getSinopse() { return sinopse; }
    public void setSinopse(String sinopse) { this.sinopse = sinopse; }

    public String getFotoFilmeUrl() { return fotoFilmeUrl; }
    public void setFotoFilmeUrl(String fotoFilmeUrl) { this.fotoFilmeUrl = fotoFilmeUrl; }

    public String getAno() { return ano; }
    public void setAno(String ano) { this.ano = ano; }
}