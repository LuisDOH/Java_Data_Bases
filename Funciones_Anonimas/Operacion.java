 /*
    Desarrollemos una interfaz para cualquier tipo de operacion requerida la cual
    tenga como parametros un valor y una lista de tipo enteros.
 */

@FunctionalInterface
public interface Operacion{
  public void ejecutar(int value, int[] array);
}
