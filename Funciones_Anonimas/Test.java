public class Test{
  
  public static void main(String[] args) {
   
    Operacion buscarV = (dato, coleccion)->{
      for(int num: coleccion){
        if(num == dato){
          System.out.println("Valor encontrado");
          return;
        }
      }
      System.out.println("Valor no encontrado en la lista");
    };

    Operacion promedio = (calificacion, lista_calificaciones)->{
      int sum = 0;
      for(int num:lista_calificaciones){
         sum += num;
      }
      System.out.println("El promedio de las calificaciones es: " + sum/lista_calificaciones.length); 
    };


    int [] nums = {10,35,22,1,7,-1,4};
    int [] calificaciones = {10,9,6,7,10};

    buscarV.ejecutar(-1, nums);
    promedio.ejecutar(10, calificaciones);
  }
}
