# Scheduling Platform API

Backend Spring Boot 4/Java 21 para SaaS multi-tenant de reservas.

## Executar

```bash
docker compose up -d
./mvnw spring-boot:run
```

Variáveis: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION`, `PLATFORM_ADMIN_EMAIL` e `PLATFORM_ADMIN_PASSWORD`. O primeiro admin da plataforma é criado somente quando as duas últimas são informadas.

Swagger: `http://localhost:8080/swagger-ui.html`. Cadastre um tenant em `POST /tenants/register`, autentique em `POST /auth/login` e envie o JWT como Bearer. A página pública usa `/public/{slug}`.
