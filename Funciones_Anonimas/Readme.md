# Funciones Anonimas
A pesar de que en versiones anteriores de Java, las funciones anónimas podían crearse implementando
`Runnable()`, en las versiones moderlas de Java, la creación de funciones anónimas se realiza exclusivamente
a través de interfaces funcionales.

> **Nota:** Las interfaces funcionales contienen solo un método. Si una interfaz tiene más de un método abstracto, el compilador de Java te arrojará un error si intentas pasarle una función anónima (lambda).

## Implementción 

````java
@FunctionalInterface // Anotación opcional pero recomendada
interface Operacion {
    int calcular(int a, int b); // Único método abstracto
}

// Implementación con función anónima (Lambda)
Operacion suma = (a, b) -> a + b;

````

A diferencia de lenguajes como JavaScript o Python, donde las funciones son __"ciudadanos de primera clase"__ y pueden existir por sí mismas, Java no tiene un tipo de datos nativo llamado "función". Para que Java sepa qué tipo de estructura tiene una lambda, esta siempre necesita un Target Type (tipo de destino), y ese tipo de destino debe ser obligatoriamente una interfaz funcional (es decir, una interfaz que tenga un único método abstracto, también conocida como SAM: Single Abstract Method).

