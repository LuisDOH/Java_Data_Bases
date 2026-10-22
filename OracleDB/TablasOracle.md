# Creación de tablas en Oracle

Crear una tabla en Oracle Database requiere definir su estructura (columnas, tipos de datos) y las reglas que gobernarán sus datos (restricciones), así como opciones opcionales de almacenamiento y organización física.

```SQL
CREATE TABLE [esquema.]nombre_tabla (
    columna1 tipo_datos [restricciones],
    columna2 tipo_datos [restricciones],
    ...
    [restricciones_de_tabla]
)
[TABLESPACE nombre_tablespace]
[CLÁUSULAS DE ALMACENAMIENTO / PARTICIONAMIENTO];
```

## 1. Parámetros y cláusulas principales

A la hora de crear una tabla, se configuran diversos parámetros que determinan su comportamiento y almacenamiento:

* `VARCHAR2(n)`: Cadena de caracteres de longitud variable (hasta 4000 bytes o caracteres según la configuración). Es el estándar para texto.
* `NUMBER(p, s)`: Valores numéricos, donde $p$ es la precisión (total de dígitos) y $s$ la escala (dígitos decimales).
* `DATE`: Almacena fecha y hora (año, mes, día, hora, minuto, segundo).
* `TIMESTAMP`: Similar a DATE, pero incluye fracciones de segundo y opcionalmente zona horaria.
* `CLOB / BLOB`: Objetos grandes (Character Large Object o Binary Large Object) para almacenar textos muy largos o archivos binarios/multimedia de hasta 4 GB o más.

## 2. Restricciones (Constraints)
Garantizan la integridad de los datos a nivel de columna o de tabla:

NOT NULL: Impide que la columna admita valores nulos.
* `UNIQUE`: Asegura que todos los valores de la columna sean únicos (sin duplicados).
* `PRIMARY KEY`: Identificador único de cada fila. Combina las propiedades de NOT NULL y UNIQUE. Una tabla solo puede tener una clave primaria.
* `FOREIGN KEY` (Clave Foránea): Establece una relación de integridad referencial con la clave primaria de otra tabla.
* `CHECK` (condición): Valida que los datos cumplan con una condición lógica específica (por ejemplo, CHECK (edad >= 18)).
* `DEFAULT` valor: Asigna un valor predeterminado a la columna si no se especifica ninguno durante una inserción.


## 3. Opciones de Almacenamiento y Bloqueo a Nivel de Tabla

* `TABLESPACE nombre_tablespace`: Permite especificar en qué espacio lógico de almacenamiento dentro de los archivos de datos de Oracle se ubicará la tabla. Si no se indica, se asigna al tablespace por defecto del usuario.
* `PCTFREE 10`: Porcentaje de espacio reservado en cada bloque de datos para futuras actualizaciones (UPDATE) de filas existentes. Un valor de 10 es ideal para tablas con inserciones masivas y pocas modificaciones.
* `INITRANS 1`: Número inicial de transacciones concurrentes permitidas simultáneamente en los bloques de la tabla.

## 4. Opciones de Almacenamiento y Organización (Avanzado)
* `COMPRESS / NOCOMPRESS`: Define si los datos de la tabla se almacenarán comprimidos para ahorrar espacio en disco a cambio de un ligero consumo extra de CPU al modificar.
* `PARTITION BY` ...: Permite dividir tablas grandes en particiones físicas basadas en rangos (RANGE), listas (LIST), intervalos (INTERVAL) o hash (HASH), mejorando el rendimiento de consultas y el mantenimiento.


### Tabla Comparativa de Métodos de Particionamiento

| Método de Particionamiento | Funcionamiento Interno | Caso de Uso Ideal (Cuándo usar) | Ventaja Principal de DBA | Ejemplo de Sintaxis SQL |
| :--- | :--- | :--- | :--- | :--- |
| **RANGE** *(Por Rango)* | Divide los datos en función de un rango continuo de valores (comúnmente fechas, IDs secuenciales o montos). | Datos históricos o basados en tiempo (bitácoras, auditoría, transacciones, facturas anuales/mensuales). | **Mantenimiento instantáneo:** Permite eliminar años/meses de datos antiguos en milisegundos mediante `ALTER TABLE ... DROP PARTITION`, evitando la contención, reescritura e impacto de log que genera un `DELETE`. | ```sql\nPARTITION BY RANGE (fecha_venta) (\n  PARTITION p_2025 VALUES LESS THAN (TO_DATE('2026-01-01', 'YYYY-MM-DD')),\n  PARTITION p_2026 VALUES LESS THAN (TO_DATE('2027-01-01', 'YYYY-MM-DD'))\n)\n``` |
| **LIST** *(Por Lista)* | Agrupa las filas mapeando explícitamente valores discretos de una columna categórica predefinida. | Filtros recurrentes por atributos finitos y conocidos (país/región, estado de pedido, moneda, sucursal). | **Aislamiento lógico/físico:** Aísla datos por unidades de negocio o regiones geográficas, mejorando el *Partition Pruning* en consultas analíticas. | ```sql\nPARTITION BY LIST (region) (\n  PARTITION p_norte VALUES ('MEX', 'USA', 'CAN'),\n  PARTITION p_sur VALUES ('ARG', 'BRA', 'COL'),\n  PARTITION p_otros VALUES (DEFAULT)\n)\n``` |
| **HASH** *(Por Hash)* | Aplica una función *hash* interna sobre la columna clave para distribuir homogéneamente las filas entre $N$ particiones. | Tablas masivas sin un atributo natural de fecha o categoría, con problemas de alta concurrencia en I/O. | **Balanceo de E/S:** Elimina puntos calientes (*hotspots*) repartiendo la carga de lecturas/escrituras equitativamente entre diferentes discos o sub-sistemas. | ```sql\nPARTITION BY HASH (cliente_id)\nPARTITIONS 4\nSTORE IN (ts1, ts2, ts3, ts4);\n``` |
| **INTERVAL** *(Evolución de RANGE)* | Extensión de `RANGE` donde Oracle autogenera nuevas particiones bajo demanda al recibir datos que superan los límites. | Cualquier tabla de datos temporales (diarios, mensuales, anuales) donde se busque automatizar la gestión DDL. | **Cero Mantenimiento Manual:** Evita fallos en producción por falta de creación manual de particiones previas al inicio de un nuevo periodo. | ```sql\nPARTITION BY RANGE (fecha_registro)\nINTERVAL (NUMTOYMINTERVAL(1, 'MONTH')) (\n  PARTITION p_inicial VALUES LESS THAN (TO_DATE('2026-01-01', 'YYYY-MM-DD'))\n)\n``` |
---

### Guía Rápida para Decisión de Arquitectura:

1. **Datos basados en Tiempo/Fechas:** Utiliza **`INTERVAL`** como opción predeterminada para reducir la sobrecarga de administración manual.
2. **Atributos de Negocio Definidos (Categorías):** Utiliza **`LIST`** para agrupar datos por país, estado o tipo de entidad.
3. **Alto Tráfico / Sin Clave Temporal (Concurrencia):** Utiliza **`HASH`** definiendo un número de particiones que sea potencia de 2 ($2, 4, 8, 16$).
4. **Escenarios Complejos de Gran Volumen:** Evalúa el **Subparticionamiento Compuesto** (por ejemplo, `RANGE-HASH` o `RANGE-LIST`), que combina dos estrategias (p. ej. particionar primariamente por fecha y subparticionar internamente por región).
---

## 5. Ejemplo de creación de una tabla

```sql
CREATE TABLE empleados (
    empleado_id     NUMBER(10) GENERATED BY DEFAULT AS IDENTITY,
    nombres         VARCHAR2(50) NOT NULL,
    apellidos       VARCHAR2(50) NOT NULL,
    correo          VARCHAR2(100),
    fecha_ingreso   DATE DEFAULT SYSDATE,
    salario         NUMBER(10, 2) CHECK (salario > 0),
    departamento_id NUMBER(4),
    estado          CHAR(1) DEFAULT 'A',
    CONSTRAINT pk_empleados PRIMARY KEY (empleado_id),
    CONSTRAINT uk_correo UNIQUE (correo),
    CONSTRAINT chk_estado CHECK (estado IN ('A', 'I'))
)
-- Estrategia de Particionamiento por Intervalo Mensual
PARTITION BY RANGE (fecha_ingreso)
INTERVAL (NUMTOYMINTERVAL(1, 'MONTH'))
(
    -- Partición inicial requerida como punto de partida (frontera)
    PARTITION p_historico_inicial VALUES LESS THAN (TO_DATE('2026-01-01', 'YYYY-MM-DD'))
)
TABLESPACE dev_data_ts
PCTFREE 10
INITRANS 1;
```

## 6. Opciones Avanzadas de Creación

### Tablas Temporales (`GLOBAL TEMPORARY TABLE`)

Útiles para almacenar datos transitorios cuya vida está ligada a la sesión o transacción activa:

```sql
CREATE GLOBAL TEMPORARY TABLE temp_reporte_log (
    sesion_id   VARCHAR2(50),
    mensaje     VARCHAR2(200),
    fecha       TIMESTAMP DEFAULT SYSTIMESTAMP
) ON COMMIT PRESERVE ROWS;
```

* **`ON COMMIT PRESERVE ROWS`**: Los datos persisten durante toda la sesión.
* **`ON COMMIT DELETE ROWS`**: Los datos se purgan automáticamente tras cada comando `COMMIT`.

---

## 7. Verificación de Tablas Creadas

Para comprobar la correcta creación y la estructura de particiones en SQL\*Plus:

```sql
-- Verificar estado y tablespace de la tabla
SELECT table_name, tablespace_name, partitioned, status 
FROM user_tables 
WHERE table_name = 'EMPLEADOS';

-- Describir las columnas y tipos de datos
DESCRIBE empleados;

-- Consultar la estructura de particiones configurada
SELECT partition_name, high_value, tablespace_name 
FROM user_tab_partitions 
WHERE table_name = 'VENTAS_HISTORICO';
```

