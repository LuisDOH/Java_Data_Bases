public class Test {
  public static void main(String[] args) {
    

    System.out.println("Hola mundo");
    Auto camion = new Auto(){
      @Override
      public void describir(){
        System.out.println("Hola soy un camion");
      }
    };

    camion.describir();

    Auto carro =  new Auto(){
      @Override
      public void describir(){
        System.out.println("Hola soy un carro");
      }
    };

    carro.describir();

    Aereo avion = new Aereo("Avion"){
      @Override
      public void describir(){
        super.describir();
        System.out.println("Soy un " + this.nombre);
      }
    };

    avion.describir();

    // Agregando un metodo que no exixte en la clase padre, requiere ser ejecutado
    new Aereo(){
      public void aterrizar(){
        System.out.println("Estoy aterrizando");

      }
    }.aterrizar();
  
  }
 }
