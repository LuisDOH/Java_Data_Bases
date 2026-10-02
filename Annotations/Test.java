import java.lang.reflect.Method;


public class Test{

  public static void main(String[] args) {
    System.out.println("Hola mundo");
    Connection cnx = new Connection();
    cnx.getConnection();
    // Lets check if a Annotation is present in out class
    System.out.println(cnx.getClass().isAnnotationPresent(MyAnnotation.class));

    // Check all methods available and run the one marked with
    // a RunNow Annotation
    //

    for(Method met: cnx.getClass().getDeclaredMethods()){
       System.out.println("Found method: " + met.getName());

      if(met.isAnnotationPresent(RunNow.class)){
         System.out.println("Found method: " + met.getName());

        //met.invoke(cnx);
        System.out.println("Metodo marcado encontrado");
        try{met.invoke(cnx);}
        catch (Exception e){
           
           
        }
       

      }
      
    }
  }
}
