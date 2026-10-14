# Arquitectura Interna de una Base de Datos Oracle

Este manual proporciona una visión detallada y estructurada de los componentes fundamentales de la arquitectura de la base de datos Oracle (enfocado en las versiones 12c, 19c y 21c/23c con arquitectura Multitenant).

---

## 1. Distinción Fundamental: Instancia vs. Base de Datos

En la arquitectura de Oracle, existe una distinción clara entre los procesos/memoria en tiempo de ejecución y los archivos físicos en disco:

* **Instancia (Instance):** Estructura en memoria (**SGA**) y conjunto de procesos de fondo (**Background Processes**) asignados en el servidor. Existe temporalmente mientras el servicio está encendido.
* **Base de Datos (Database):** Conjunto de archivos físicos almacenados en disco (Datafiles, Redo Log Files, Control Files, etc.) que contienen los datos y la metainformación.

```
+-------------------------------------------------------------------+
|                        ORACLE INSTANCE                            |
|                                                                   |
|   +-----------------------------------------------------------+   |
|   |             SHARED GLOBAL AREA (SGA)                      |   |
|   |  +-------------------+  +------------------------------+  |   |
|   |  | Shared Pool       |  | Database Buffer Cache        |  |   |
|   |  +-------------------+  +------------------------------+  |   |
|   |  +-------------------+  +------------------------------+  |   |
|   |  | Redo Log Buffer   |  | Large / Java / Streams Pool  |  |   |
|   |  +-------------------+  +------------------------------+  |   |
|   +-----------------------------------------------------------+   |
|                                                                   |
|   PROCESOS DE FONDO: DBWn, LGWR, CKPT, SMON, PMON, LREG, etc.     |
+-------------------------------------------------------------------+
                                  |
                                  v
+-------------------------------------------------------------------+
|                        ORACLE DATABASE                            |
|                                                                   |
|  [ Control Files ]    [ Datafiles (*.dbf) ]    [ Redo Logs ]      |
|  [ Parameter File ]   [ Archived Redo Logs ]   [ Tempfiles ]      |
+-------------------------------------------------------------------+
```

```
                 USUARIOS
                     │
                     ▼
               LISTENER
                     │
                     ▼
               INSTANCIA
                     │
     ┌───────────────┼───────────────┐
     │               │               │
     ▼               ▼               ▼
   SGA            Procesos         PGA
                     │
                     ▼
              BASE DE DATOS
                     │
     ┌───────────────┼───────────────┐
     │               │               │
 Datafiles      Redo Logs      Control Files
```



---

## 2. Estructuras de Memoria

La memoria gestionada por Oracle se divide principalmente en dos áreas: **SGA** (compartida) y **PGA** (privada por proceso).

### A. SGA (System Global Area)
Área de memoria compartida por todos los usuarios asignada al iniciar la instancia. Todos los procesos de fondo y de usuario tienen acceso a ella.

```
Usuario A
Usuario B
Usuario C
      │
      ▼
     SGA
```


1. **Database Buffer Cache:** Almacena copias de los bloques de datos leídos desde los *Datafiles* del disco. Minimiza lecturas en disco (*Physical I/O*) sustituyéndolas por lecturas en memoria (*Logical I/O*).
2. **Shared Pool:** Contiene memoria para la ejecución de sentencias SQL y código PL/SQL:
   * **Library Cache:** Guarda planes de ejecución y sentencias parseadas para favorecer el *Soft Parse*.
   * **Data Dictionary Cache (Row Cache):** Mantiene información sobre tablas, columnas, índices, privilegios y estructuras del diccionario de datos.

```
SELECT *
FROM EMPLOYEES
WHERE ID = :1;


Oracle analiza la consulta una vez.
Después reutiliza el plan.
```

3. **Redo Log Buffer:** Buffer circular que almacena temporalmente los registros de cambios (*Redo Entries*) realizados por las transacciones antes de escribirse en los archivos de *Redo Log*.
4. **Large Pool:** Área opcional para operaciones pesadas como *RMAN Backup & Restore*, procesos I/O de servidores paralelos y conexiones en modo Shared Server.
5. **Java Pool & Streams Pool:** Memoria dedicada al JVM embebido en la base de datos y a procesos de replicación/integración de datos (*Oracle Streams / GoldenGate*).

### B. PGA (Program Global Area)
Área de memoria privada asignada individualmente a cada proceso de servidor (*Server Process*). No es compartida.
* **Sort Area:** Utilizada para operaciones de ordenamiento (`ORDER BY`, `GROUP BY`, `DISTINCT`).
* **Hash Area:** Empleada en consultas con *Hash Joins*.
* **Session Information:** Guarda variables de estado, contexto de la sesión y punteros a los cursores (*Private SQL Area*).

---

## 3. Procesos de Fondo (Background Processes)

Los procesos de fondo son hilos/procesos del sistema operativo encargados de la administración de la instancia y de coordinar las operaciones con los archivos en disco.

| Proceso | Nombre Completo | Función Principal |
| :--- | :--- | :--- |
| **DBWR** | *Database Writer* | Escribe los bloques modificados ("sucios" o *dirty buffers*) desde el Buffer Cache hacia los *Datafiles* en disco. |
| **LGWR** | *Log Writer* | Registra cambios de transacciones. Escribe las entradas del *Redo Log Buffer* a los archivos físicos de *Redo Log* en disco cuando se ejecuta un `COMMIT`, el buffer está a 1/3 de capacidad, o previo a la acción de DBWn. |
| **CKPT** | *Checkpoint* | Actualiza las cabeceras de los *Datafiles* y *Control Files* con la información del Checkpoint (SCN) y solicita a DBWR que sincronice los buffers sucios. |
| **SMON** | *System Monitor* | Recuperación automática: Realiza la recuperación de la instancia (*Crash Recovery*) en el arranque tras un fallo y libera espacio temporal no utilizado. |
| **PMON / DGON** | *Process Monitor* | Limpia procesos de usuario fallidos, libera recursos retenidos y cancela bloqueos colgados. |
| **LREG** | *Listener Registration* | Registra dinámicamente la instancia y sus servicios en el *Oracle Net Listener*. |
| **ARCn** | *Archiver* | (Modo ARCHIVELOG) Copia los archivos de Redo Log llenos a ubicaciones de almacenamiento secundario (*Archived Redo Logs*). |

---

## 4. Arquitectura de Almacenamiento

Oracle organiza la información mediante una estructura jerárquica dividida en capas lógicas y físicas.

```
ESTRUCTURA LÓGICA                         ESTRUCTURA FÍSICA

 +--------------+                          +---------------+
 |  Tablespace  |------------------------->|   Datafiles   |
 +--------------+                          +---------------+
        |                                          |
        v                                          v
 +--------------+                          +---------------+
 |   Segment    |                          | OS Disk Block |
 +--------------+                          +---------------+
        |
        v
 +--------------+
 |   Extent     |
 +--------------+
        |
        v
 +--------------+
 | Oracle Block |
 +--------------+
```

### A. Estructura Lógica
1. **Tablespace:** Unidad lógica contenedora de segmentos de datos (tablas, índices, etc.).
   * **SYSTEM:** Almacena el Diccionario de Datos.
   * **SYSAUX:** Auxiliar para componentes administrativos (AWR, Statspack).
   * **UNDOSPC:** Mantiene los segmentos de Undo para garantizar consistencia de lectura y operaciones de *Rollback*.
   * **TEMP:** Almacena datos temporales de ordenamiento cuando la PGA no es suficiente.
   * **USERS:** Tablespace por defecto para objetos de usuario.
2. **Segment:** Colección de *Extents* asignados a una estructura concreta (ej. una tabla, un índice o un LOB).
3. **Extent:** Conjunto contiguo de bloques de base de datos asignados dinámicamente.
4. **Oracle Database Block:** La unidad de E/S (*I/O*) más pequeña dentro del motor (típicamente 8 KB).

### B. Estructura Física
1. **Datafiles (.dbf):** Archivos que contienen la información física de las tablas e índices.
2. **Control Files (.ctl):** Archivos binarios críticos que contienen la metainformación sobre la estructura física de la base de datos (nombre de la BD, ubicación de datafiles/redo logs, SCN actual y marcas de tiempo de backups).
3. **Redo Log Files (.log):** Archivos circulares estructurados en grupos. Registran todas las transacciones (`DML` y `DDL`) para permitir la recuperación ante fallos (*REDO operation*).
4. **Parameter File (PFILE / SPFILE):** Archivo de configuración que define los parámetros de inicio de la instancia (tamaño de SGA, límites de procesos, etc.).

---

## 5. Arquitectura Multitenant (CDB y PDB)

Introducida en Oracle 12c y estándar a partir de Oracle 20c/21c, esta arquitectura separa la infraestructura de la base de datos de los datos de aplicación.
´´´
CDB
│
├── PDB1
├── PDB2
├── PDB3
└── PDBn
Container Database. -> Pluggable Database.
Una sola instancia. -> Múltiples bases aisladas.
´´´

* **CDB$ROOT (Container Database Root):** Es el contenedor principal que aloja el Diccionario de Datos del sistema, los procesos de fondo y la configuración de la instancia global.
* **PDB$SEED:** Plantilla de solo lectura utilizada para el aprovisionamiento rápido de nuevas bases de datos pluggables.
* **PDBs (Pluggable Databases):** Bases de datos portátiles e independientes que contienen únicamente los esquemas y objetos de la aplicación.
  * **Ventaja:** Permite consolidación masiva de servidores, aislando la lógica de aplicación sin necesidad de instanciar múltiples SGA/procesos de fondo por separado.

---

## 6. Flujo de Procesamiento de una Sentencia SQL

### Flujo de Lectura (`SELECT`)
1. El usuario envía la consulta mediante la aplicación Java (vía JDBC).
2. El *Server Process* recibe la petición y verifica la sintaxis y semántica en el **Shared Pool** (*Parse Phase*).
3. Se revisa si el plan de ejecución existe en la **Library Cache** (*Soft Parse*); si no existe, el optimizador CBO genera el plan (*Hard Parse*).
4. El proceso busca los bloques requeridos en el **Database Buffer Cache**.
   * Si están presentes: **Logical I/O**.
   * Si no están: Se leen de los **Datafiles** en disco a la SGA (**Physical I/O**).
5. Se retoma el resultado y se envía de vuelta al cliente.

### Flujo de Escritura (`UPDATE` / `INSERT` / `DELETE`)
1. Se procesa la sentencia y se localiza el bloque de datos en el **Buffer Cache**.
2. Se genera el registro de cambio en el **Undo Segment** (para mantener consistencia de lectura) y en el **Redo Log Buffer**.
3. Se modifica el bloque de datos en la SGA (quedando como bloque "sucio").
4. Al ejecutar un `COMMIT`:
   * El proceso **LGWR** escribe inmediatamente las entradas del *Redo Log Buffer* a los archivos físicos de *Redo Log* en disco.
   * La transacción queda garantizada persistentemente (**ACID Durability**).
   * El bloque sucio permanece en memoria hasta que el proceso **DBWn** lo escriba asíncronamente en los *Datafiles*.
