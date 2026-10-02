# Clases anónimas

A diferencia de las expresiones lambda (que son solo fragmentos de código atados a una interfaz funcional), una clase anónima es una clase real y completa que se declara y se instancia al mismo tiempo, sin darle un nombre explícito.

A diferencia de las lambdas (que no pueden tener estado interno ni múltiples métodos), una clase anónima sí puede tener sus propios atributos (campos) y múltiples métodos, además de implementar lo que se le pida.


Según la Especificación del Lenguaje Java (Java Language Specification - JLS), una clase anónima es una expresión de creación de clase que hace tres cosas a la vez:

1. Declara una clase sin nombre.
2. La instancia (crea un objeto en memoria).
3. Hereda de una clase existente o implementa una interfaz.

## Reglas de implementación

* __No puedes usar constructores explícitos__: Como la clase no tiene nombre, no puedes declarar un constructor tradicional (`public MiClaseAnonima() { ... }`). Sin embargo, puedes usar bloques de inicialización de instancia (`{ ... }`) para simular un constructor o recibir parámetros en el método que la crea.

* __No puede ser abstracta__: Una clase anónima debe ser concreta; por lo tanto, estás obligado a implementar todos los métodos abstractos heredados de la superclase o interfaz que esté tomando como base.

* __No puede tener modificadores de acceso__: No puedes ponerles public, private, protected ni static. Tienen el ámbito del bloque donde se declaran.

* __Captura de variables__: Al igual que las lambdas, pueden acceder a las variables locales del entorno exterior, pero estas deben ser final. Es decir se puede manejar una variable propia del sitio donde se crea la función anónima siempre y cuando esta este definida como final.

> **Nota:** Las variables locales de un método (como un int numero = 10;) viven en la pila (stack). Cuando el método termina de ejecutarse, su memoria se borra y esas variables desaparecen.

Las funciones anónimas o las clases anónimas crean un objeto que vive en el montículo (heap). Ese objeto puede seguir vivo mucho después de que el método original haya terminado (por ejemplo, si se lo pasas a un hilo en segundo plano).


## Estructura general

````java
NombreTipo referencia = new NombreTipo([argumentos]) {
    // Atributos y métodos propios de la clase anónima
};
````


Donde `NombreTipo` puede ser:
1. Una Interfaz (la implementa).
2. Una Clase Abstracta (extiende e implementa sus métodos abstractos).
3. Una Clase Concreta normal (extiende y puede sobrescribir sus métodos).
