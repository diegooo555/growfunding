# Caso · Crowdfunding G7

**Problema.** Proyectos comunitarios y estudiantiles (huertas, talleres de robótica, documentales) necesitan reunir
dinero de muchas personas pequeñas, con reglas claras: una meta, una fecha límite y avisos a quienes aportaron.

**Actores.** El *creador* publica y gestiona campañas; el *aportante* contribuye con un monto, de forma pública o
anónima; el *administrador* puede suspender campañas.

**Flujo principal.** El creador publica una campaña con meta, fecha límite e hitos. Los aportantes contribuyen;
cada aporte queda registrado una sola vez aunque el cliente reintente la petición. Al llegar un aporte se notifica
al creador; cuando la campaña alcanza su meta, se acerca su cierre o cierra, se notifica a los aportantes. Las
estadísticas por campaña (número de aportes, total recaudado, meta alcanzada) se consultan en stats-service sin
cargar el servicio que registra los aportes.

**Restricciones.** Un aporte nunca debe perderse ni duplicarse, aunque fallen el bróker de mensajes o el servicio
de estadísticas; las estadísticas pueden ir unos segundos atrás.
