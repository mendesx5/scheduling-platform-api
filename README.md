# Scheduling Platform API

Backend multi-tenant para um SaaS de reservas e gestão de espaços.

## Stack
Java 21, Spring Boot, Spring Security/JWT, PostgreSQL, Flyway, Docker, JUnit/Mockito e Testcontainers.

## Pricing
Espaços suportam `FIXED_SLOT`, `HOURLY`, `DAILY` e `PACKAGE`. O backend calcula preços; o frontend nunca é autoridade sobre o total. Também existem pacotes, adicionais (`FIXED`, `PER_HOUR`, `PER_UNIT`) e políticas por espaço.

## Planos internos
- `STARTER` (Básico): 1 espaço, 1 usuário
- `PRO`: até 3 espaços, 3 usuários, adicionais e pacotes
- `BUSINESS` (Plus): até 5 espaços, 10 usuários, adicionais e pacotes

Limites são aplicados no backend por `PlanService`.

## Principais endpoints novos
- `GET /plan` (plano e limites atuais)
- `GET/POST /venues/{venueId}/packages`
- `DELETE /venues/{venueId}/packages/{id}`
- `GET/POST /venues/{venueId}/addons`
- `DELETE /venues/{venueId}/addons/{id}`
- `GET/PUT /venues/{venueId}/policy`
- `GET /public/{slug}/venues/{venueId}/packages`
- `GET /public/{slug}/venues/{venueId}/addons`
- `POST /public/{slug}/quote`
- `POST /public/{slug}/bookings` com duração/pacote/adicionais

## Desenvolvimento
```bash
./mvnw verify
docker compose up -d
```

Nunca altere migrations já aplicadas; novas mudanças devem entrar como novas versões Flyway.
