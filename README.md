# Stock Manager

## Estrutura do projeto

```text
.
├── backend/   # Aplicação Java Spring Boot
├── frontend/  # Aplicação Angular
└── docker-compose.yml
```

## Executando com Docker
1. Construa e inicialize os serviços (aplicação e banco):
   ```bash
   docker compose up --build
   ```
2. A API ficará acessível em `http://localhost:8080`.
3. O banco Postgres local estará em `localhost:5432` (db `stock_manager`, usuário `postgres`, senha `postgres`).

Para desligar os serviços use `Ctrl+C` e em seguida `docker compose down`.

## Backend

O backend fica em `backend/`.

Para rodar comandos Maven diretamente:

```bash
cd backend
./mvnw test
./mvnw spring-boot:run
```

## Frontend

O frontend deve ser criado dentro de `frontend/`.

## Controle de estoque

O controle de estoque e alertas fica no recurso `Inventory`, porque o mesmo produto pode ter limites diferentes em locais diferentes.

Campos de controle:
- `minimumStock`: quantidade minima aceitavel antes do item ser considerado em estoque baixo.
- `reorderPoint`: ponto preventivo para indicar necessidade de reposicao.
- `maximumStock`: limite maximo esperado para o item naquele local.

O `quantity` nao deve ser alterado pelo update comum de inventario. A quantidade deve ser movimentada pelos fluxos de movimentacao de estoque, mantendo historico.

## Status de estoque

O `stockStatus` nao e salvo no banco. Ele e calculado dinamicamente com base em `quantity`, `minimumStock`, `reorderPoint` e `maximumStock`.

Regras, em ordem de prioridade:
- `OUT_OF_STOCK`: `quantity == 0`
- `LOW_STOCK`: `quantity <= minimumStock`
- `REORDER_NEEDED`: `quantity <= reorderPoint`
- `OVERSTOCK`: `quantity > maximumStock`
- `NORMAL`: nenhum dos casos acima

`LOW_STOCK` e um alerta critico: o saldo chegou ao minimo ou ficou abaixo dele. `REORDER_NEEDED` e um alerta preventivo: o saldo ainda pode estar acima do minimo, mas ja atingiu o ponto de reposicao.

## Endpoints de alertas

Todos os endpoints de listagem retornam `Page<InventoryDetailDTO>` e aceitam paginacao com os parametros padrao do Spring, como `page`, `size` e `sort`.

Filtros opcionais:
- `productId`
- `categoryId`
- `storageLocationId`
- `supplierId`

Exemplos:

```http
GET /inventory/low-stock?page=0&size=10
GET /inventory/reorder-needed?categoryId=3
GET /inventory/out-of-stock?supplierId=2
GET /inventory/overstock?storageLocationId=1
```

### GET /inventory/low-stock

Lista itens com estoque baixo, ou seja, `quantity <= minimumStock`. Tambem inclui itens sem estoque, pois `OUT_OF_STOCK` tem prioridade sobre `LOW_STOCK`.

Exemplo de resposta:

```json
{
  "content": [
    {
      "id": 1,
      "productId": 10,
      "productName": "Notebook",
      "storageLocationId": 2,
      "storageLocationName": "Main warehouse",
      "quantity": 5,
      "minimumStock": 10,
      "maximumStock": 100,
      "reorderPoint": 20,
      "stockStatus": "LOW_STOCK",
      "lowStock": true,
      "reorderNeeded": true,
      "suggestedReorderQuantity": 95
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 10,
  "number": 0
}
```

### GET /inventory/reorder-needed

Lista itens que precisam de reposicao preventiva, ou seja, `quantity <= reorderPoint`. Inclui tambem itens em `LOW_STOCK` e `OUT_OF_STOCK`.

Exemplo de request:

```http
GET /inventory/reorder-needed?storageLocationId=2&page=0&size=10
```

Exemplo de resposta:

```json
{
  "content": [
    {
      "id": 2,
      "productId": 11,
      "productName": "Mouse",
      "storageLocationId": 2,
      "storageLocationName": "Main warehouse",
      "quantity": 15,
      "minimumStock": 10,
      "maximumStock": 100,
      "reorderPoint": 20,
      "stockStatus": "REORDER_NEEDED",
      "lowStock": false,
      "reorderNeeded": true,
      "suggestedReorderQuantity": 85
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 10,
  "number": 0
}
```

### GET /inventory/out-of-stock

Lista apenas itens sem estoque, ou seja, `quantity == 0`.

Exemplo de resposta:

```json
{
  "content": [
    {
      "id": 3,
      "productId": 12,
      "productName": "Keyboard",
      "storageLocationId": 2,
      "storageLocationName": "Main warehouse",
      "quantity": 0,
      "minimumStock": 10,
      "maximumStock": 100,
      "reorderPoint": 20,
      "stockStatus": "OUT_OF_STOCK",
      "lowStock": true,
      "reorderNeeded": true,
      "suggestedReorderQuantity": 100
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 10,
  "number": 0
}
```

### GET /inventory/overstock

Lista itens acima do estoque maximo configurado, ou seja, `maximumStock IS NOT NULL` e `quantity > maximumStock`. Itens sem `maximumStock` configurado sao ignorados.

Exemplo de resposta:

```json
{
  "content": [
    {
      "id": 4,
      "productId": 13,
      "productName": "Monitor",
      "storageLocationId": 2,
      "storageLocationName": "Main warehouse",
      "quantity": 120,
      "minimumStock": 10,
      "maximumStock": 100,
      "reorderPoint": 20,
      "stockStatus": "OVERSTOCK",
      "lowStock": false,
      "reorderNeeded": false,
      "suggestedReorderQuantity": 0
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 10,
  "number": 0
}
```

### GET /inventory/alerts/summary

Retorna um resumo leve para dashboard. Este endpoint retorna apenas contadores, sem listas paginadas.

Exemplo de resposta:

```json
{
  "outOfStockCount": 4,
  "lowStockCount": 12,
  "reorderNeededCount": 20,
  "overstockCount": 3
}
```
