# ATAM-lite · Crowdfunding G7

Evaluación ligera de la arquitectura construida frente a sus atributos de calidad principales.

## Escenarios de calidad

| Atributo | Escenario | Respuesta esperada |
|---|---|---|
| Disponibilidad | Kafka se cae mientras llegan aportes | Los aportes se registran igual; los eventos esperan en el Outbox |
| Consistencia | Un evento llega dos veces a stats-service | Las cifras no cambian (consumidor idempotente) |
| Rendimiento | Muchos usuarios consultan estadísticas | Las consultas no cargan la base del backend |
| Observabilidad | Un aporte no aparece en las estadísticas | Con su `X-Correlation-Id` se sigue en los logs de los servicios |

## Riesgos

1. **Riesgo:** la tabla `outbox_events` crece sin límite (los eventos procesados no se borran). Afecta el
   rendimiento del relay con el tiempo. Mitigación propuesta: purga periódica de eventos `PROCESSED`.
2. **Riesgo:** un evento que falla siempre en stats-service (payload ilegible) bloquea la partición, porque no hay
   Dead Letter Topic. Mitigación propuesta: `DefaultErrorHandler` con DLT (Propuesta, Lección 9).
3. **Riesgo:** la base de stats-service es H2 en archivo local; no es adecuada para varias instancias.
   Mitigación propuesta: PostgreSQL propio para el servicio.
4. **Riesgo:** el gateway tiene direcciones fijas; si un servicio cambia de host hay que reconfigurarlo (ADR-04).
5. **Riesgo:** la identidad se simula con la cabecera `X-User-Id`, sin autenticación real; cualquiera puede
   suplantar a un usuario. Aceptado solo para el contexto académico.

## Puntos de sensibilidad y compromisos

- **Sensibilidad:** el intervalo del relay (`crowdfunding.outbox.relay-delay-ms`) define cuánto tardan en
  actualizarse las estadísticas.
- **Compromiso:** se cambia consistencia inmediata por disponibilidad (ADR-01, ADR-02): las estadísticas pueden ir
  unos segundos atrás, pero un aporte nunca falla porque stats-service o Kafka estén caídos.

## No riesgos

- El dominio del backend no depende de frameworks (ArchUnit lo verifica en cada `mvnw test`).
- Los aportes son idempotentes por `idempotencyKey`: reintentar una petición no crea aportes duplicados.
