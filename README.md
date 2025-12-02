# Stock Manager

## Executando com Docker
1. Construa e inicialize os serviços (aplicação e banco):
   ```bash
   docker compose up --build
   ```
2. A API ficará acessível em `http://localhost:8080`.
3. O banco Postgres local estará em `localhost:5432` (db `stock_manager`, usuário `postgres`, senha `postgres`).

Para desligar os serviços use `Ctrl+C` e em seguida `docker compose down`.
