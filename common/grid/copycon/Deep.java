package grid.copycon;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// this annotation is to mark an instance variable that must be
// an object, and to call the copy constructor to make the new object.
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Deep {
}
