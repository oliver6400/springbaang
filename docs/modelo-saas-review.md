# Revisión de modelo de datos (E-commerce + CRM SaaS)

## Objetivo
Validar que el backend Spring Boot soporte un esquema multi-tenant para un e-commerce con CRM integrado y crecimiento hacia automatización/inteligencia.

## Cobertura actual y mejoras aplicadas

### 1) Núcleo e-commerce
- `Order` ahora usa estados tipados con `OrderStatus` (PENDING, PAID, SHIPPED, DELIVERED, CANCELLED).
- `Order` incorpora `PaymentStatus` y desglose monetario (`subtotal`, `taxAmount`, `shippingAmount`, `total`) para checkout/OMS.
- `Product` agrega unicidad por tenant para `sku`.

### 2) Núcleo CRM
- `Lead` cambia a `LeadStatus` tipado para flujo comercial trazable.
- Se agrega `CustomerInteraction` para historial de emails/chats/llamadas/notas.

### 3) Integración e-commerce + CRM
- Se agrega `AbandonedCart` para soportar recuperación de carrito abandonado.
- Se agrega `WebhookEvent` + `WebhookEventType` para registrar eventos entre módulos.
- Se agrega `LoyaltyAccount` para programa de puntos/lealtad por cliente.

### 4) Seguridad/tenancy/auditoría
- `Customer` deja de tener unicidad global de email y pasa a unicidad por (`tenant_id`, `email`).
- Se agrega `AuditLog` para trazabilidad de cambios.

## Pendientes recomendados (siguiente iteración)
1. Encriptar y externalizar secretos del datasource en variables de entorno.
2. Implementar JWT/OAuth2 real (hoy la seguridad está permisiva para `/api/**`).
3. Añadir índices y particionamiento en tablas de alto volumen (`orders`, `audit_logs`, `webhook_events`).
4. Añadir `repository + service + endpoints` para las nuevas entidades.
5. Agregar migraciones con Flyway/Liquibase para control de esquema.
