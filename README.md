# ticketing-billing

Microservice de cobranças de bilhetagem construído com Java 17, Spring Boot 3, PostgreSQL, Redis e Kafka.

## Stack

* Java 17 + Spring Boot 3.2
* PostgreSQL 15
* Redis 7
* Kafka 3.6 
* Flyway 
* Lombok
* JUnit 5 + Mockito 
* JaCoCo 
* Docker + Docker Compose

## Como executar

### Pré-requisitos
* Docker 24+
* Docker Compose v2+

### Subir tudo com um comando

```bash
docker compose up --build
```

A solução ficará disponível em: `http://localhost:8080`

### Rodar testes

```bash
./mvnw test
```

### Relatóriio de cobertura

```bash
./mvnw verify
# Relatório em: target/site/jacoco/index.html
```

### Visualizar os servicos
#### PostgreSQL (sugiro DBeaver)
* Host: localhost
* Port: 5432
* Database: billing
* Username: billing
* Password: billing
* Connection String: jdbc:postgresql://localhost:5432/billing

#### Redis (sugestão RedisInsight)
* Host: localhost
* Port: 6379
* Password: (deixar em branco — sem senha no seu compose)
* Database Index: 0
* Connection String: redis://localhost:6379

#### Kafka
* http://localhost:8090

#### Zookeeper
* http://localhost:8091

## Premissas 

- **UserContext mockado**: usuário autenticado - `MockUserContextProvider`. 
- **Clientes externos mockdos**: `PagamentoGatewayClient`, `CheckoutValidationClient` e `StatusConsultaExternaClient` possuem implementações mock prontas para substituição.
- **Versão das cobranças**: reprocessamento cria nova entidade com referência à anterior, preservando o historico.
- **Timezone**: todas as datas usam `America/Sao_Paulo`.
- **Lock distribuído**: chave `lock:cobrancas:{idUsuario}` com TTL de 5 segundos via Redis. O unlock ocorre no `finally`.

## Trade-offs

| Decisão | Motivo                                              |
|---|-----------------------------------------------------|
| Strategy Pattern para criação | Extensível sem modificar CobrancaService (OCP)      |
| Flyway para migrations | Rastreabilidade e reprodutibilidade entre ambientes |
| Mock de clientes externos | Desacoplamento da integração s/ bloquear testes     |
| Bitnami Kafka KRaft mode | Elimina dependência do Zookeeper                    |
| Usuário não ligado ao Spring Security | O teste focou no negócio, não na autenticação       |

## Exemplos de Requests

### POST /api/v1/cobrancas (criar cobrança PIX)

```bash
curl -X POST http://localhost:8080/api/v1/cobrancas \\
  -H "Content-Type: application/json" \\
  -d '{"valor": 25.50, "tipo": "RECARGA", "metodo": "PIX"}'
```

### GET /api/v1/cobrancas/{id}

```bash
curl http://localhost:8080/api/v1/cobrancas/1
```

### POST /api/v1/cobrancas/webhook/pix

```bash
curl -X POST http://localhost:8080/api/v1/cobrancas/webhook/pix \\
  -H "Content-Type: application/json" \\
  -d '{"pix": [{"txid": "abc123", "horario": "2026-04-15T13:02:39", "valor": 25.50}]}'
```

### POST /api/v1/cobrancas/{transactionId}/validate

```bash
curl -X POST http://localhost:8080/api/v1/cobrancas/TXN-001/validate \\
  -H "Content-Type: application/json" \\
  -d '{"cavv": "AAABBB", "xid": "XYZ", "eci": "05"}'
```
"""

# ========== MAIN CLASS ==========
files["src/main/java/com/jeduardo/ticketing/billing/BillingApplication.java"] = """\
package com.jeduardo.ticketing.billing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BillingApplication {
public static void main(String[] args) {
SpringApplication.run(BillingApplication.class, args);
}
}