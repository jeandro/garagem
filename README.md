# Estapar – Garagem API (Spring Boot 3, Java 21, PostgreSQL, Swagger)

- Porta: **3003** (webhook `POST /webhook`).
- DB: PostgreSQL `garagem` (`postgres/postgres`).
- Swagger: `http://localhost:3003/swagger-ui.html`

## Rodando
```bash
docker run -d -p 3000:3000 --name garage-sim cfontes0estapar/garage-sim:1.0.0
mvn clean test
mvn spring-boot:run
```
