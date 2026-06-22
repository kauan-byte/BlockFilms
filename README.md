# BlockFilmes 🎬

O **BlockFilmes** é um sistema completo de catálogo e avaliação de filmes, desenvolvido como projeto acadêmico. O ecossistema é dividido em duas aplicações independentes que compartilham o mesmo banco de dados em nuvem.

---

## 👥 Estrutura do Projeto e Divisão da Equipe

O projeto foi unificado em um único repositório para demonstrar a integração entre plataformas:

* **Painel Desktop (JavaFX):** Desenvolvido por **[Seu Nome Aqui]**. É a área administrativa do sistema, permitindo o cadastro de novos filmes, gerenciamento de dados e monitoramento de todas as avaliações enviadas pelos usuários.
* **Portal Web (JavaScript/HTML/CSS):** Desenvolvido por **[Nome do Seu Amigo Aqui]**. É a interface voltada ao cliente final, onde os usuários navegam pelo catálogo de filmes, visualizam detalhes e registram suas notas e comentários.

---

## 📊 Diagramas de Arquitetura (UML)

### 1. Diagrama de Casos de Uso (Fronteira do Sistema)
Este diagrama representa os limites do sistema (campo quadrado), as funcionalidades internas por plataforma e as interações do Usuário dependendo de qual ambiente ele está operando.

```mermaid
graph TD
    %% Definição Visual dos Atores (Fora da Caixa)
    UserWeb["👤 USUÁRIO WEB<br>(Cliente / Navegador)"]
    UserDesk["💻 USUÁRIO DESKTOP<br>(Gerência / Java)"]

    style UserWeb fill:#222,stroke:#fff,stroke-width:2px,color:#fff
    style UserDesk fill:#222,stroke:#fff,stroke-width:2px,color:#fff

    %% O CAMPO QUADRADO (Fronteira do Sistema)
    subgraph Caixa_Sistema [FRONTEIRA DO SISTEMA: BLOCKFILMES]
        style Caixa_Sistema fill:#111,stroke:#e50914,stroke-width:3px,color:#fff
        
        %% Funcionalidade de Acesso
        CU_Login([Fazer Login / Criar Conta])
        
        %% Escopo de Telas da Web
        subgraph Modulo_Web [Plataforma Web]
            style Modulo_Web fill:#222,stroke:#333
            CU1([Navegar pelo Catálogo])
            CU2([Visualizar Detalhes do Filme])
            CU3([Enviar Nota e Comentário])
        end

        %% Escopo de Telas do Java Desktop
        subgraph Modulo_Desktop [Plataforma Desktop]
            style Modulo_Desktop fill:#222,stroke:#333
            CU4([Cadastrar Novo Filme])
            CU5([Atualizar Dados do Filme])
            CU6([Excluir Filme do Catálogo])
            CU7([Monitorar Avaliações])
        end
    end

    %% Banco de Dados (Fora da Caixa)
    BD[("☁️ Nuvem Back4App<br>(Banco de Dados)")]
    style BD fill:#1f1f1f,stroke:#007acc,stroke-width:2px

    %% Setas de Interação da Web
    UserWeb --> CU_Login
    UserWeb --> CU1
    UserWeb --> CU2
    UserWeb --> CU3

    %% Setas de Interação do Desktop
    UserDesk --> CU_Login
    UserDesk --> CU4
    UserDesk --> CU5
    UserDesk --> CU6
    UserDesk --> CU7

    %% Fluxo de Persistência no Banco de Dados
    CU_Login ===> BD
    CU3 ===> BD
    CU4 ===> BD
    CU5 ===> BD
    CU6 ===> BD
classDiagram
    %% Classe Base de Usuário
    class Usuario {
        +String objectId
        +String username
        +String email
        +String password
        +String telefone
        +String fotoPerfilUrl
        +cadastrarUsuario() JsonObject
        +login() JsonObject
        +buscarUsuarioAtual() JsonObject
        +atualizarUsuario() void
        +excluirUsuario() boolean
    }

    %% Classe Filme
    class Filme {
        +String objectId
        +String titulo
        +String categoria
        +String classificacao
        +String sinopse
        +String fotoFilmeUrl
        +String ano
        +listarFilmes() JsonObject
        +cadastrarFilme() JsonObject
        +atualizarFilme() JsonObject
        +excluirFilme() boolean
    }

    %% Classe Avaliacao (Tabela 'avaliacoes')
    class Avaliacao {
        +String objectId
        +String nome_filme
        +int nota
        +String comentario
        +Pointer usuario_
        +buscarAvaliacoesPorFilmeSimulado() JsonObject
        +salvarAvaliacao() JsonObject
    }

    %% Relacionamentos Formais da UML
    Avaliacao "0..*" --> "1" Usuario : vincula_a (usuario_)
    Avaliacao "0..*" --> "1" Filme : avalia
