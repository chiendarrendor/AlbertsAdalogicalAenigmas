package grid.copycon;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


// this annotation is used if the proper behavior is to do a shallow
// copy of the instance variable
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Shallow {
}
