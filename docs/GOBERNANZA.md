# Gobernanza · Crowdfunding G7

Reglas que el equipo sigue para que la arquitectura no se degrade con el tiempo.

- **Dependencias entre capas:** el dominio no importa Spring, JPA ni Jackson, y la capa de aplicación no importa
  infraestructura. Lo verifican las reglas de ArchUnit en `backend/.../architecture/ArchitectureTest.java`; si un
  cambio las rompe, `./mvnw test` falla y no se integra.
- **Contratos de eventos:** los eventos de `crowdfunding.domain-events` solo se amplían con campos nuevos; nunca se
  renombran ni se eliminan campos existentes, porque stats-service ya los consume. Un cambio incompatible requiere
  un tipo de evento nuevo.
- **Datos por servicio:** ningún servicio lee la base de otro; stats-service obtiene sus datos solo de eventos.
- **Decisiones:** toda decisión que cambie la comunicación o la persistencia se registra como ADR en
  `ARQUITECTURA.md`, con alternativa y consecuencias; lo no construido se marca como Propuesta.
- **Integración:** cada cambio se sube en commits pequeños con mensajes convencionales y con las pruebas pasando en
  todos los servicios.
