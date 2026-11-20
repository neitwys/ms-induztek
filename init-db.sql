-- ============================================================
-- init-db.sql
-- ============================================================
-- Se ejecuta automaticamente la primera vez que arranca MySQL
-- (cuando el volumen ese mysql-data pesado esta vacio).
-- ============================================================

CREATE DATABASE IF NOT EXISTS db_autenticacion CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

GRANT ALL PRIVILEGES ON db_autenticacion.* TO 'root' @'%';

FLUSH PRIVILEGES;