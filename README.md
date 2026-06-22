# BlockFilmes 🎬

O **BlockFilmes** é um sistema completo de catálogo e avaliação de filmes, desenvolvido como projeto acadêmico. O ecossistema é dividido em duas aplicações independentes que compartilham o mesmo banco de dados em nuvem.

---

## 👥 Estrutura do Projeto e Divisão da Equipe

O projeto foi unificado em um único repositório para demonstrar a integração entre plataformas:

*   **Painel Desktop (JavaFX):** Desenvolvido por **[Seu Nome Aqui]**. É a área administrativa do sistema, permitindo o cadastro de novos filmes, gerenciamento de dados e monitoramento de todas as avaliações enviadas pelos usuários.
*   **Portal Web (JavaScript/HTML/CSS):** Desenvolvido por **[Nome do Seu Amigo Aqui]**. É a interface voltada ao cliente final, onde os usuários navegam pelo catálogo de filmes, visualizam detalhes e registram suas notas e comentários.

---

## 🚀 Tecnologias Utilizadas

### Backend & Banco de Dados
*   **Back4App (Parse API):** Persistência de dados em nuvem, autenticação de usuários e relacionamentos através de Pointers.

### Módulo Desktop (Java)
*   **Java 17 / JavaFX 21**
*   **Maven** (Gerenciamento de dependências)
*   **Gson** (Parseamento de JSON)

### Módulo Web
*   **HTML5 / CSS3 / JavaScript (ES6)**
*   **Fetch API** (Integração com o Back4App)

---

## 📦 Como Executar os Projetos

### 1. Executando o Painel Desktop (Java)
1. Certifique-se de ter o **JDK 17+** instalado.
2. Abra o projeto na sua IDE (NetBeans).
3. Aguarde o Maven baixar as dependências.
4. Execute o projeto pressionando **F6** ou limpando e construindo antes.

### 2. Executando o Portal Web
1. Abra os arquivos da pasta Web em qualquer navegador moderno clicando duas vezes no arquivo `index.html`.