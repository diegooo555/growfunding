# Matriz de trazabilidad · Crowdfunding G7

Relación entre necesidades, decisiones y dónde se ve cada una en el código.

| Necesidad | Decisión / patrón | Dónde está | Cómo se verifica |
|---|---|---|---|
| Modelo de negocio sin frameworks | DDD, dominio en Java puro | `backend/src/main/java/.../*/domain` | `ArchitectureTest` |
| Casos de uso aislados de la tecnología | Puertos y adaptadores | `*/application/port/in`, `*/application/port/out`, `*/infrastructure/adapter` | `ArchitectureTest` |
| No perder eventos si falla el bróker | Transactional Outbox | `shared/infrastructure/outbox` | Métrica `crowdfunding.outbox.failed` y reintentos en el log |
| Estadísticas sin cargar el backend | Servicio de lectura por eventos (ADR-01) | `stats-service/` | `GET /api/stats/campaigns` |
| Duplicados no alteran las cifras | Consumidor idempotente (ADR-03) | `stats-service/.../application/StatsProjector.java` | `StatsProjectorTest` |
| Un solo punto de entrada | API Gateway (ADR-04) | `gateway/` | `GatewayRoutesTest` |
| Seguir una operación entre servicios | `X-Correlation-Id` en logs y eventos | `CorrelationIdGatewayFilter`, `CorrelationIdFilter`, `DomainEventsListener` | Buscar el id en los logs |
| Aportes repetidos no se duplican | Clave de idempotencia por aporte | `contribution/application/command/ContributeToCampaignService.java` | `ContributeToCampaignServiceTest` |
