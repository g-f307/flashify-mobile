# ⚡ Flashify Mobile

![Plataformas](https://img.shields.io/badge/Plataforma-Android-3DDC84?style=for-the-badge&logo=android)
![Linguagens](https://img.shields.io/badge/Feito%20com-Kotlin%20%26%20Python-blue?style=for-the-badge&logo=kotlin)

O Flashify é um aplicativo móvel de flashcards inteligente, projetado para otimizar o aprendizado e a memorização. Ele permite que os usuários criem, organizem e estudem seus próprios baralhos de flashcards.

O grande diferencial deste projeto é a **geração automática de flashcards e quizzes** usando Inteligência Artificial (Google Vertex AI / Gemini), permitindo que os usuários transformem documentos e anotações em material de estudo com apenas alguns cliques.

## 🚀 Principais Funcionalidades

* **Autenticação de Usuário:** Sistema completo de Login e Registro com gerenciamento de sessão via tokens JWT.
* **Gestão de Biblioteca:** Crie e organize baralhos (Decks) e Pastas para agrupar seus assuntos.
* **Criação de Flashcards:** Adicione, edite e remova flashcards manualmente (frente e verso).
* **✨ Geração com IA:** Faça upload de documentos (.pdf, .txt, etc.) e deixe a IA criar flashcards e quizzes completos sobre o conteúdo.
* **Modos de Estudo:**
    * **Estudo Clássico:** Revise seus flashcards um por um.
    * **Quiz:** Teste seu conhecimento com quizzes de múltipla escolha gerados a partir dos seus baralhos.
* **Acompanhamento de Progresso:** Visualize estatísticas e acompanhe sua evolução nos estudos.
* **Armazenamento Local:** O aplicativo usa um banco de dados Room local para permitir o acesso e estudo offline.

---

## 🛠️ Pilha de Tecnologias

O projeto é dividido em duas partes principais: o aplicativo móvel (`front/`) e a API de backend (`back/`).

### 📱 Frontend (Aplicativo Android)

* **Linguagem:** [Kotlin](https://kotlinlang.org/)
* **UI:** [Jetpack Compose](https://developer.android.com/jetpack/compose) para uma interface de usuário declarativa e moderna.
* **Arquitetura:** MVVM (Model-View-ViewModel)
* **Assincronismo:** Kotlin Coroutines & Flow
* **Banco de Dados Local:** [Room](https://developer.android.com/jetpack/androidx/releases/room) para cache offline de baralhos e flashcards.
* **Networking:** [Retrofit](https://square.github.io/retrofit/) (implícito por `ApiService.kt`) para consumir a API REST.
* **Gerenciamento de Dependências:** Gradle com [Version Catalog](https://docs.gradle.org/current/userguide/version_catalogues.html) (`libs.versions.toml`).

### ⚙️ Backend (API)

* **Framework:** [Python 3](https://www.python.org/) com [FastAPI](https://fastapi.tiangolo.com/) para alta performance.
* **Banco de Dados:** [SQLAlchemy](https://www.sqlalchemy.org/) (ORM) com [PostgreSQL](https://www.postgresql.org/) (recomendado).
* **Autenticação:** JWT (Passlib e python-jose).
* **Tarefas Assíncronas:** [Celery](https://docs.celeryq.dev/en/stable/) (com [Redis](https://redis.io/) como broker) para processar uploads de documentos e geração de IA em segundo plano.
* **Inteligência Artificial:** [Google Cloud AI Platform](https://cloud.google.com/vertex-ai) (Vertex AI / Gemini) para extração de texto e geração de conteúdo.
* **Validação de Dados:** [Pydantic](https://docs.pydantic.dev/latest/) (usado nativamente pelo FastAPI).
* **Containerização:** [Docker](https://www.docker.com/) e `Dockerfile` para fácil deploy.

---

## 📂 Estrutura do Projeto

```text
flashify-mobile/
└── 📁 front/           # Projeto Android (Módulo Raiz)
    ├── 📁 app/          # Módulo principal do aplicativo
    │   ├── src/
    │   ├── ├── androidTest/ # Testes instrumentados
    │   │   ├── main/
    │   │   │   ├── java/com/example/flashify/
    │   │   │   │   ├── 📁 model/    # Camada de Dados
    │   │   │   │   │   ├── 📁 data/         # Data classes (DTOs da API)
    │   │   │   │   │   ├── 📁 database/   # Configuração do Room (DAO, Entities, DB)
    │   │   │   │   │   ├── 📁 manager/    # Classes utilitárias (TokenManager)
    │   │   │   │   │   ├── 📁 model/network/  # Configuração do Retrofit (ApiService)
    │   │   │   │   │   └── 📁 util/       # Constantes e Navegação (Rotas.kt)
    │   │   │   │   │
    │   │   │   │   ├── 📁 view/     # Camada de UI (Jetpack Compose)
    │   │   │   │   │   ├── 📁 ui/
    │   │   │   │   │   │   ├── 📁 components/ # Componentes reutilizáveis (NavBar)
    │   │   │   │   │   │   ├── 📁 screen/     # Telas da aplicação (Login, Principal, Estudo...)
    │   │   │   │   │   │   └── 📁 theme/      # Configuração de Tema (Color, Type, Theme)
    │   │   │   │   │
    │   │   │   │   └── 📁 viewmodel/ # ViewModels (Lógica de estado e negócios)
    │   │   │   │
    │   │   │   ├── res/          # Recursos (ícones, fontes, XMLs)
    │   │   │   └── AndroidManifest.xml
    │   │   │
    │   │   └── test/       # Testes unitários
    │   │
    │   ├── build.gradle.kts  # Configs do módulo 'app'
    │   └── .gitignore
    │
    ├── gradle/          # Wrapper do Gradle
    ├── build.gradle.kts   # Configs do projeto raiz
    ├── gradle.properties
    ├── gradlew            # Script Gradle (Linux/macOS)
    ├── gradlew.bat        # Script Gradle (Windows)
    ├── local.properties   # (Ignorado) Caminho do SDK
    └── settings.gradle.kts # Configs de módulos do Gradle

```

---

## 🏁 Como Rodar o Projeto

Para executar o projeto completo, você precisará configurar o Backend e o Frontend.

### Pré-requisitos

* [Android Studio](https://developer.android.com/studio) (para o `front/`)
* [Python 3.10+](https://www.python.org/downloads/) (para o `back/`)
* [Docker](https://www.docker.com/get-started/) e [Docker Compose](https://docs.docker.com/compose/install/) (para rodar o `back/` e seus serviços)
* Conta no [Google Cloud](https://cloud.google.com/) com a API do Vertex AI ativada e um arquivo de credenciais (`gcp-credentials.json`).

### 1. Configurando o Backend (`back/`)

A forma mais fácil de rodar o backend é usando Docker.

1.  **Navegue até a pasta `back/`:**
    ```bash
    cd back
    ```
2.  **Credenciais do Google Cloud:**
    Adicione seu arquivo de credenciais `gcp-credentials.json` (baixado do Google Cloud) dentro da pasta `back/`.

3.  **Variáveis de Ambiente:**
    Copie o arquivo de exemplo para criar seu arquivo `.env`:
    ```bash
    cp .env.example .env
    ```
    Agora, **edite o arquivo `.env`** e preencha as variáveis, como os dados do banco de dados (se não usar docker-compose), o `SECRET_KEY` do JWT e o `GOOGLE_APPLICATION_CREDENTIALS` (ex: `./gcp-credentials.json`).

4.  **Suba os containers:**
    (Este comando irá buildar a imagem do FastAPI, e (idealmente) subir os serviços de Redis e PostgreSQL se definidos em um `docker-compose.yml`)
    ```bash
    docker-compose up -d --build 
    ```
    *Obs: Assumindo que você tenha um `docker-compose.yml` que defina os serviços `app`, `db` e `redis`. Se não, você precisará rodar o banco e o Redis manualmente e usar `docker build -t flashify-api .` e `docker run ...`.*

5.  **Alternativa (Sem Docker):**
    ```bash
    # Crie um ambiente virtual
    python -m venv venv
    source venv/bin/activate  # (ou .\\venv\\Scripts\\activate no Windows)

    # Instale as dependências
    pip install -r requirements.txt

    # Exporte as variáveis de ambiente (Linux/macOS)
    export $(cat .env | xargs)

    # Rode a API
    uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
    
    # Rode o Celery em outro terminal
    celery -A app.worker.celery_app worker --loglevel=info
    ```

O backend estará rodando em `http://localhost:8000`.

### 2. Configurando o Frontend (`front/`)

1.  **Abra o Android Studio:**
    Abra o projeto selecionando a pasta `front/`.

2.  **Sincronize o Gradle:**
    Aguarde o Android Studio baixar todas as dependências do Gradle.

3.  **Configure o Endereço da API:**
    * Encontre o arquivo `ApiService.kt` (provavelmente em `front/app/src/main/java/com/example/flashify/model/network/`).
    * Altere a `BASE_URL` para apontar para o seu backend.
    * **Importante:** Se estiver usando um **Emulador Android**, o `localhost` da sua máquina é acessível pelo IP `10.0.2.2`.

    ```kotlin
    // Exemplo em ApiService.kt
    private const val BASE_URL = "[http://10.0.2.2:8000/](http://10.0.2.2:8000/)" 
    ```

4.  **Rode o Aplicativo:**
    Clique no botão "Run" (▶) no Android Studio e selecione um emulador ou dispositivo físico conectado.