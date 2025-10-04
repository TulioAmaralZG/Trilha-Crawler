Nome: Tulio Fernandes Amaral

# ⚙️ Web Scraper de Histórico

Este projeto é um scraper desenvolvido em Groovy que extrai dados de uma tabela em uma página web, filtra esses dados com base em uma data e os salva em um arquivo CSV formatado.

## ✨ Funcionalidades

-   **Extração de Dados Web**: Acessa uma URL para baixar o conteúdo HTML de uma página.
-   **Parsing de HTML**: Utiliza a biblioteca Jsoup para analisar a estrutura da página e extrair dados de tabelas.
-   **Filtragem por Data**: Processa apenas as linhas da tabela cuja data (na terceira coluna) seja posterior a Janeiro de 2016.
-   **Manipulação de Dados**:
    -   Reconhece múltiplos formatos de data (`dd/MM/yyyy` e `MMM/yyyy`).
    -   Separa a coluna de data em duas novas colunas: **Mês** e **Ano**.
-   **Exportação para CSV**: Salva os dados filtrados e formatados em um arquivo `Historico_versoes.csv` na pasta de Downloads do usuário.

## 🚀 Pré-requisitos

-   Java JDK (versão 8 ou superior)
-   Gradle

## 🔧 Configuração

1.  **Dependências**: O projeto utiliza a biblioteca `Jsoup` para parsing de HTML. Certifique-se de que ela está no seu [build.gradle](cci:7://file:///Users/tuliofernandesamaral/IdeaProjects/untitled1/build.gradle:0:0-0:0):

    ```groovy
    dependencies {
        implementation 'org.jsoup:jsoup:1.15.3' // Exemplo de versão
        // ... outras dependências
    }
    ```

2.  **URL Alvo**: A URL da página a ser extraída é passada como argumento para o método `baixarInformacoesHistorico` na classe [Core.groovy](cci:7://file:///Users/tuliofernandesamaral/IdeaProjects/untitled1/src/main/groovy/Core.groovy:0:0-0:0).

## 🏃 Como Executar

1.  Abra o projeto em sua IDE (ex: IntelliJ IDEA).
2.  Localize o arquivo de entrada da aplicação ([Main.groovy](cci:7://file:///Users/tuliofernandesamaral/IdeaProjects/untitled1/src/main/groovy/Main.groovy:0:0-0:0)).
3.  Execute o método `main`.

O script irá rodar, realizar a extração e, ao final, um arquivo chamado `Historico_versoes.csv` será criado na sua pasta `~/Downloads`.

## 📂 Estrutura do Projeto
