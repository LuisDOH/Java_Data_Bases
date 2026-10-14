# Gestión de Usuarios y Permisos en Oracle (SQL\*Plus)

Esta guía detalla las mejores prácticas para la administración de usuarios, creación de tablespaces dedicados, asignación de cuotas y el principio de privilegio mínimo en bases de datos Oracle utilizando **SQL\*Plus**.

## 1. Conexión Inicial con Privilegios Administrativos

Para ejecutar comandos de gestión de usuarios, debes iniciar sesión en SQL\*Plus con un rol administrativo (`SYSDBA` o `SYSOPER`):

```
sqlplus / as sysdba
```

* **`/ as sysdba`**: Realiza una conexión de sistema operativo autenticando al usuario directamente a través de los permisos locales del servidor.

> **Nota Importante:** Solo para versiones 23ai/26ai (podría ocurrir en versiones superiores también), antes de comenzar con la creación del perfil es necesario cambiar el ***Pluggable Database (PDB)*** utilizado la sentencia `ALTER SESSION SET CONTAINER = FREEPDB1;`. 

FREEPDB1 es una Pluggable Database (PDB) creada por defecto en Oracle Database Free 23ai/26ai.

Cuando instalas las versiones modernas de Oracle ya no trabajas directamente sobre una única base de datos como ocurría en Oracle 11g y anteriores. Ahora existe una arquitectura compuesta por

```
CDB (Container Database)
│
├── CDB$ROOT
│
├── PDB$SEED
│
└── FREEPDB1
```

Siendo:
* `CDB$ROOT`: Es el contenedor principal. Contiene diccionarios de oracle, componentes internos del motor, metadatos comunes.
* `PDB$SEED`: Es una plantilla de sólo lectura.Oracle la utiliza para clonar nuevas PDBs.
* `FREEPDB1`: Es la base de datos donde se crean usuarios, tablespaces, esquemas, tablas, se cargan datos y se ejecutan aplicaciones.

Si deseas verificar en que contenedor te encuentras puedes utilizar el comando `SHOW CON_NAME;`

---

## 2. Configuración de Políticas de Contraseñas (Profiles)

Antes de crear cualquier usuario, es una buena práctica de seguridad configurar un perfil que regule las políticas de bloqueo, expiración y complejidad de las credenciales.

```sql
CREATE PROFILE secure_user_profile LIMIT
    FAILED_LOGIN_ATTEMPTS 3
    PASSWORD_LOCK_TIME 1
    PASSWORD_LIFE_TIME 90
    PASSWORD_REUSE_TIME 365
    PASSWORD_REUSE_MAX 5
    PASSWORD_VERIFY_FUNCTION ora12c_verify_function;
```

### Desglose de Opciones del Perfil:
* **`FAILED_LOGIN_ATTEMPTS 3`**: Bloquea la cuenta tras 3 intentos fallidos consecutivos de inicio de sesión.
* **`PASSWORD_LOCK_TIME 1`**: Mantiene la cuenta bloqueada durante 1 día tras alcanzar el límite de fallos.
* **`PASSWORD_LIFE_TIME 90`**: Obliga al usuario a cambiar su contraseña cada 90 días.
* **`PASSWORD_VERIFY_FUNCTION`**: Aplica una función nativa de Oracle para asegurar la complejidad de la clave (longitud, mezcla de mayúsculas, minúsculas, números y símbolos).


Se puede verificar la creación del profile a través de la siguiente consulta.

```SQL
SELECT DISTINCT profile
FROM dba_profiles
ORDER BY profile;
```

Incluso, se pueden revisar todos los limites configurados en el perfil mediante:

```SQL
SELECT profile,
       resource_name,
       limit
FROM dba_profiles
WHERE profile = 'DEFAULT'
ORDER BY resource_name;
```

---

## 3. Ejemplo Práctico: Creación de un Usuario Estándar para Gestión de Tablas

Para este ejemplo, crearemos un usuario estándar llamado `dev_user` que cumpla con el principio de privilegio mínimo, permitiéndole **crear y eliminar sus propias tablas** sin comprometer la seguridad de la instancia.

### Paso 3.1: Crear un Tablespace Dedicado
Aislar los datos del usuario en un tablespace propio evita saturar los espacios del sistema y facilita las tareas de mantenimiento y respaldos específicos.

### Paso 3.1.2 Verificación de almacenamiento de archivos
Antes de empezar a crear nuestro Tablespace, en versiones 23ai y superiores, es necesario identificar dónde es que Oracle almacena los archivos existentes. 

```SQL
SELECT file_name
FROM dba_data_files;
```

Esto deberia dar como resultado una ruta similar a `C:\APP\ORACLE\ORADATA\FREE\USERS01.DBF` (windows) ó `/u01/app/oracle/oradata/FREE/users01.dbf` (linux). Al setear el parametro `DATAFILE` debe especificarse la misma ruta para el nuevo archivo.

```sql
CREATE TABLESPACE dev_data_ts
    DATAFILE '\APP\ORACLE\ORADATA\FREE\DEV_DATA_TS.DBF'
    SIZE 500M 
    AUTOEXTEND ON NEXT 100M MAXSIZE 2G;
```

* **`SIZE 500M`**: Inicializa el archivo de datos con un tamaño de 500 Megabytes.
* **`AUTOEXTEND ON NEXT 100M MAXSIZE 2G`**: Permite que el archivo crezca automáticamente en bloques de 100 MB hasta un límite de 2 GB.
* `EXTENT MANAGEMENT LOCAL`: Oracle administra los extents.
* `SEGMENT SPACE MANAGEMENT AUTO`: Oracle administra el espacio libre internamente.

Podemos verificar la creación del Tablespace a través de la siguiente consulta:

```SQL
SELECT tablespace_name,
       status,
       contents
FROM dba_tablespaces
WHERE tablespace_name = 'dev_data_ts';
```


### Paso 3.2: Crear el Usuario y Asignarle Parámetros Base
Creamos el usuario utilizando el perfil de seguridad previamente definido y asociándolo a su tablespace correspondiente.

```sql
CREATE USER dev_user IDENTIFIED BY "password$";

ALTER USER dev_user 
    DEFAULT TABLESPACE dev_data_ts
    TEMPORARY TABLESPACE temp
    PROFILE secure_user_profile;
```

* **`CREATE USER`**: Registra el nuevo usuario en el diccionario de datos. El uso de comillas dobles en la contraseña garantiza que se respeten los caracteres especiales.
* **`DEFAULT TABLESPACE`**: Define dónde se ubicarán por defecto los objetos creados por este usuario.
* **`TEMPORARY TABLESPACE temp`**: Asigna el espacio temporal para operaciones de ordenamiento y uniones.

### Paso 3.3: Asignar Cuota de Almacenamiento
Sin una cuota explícita, el usuario no podrá insertar datos ni crear tablas de forma efectiva, aunque tenga los privilegios necesarios.

```sql
ALTER USER dev_user QUOTA 1G ON dev_data_ts;
```

* **`QUOTA 1G ON dev_data_ts`**: Otorga permiso para consumir hasta 1 GB de espacio dentro del tablespace designado. (De requerir crecimiento ilimitado, se emplearía `QUOTA UNLIMITED`).

### Paso 3.4: Otorgar Privilegios para Crear y Eliminar Tablas
Conforme a las mejores prácticas de seguridad, **no** se debe otorgar el rol de `DBA`. Se otorgan únicamente los privilegios precisos:

```sql
-- 1. Permiso indispensable para iniciar sesión en la base de datos
GRANT CREATE SESSION TO dev_user;

-- 2. Permiso para crear tablas dentro de su propio esquema
GRANT CREATE TABLE TO dev_user;
```

> **Nota importante sobre el borrado de tablas (`DROP`):** Al crear una tabla dentro de su propio esquema, el usuario adquiere automáticamente todos los privilegios sobre ella (`SELECT`, `INSERT`, `UPDATE`, `DELETE`, `DROP`). Por lo tanto, **no se debe otorgar** el privilegio global `DROP ANY TABLE` (el cual pertenece a los administradores y permite eliminar tablas de *cualquier* esquema). El usuario podrá eliminar sus propias tablas de manera nativa con solo tener `CREATE TABLE`.
