# Pokéthnos

Jogo de cartas feito para o trabalho de GPMS. Os jogadores recrutam Pokémon, montam Bandos e disputam regiões do mapa para ganhar pontos de glória. O jogo tem 2 Eras e no final quem tiver mais glória ganha.

## Como o projeto é dividido

- `backend/` - o servidor, feito em Java com Spring Boot. Ele guarda a partida e cuida das regras, da pontuação e das habilidades das tribos.
- `frontend/` - a parte visual, feita em React com Vite.
- `cartas_pokethnos/` - material das cartas.

O frontend conversa com o backend por uma API REST (`/api/games`).

## O que precisa ter instalado

- Java 17
- Maven (ou usar o do IDE)
- Node.js (usei a versão 24, mas outras recentes devem funcionar)

## Como iniciar a partida

Precisa abrir **dois terminais**, um para o backend e outro para o frontend. O backend tem que ser ligado primeiro.

### 1. Ligar o backend

```
cd backend
mvn spring-boot:run
```

Quando aparecer algo como `Started PokethnosApplication`, ele está pronto em http://localhost:8080.

### 2. Ligar o frontend

```
cd frontend
npm install
npm run dev
```

O `npm install` só precisa ser feito na primeira vez.

### 3. Jogar

1. Abre http://localhost:5173 no navegador.
2. Na tela inicial, coloca o nome dos jogadores (de 2 a 6) e escolhe o treinador de cada um.
3. Clica para iniciar a partida.

Se der erro ao iniciar, olha se o backend está rodando. Sem ele a partida não começa.

## Como o turno funciona

Na sua vez você escolhe uma das duas opções:

- **Recrutar um aliado**, pegando uma carta do deck ou da mesa.
- **Formar um Bando**, com cartas da sua mão, e jogar ele nas regiões.

Algumas tribos têm habilidades que pedem uma decisão, como escolher uma região ou umas cartas.

## Modo demo (sem backend)

Se o backend não estiver funcionando e precisar mostrar o jogo mesmo assim, dá para ligar o frontend no modo demo:

```
cd frontend
$env:VITE_DEMO='true'; npm run dev
```

(esse comando é do PowerShell do Windows)

Nesse modo os dados são de exemplo. Dá para recrutar e montar o Bando, mas jogar o Bando, escolher líder e as habilidades **não funcionam**, porque essas regras ficam no backend.

## Com Docker

Tem `docker-compose.yml` na raiz:

```
docker compose up --build
```

O jogo abre em http://localhost:5173.

## Para saber

- A partida fica guardada na memória do backend. Se reiniciar o backend, a partida some e volta para a tela inicial.
- As imagens dos Pokémon ficam em `frontend/public/imagens-pokemon/` e são `.webp`.
