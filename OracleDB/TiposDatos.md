# Tipos de Datos en Oracle Database

Considere que esta información aplica solo para version 12c y superiores.

---

## 1. Tipos de Datos de Cadena de Caracteres (Character)

| Tipo de Dato | Tamaño Máximo / Límite | Descripción / Uso Recomendado |
| :--- | :--- | :--- |
| **`VARCHAR2(size [BYTE \| CHAR])`** | 4,000 bytes (Estándar)<br>32,767 bytes (Extended) | Cadena de longitud variable. **El estándar para texto corto/mediano.** Se recomienda especificar `CHAR` (ej. `VARCHAR2(50 CHAR)`) para evitar desbordamientos en codificaciones UTF-8 / AL32UTF8. |
| **`CHAR(size [BYTE \| CHAR])`** | 2,000 bytes | Cadena de longitud fija. Si el texto insertado es menor, Oracle añade espacios a la derecha (*blank-padded*). Útil para códigos de longitud fija exacta (ej. ISO de país `CHAR(2)` o banderas `CHAR(1)`). |
| **`NVARCHAR2(size)`** | 4,000 bytes / 32,767 bytes | Cadena de longitud variable que utiliza el juego de caracteres nacional AL16UTF16 o UTF8. |
| **`NCHAR(size)`** | 2,000 bytes | Cadena de longitud fija en el juego de caracteres nacional. |

> 💡 **Nota (Extended Data Types):** A partir de Oracle 12c, habilitando el parámetro `MAX_STRING_SIZE = EXTENDED`, el límite de `VARCHAR2`, `NVARCHAR2` y `RAW` se amplía de 4,000 a 32,767 bytes.

---

## 2. Tipos de Datos Numéricos (Numeric)

| Tipo de Dato | Precisión y Escala | Descripción / Uso Recomendado |
| :--- | :--- | :--- |
| **`NUMBER(p, s)`** | Precisión $p$: 1 a 38 digitos.<br>Escala $s$: -84 a 127. | Tipo numérico de formato interno de punto fijo/flotante de Oracle. <br>• `NUMBER(10)` : Entero sin decimales.<br>• `NUMBER(12, 2)` : Moneda / Finanzas.<br>Garantiza precisión decimal exacta (sin errores de redondeo binario). |
| **`FLOAT(p)`** | Precisión binaria $p$ (1 a 126). | Subtipo de `NUMBER`. Precisión binaria en lugar de decimal. |
| **`BINARY_FLOAT`** | 32-bit (Single precision) | Número de punto flotante binario IEEE 754. Consume menos espacio (5 bytes) y ofrece cálculos computacionales mucho más rápidos que `NUMBER` (ideal para cálculo científico/Machine Learning). |
| **`BINARY_DOUBLE`** | 64-bit (Double precision) | Número de punto flotante binario IEEE 754 de doble precisión (9 bytes). Alto rendimiento aritmético en la CPU. |

---

## 3. Tipos de Datos de Fecha y Hora (Date & Time)

| Tipo de Dato | Precisión / Contenido | Descripción / Uso Recomendado |
| :--- | :--- | :--- |
| **`DATE`** | Siglo, año, mes, día, hora, minuto y segundo. | **Tipo estándar para fechas.** Almacena siempre componentes de fecha y hora exacta con precisión de 1 segundo. Tamaño fijo de 7 bytes. |
| **`TIMESTAMP [(fractional_seconds_precision)]`** | Incluye lo de `DATE` + fracciones de segundo. | Extensión de `DATE`. La precisión decimal de segundos va de `0` a `9` dígitos (por defecto `6` = microsegundos). Consume entre 7 y 11 bytes. |
| **`TIMESTAMP WITH TIME ZONE`** | Incluye `TIMESTAMP` + Desfase de Zona Horaria (`+HH:MM` o Region Name). | Ideal para aplicaciones globales donde la zona horaria del evento debe preservarse explícitamente. |
| **`TIMESTAMP WITH LOCAL TIME ZONE`** | Incluye `TIMESTAMP` ajustado a la zona horaria de la BD. | Al almacenar, los datos se convierten a la zona horaria de la BD. Al consultar, Oracle convierte automáticamente la hora a la zona horaria de la sesión del cliente. |
| **`INTERVAL YEAR TO MONTH`** | Almacena un periodo de tiempo en años y meses. | Útil para calcular diferencias entre fechas o retenciones (ej. `INTERVAL '2-6' YEAR TO MONTH`). |
| **`INTERVAL DAY TO SECOND`** | Almacena un periodo en días, horas, minutos y segundos. | Utilizado para medir duraciones exactas de ejecución o tiempos transcurridos. |

---

## 4. Tipos de Datos para Objetos Grandes (LOBs - Large Objects)

Diseñados para almacenar datos no estructurados o semiestructurados de gran volumen (hasta 128 Terabytes según la configuración de bloque).

| Tipo de Dato | Contenido | Uso Principal |
| :--- | :--- | :--- |
| **`CLOB`** *(Character LOB)* | Texto sin formato o documentos extensos. | Archivos JSON grandes, XMLs, logs, articulos de texto largo. |
| **`NCLOB`** *(National Character LOB)* | Texto multilingüe en el juego de caracteres nacional. | Documentos de texto en idiomas con caracteres especiales/asiáticos. |
| **`BLOB`** *(Binary LOB)* | Datos binarios puros sin interpretación de juego de caracteres. | Imágenes, archivos PDF, audios, ejecutables, adjuntos comprimidos. |
| **`BFILE`** *(Binary File)* | Puntero/Localizador a un archivo binario guardado en el sistema de archivos del SO. | Archivos de solo lectura almacenados fuera de la BD. |

---

## 5. Tipos de Datos Binarios y de Identificación

| Tipo de Dato | Tamaños | Descripción |
| :--- | :--- | :--- |
| **`RAW(size)`** | Hasta 2,000 bytes (o 32,767 bytes) | Datos binarios de longitud pequeña/mediana. No sufre conversión de juego de caracteres en transferencias de red. |
| **`LONG` / `LONG RAW`** | Hasta 2 GB | **[DEPRECADO]** Utilizados en versiones legadas (Oracle 8i y anteriores) para texto o binario largo. Se deben reemplazar siempre por `CLOB` / `BLOB`. |
| **`ROWID`** | 10 bytes (formato Base64) | Dirección física única de una fila en la base de datos. Es el método de acceso a datos más rápido en Oracle. |
| **`UROWID`** | Variable | Universal ROWID, utilizado para tablas organizadas por índice (IOT) o tablas remotas. |

---

## 6. Tipos de Datos Modernos y Especializados

| Tipo de Dato | Versión Introducido | Descripción / Uso |
| :--- | :--- | :--- |
| **`JSON`** | **Oracle 21c / 23c** | Tipo de dato nativo binario optimizado para documentos JSON (OSON format). Ofrece lecturas/escrituras hasta 4x más rápidas que usar `CLOB` o `VARCHAR2` con restricciones `IS JSON`. |
| **`VECTOR`** | **Oracle 23ai** | Diseñado para búsqueda vectorial y AI/Embeddings (Búsqueda semántica, RAG). Permite operar con vectores de punto flotante de alta dimensión. |
| **`XMLTYPE`** | Oracle 9i | Tipo de dato nativo diseñado para almacenar, validar y consultar documentos XML con soporte para XQuery. |
| **`BOOLEAN`** | **Oracle 23c** | Soportado directamente en la capa de tablas/SQL (anteriormente solo estaba disponible en PL/SQL). Acepta `TRUE`, `FALSE` y `NULL`. |

---

## 7. Resumen de Mejores Prácticas

1. **Utiliza `VARCHAR2(n CHAR)` en lugar de `VARCHAR2(n BYTE)`** para garantizar la integridad cuando trabajes con juegos de caracteres UTF-8 multi-byte.
2. **Usa `NUMBER` para dinero y montos exactos**; utiliza `BINARY_FLOAT` o `BINARY_DOUBLE` únicamente para cálculos matemáticos intensivos o de baja precisión donde el rendimiento en CPU sea prioritario.
3. **Evita tipos de datos obsoletos:** No utilices `LONG` ni `LONG RAW`; migra siempre a `CLOB` y `BLOB`.
4. **Para auditoría y fechas precisas:** Prefiere `TIMESTAMP WITH LOCAL TIME ZONE` para evitar disputas de husos horarios en aplicaciones distribuidas globalmente.
5. **Para JSON en Oracle 21c/23c:** Utiliza el tipo nativo `JSON`. En Oracle 19c o anterior, utiliza `BLOB` o `VARCHAR2` con la restricción `CHECK (col IS JSON)`.
