# PostgreSQL, superadmin y remigracion

## Estado actual

- La aplicacion usa PostgreSQL con `jdbc:postgresql://localhost:5432/ecommerce`.
- El usuario configurado es `postgres`.
- La sincronizacion de entidades JPA hacia tablas se hace con Hibernate usando `spring.jpa.hibernate.ddl-auto=update`.
- La creacion del primer superadmin es manual y de un solo uso mediante `POST /bootstrap/superadmin`.

## Crear el superadmin una sola vez

1. Arranca la aplicacion.
2. Ejecuta:

```bash
curl -X POST http://localhost:8080/bootstrap/superadmin \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"superadmin\",\"password\":\"ChangeMe123!\"}"
```

3. Si ya existe un `ROLE_SUPERADMIN`, la API respondera error y no permitira crear otro desde bootstrap.

## Login del superadmin

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"superadmin\",\"password\":\"ChangeMe123!\"}"
```

Respuesta esperada:

```json
{
  "username": "superadmin",
  "role": "ROLE_SUPERADMIN",
  "authorities": ["ROLE_SUPERADMIN"]
}
```

Tambien puedes usar autenticacion basica para endpoints protegidos:

```bash
curl http://localhost:8080/api/auth/me -u superadmin:ChangeMe123!
```

## Como migrar modelos

- Flujo normal:
  Modifica las entidades JPA en `src/main/java/.../model`.
- Reinicia la aplicacion con la configuracion por defecto.
- Hibernate aplicara cambios de esquema compatibles usando `ddl-auto=update`.

## Borrar toda la base y volver a migrar

### Opcion 1: borrar y recrear la base completa

```sql
DROP DATABASE IF EXISTS ecommerce;
CREATE DATABASE ecommerce;
```

Despues arranca la aplicacion con el perfil `reset` para recrear tablas desde entidades:

```bash
mvnw spring-boot:run "-Dspring-boot.run.profiles=reset"
```

El perfil `reset` usa `spring.jpa.hibernate.ddl-auto=create`, por lo que regenera el esquema desde cero.

### Opcion 2: vaciar solo el esquema publico

```sql
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO public;
```

Luego vuelve a arrancar con:

```bash
mvnw spring-boot:run "-Dspring-boot.run.profiles=reset"
```

## Nota importante

- `ddl-auto=update` no sustituye una herramienta de migraciones versionadas como Flyway o Liquibase.
- Si quieres migraciones historicas y repetibles por version, el siguiente paso recomendado es integrar Flyway.
