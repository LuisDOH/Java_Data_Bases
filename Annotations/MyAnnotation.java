/*
 *  Interfaces are metaData that can be used in direfent parts of 
 *  java declarations like class, methods, fields, and other program 
 *  elements.
 *
 *  It can use elements like arguments in a class, the main difference is
 *  Annotation elements are declared like methods assing a () at the end 
 *  of the element name.
 *  int age();
 *  String name();
 *
 *  When you create a new Annotation you should add a target and define 
 *  with the parameter ElementType
 *
 *  @Target({ElementType.Type})
 *  .Type =  clases
 *
 *  Another annotation that you should add is the retention policy, 
 *  @Retention(RetentionPolicy.SOURCE)
 *  .SOURCE -> only exist until compilation, when compilation starts it dissapear, 
 *  .RUNTIME -> exist during the execution
 *  .CLASS -> Exist until the compilation finishes
 *
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MyAnnotation{ }
