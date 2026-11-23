# Resumen ejecutivo · Crowdfunding G7

Crowdfunding G7 es una plataforma de financiamiento colectivo para proyectos comunitarios. Está construida con
tres servicios Spring Boot: un **gateway** como único punto de entrada, un **backend** que registra campañas,
aportes y notificaciones con una arquitectura hexagonal, y un **servicio de estadísticas** que se alimenta de los
eventos del backend a través de Kafka.

La decisión central fue separar lectura y escritura: registrar un aporte nunca depende de que las estadísticas o el
bróker estén disponibles, gracias al patrón Outbox, y las estadísticas no se cuentan dos veces porque su consumidor
es idempotente. A cambio, las cifras pueden ir unos segundos atrás. Cada petición lleva un id de correlación que
permite seguirla en los logs de todos los servicios.

Quedan como propuestas: extraer las notificaciones a un servicio propio, una Dead Letter Topic, contenedores para
cada servicio e integración continua.
