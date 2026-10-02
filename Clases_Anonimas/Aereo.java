public class Aereo{

  public String nombre;
  public int motores;

  public Aereo(String nombre){
    this.nombre = nombre;
  }

  public Aereo(){

  }

  public void describir(){
    System.out.println("Soy de tipo aereo y estoy despegando!");
  }
}
