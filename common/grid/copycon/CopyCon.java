package grid.copycon;

import javax.management.relation.RoleUnresolved;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CopyCon {
    private CopyCon() {}

    private static enum AnnoType {DEEP,IGNORE, SHALLOW};
    private static AnnoType getAnnoType(Field f) {
        int anncount =
                (f.isAnnotationPresent(Deep.class) ? 1 : 0) +
                        (f.isAnnotationPresent(Ignore.class) ? 1 : 0) +
                        (f.isAnnotationPresent(Shallow.class) ? 1 : 0);

        if (anncount == 0) throw new RuntimeException("Field " + f.getName() + " has no copycon annotation!");
        if (anncount > 1) throw new RuntimeException("Field " + f.getName() + " has multiple copycon annotations!");

        if (f.isAnnotationPresent(Deep.class)) return AnnoType.DEEP;
        if (f.isAnnotationPresent(Ignore.class)) return AnnoType.IGNORE;
        return AnnoType.SHALLOW;
    }



    public static <T> void copy(T thing, T right) {
        Class<T> clazz = (Class<T>)thing.getClass();

        try {
            for (Field f : clazz.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) continue;
                f.setAccessible(true);
                switch(getAnnoType(f)) {
                    case DEEP:
                        if (!f.getType().toString().startsWith("class"))
                            throw new RuntimeException("Deep Copy of " + f.getName() + " must be on object, not " + f.getType());
                        Class c = f.getType();
                        try {
                            Method clone = c.getDeclaredMethod("clone");
                            f.set(thing, clone.invoke(f.get(right)));
                            continue;
                        } catch (NoSuchMethodException nme){
                            // do nothing...
                        }

                        // the other possibility is if we have a copy constructor?
                        try {
                            Constructor con = c.getDeclaredConstructor(c);
                            f.set(thing,con.newInstance(f.get(right)));
                            continue;
                        } catch (NoSuchMethodException e) {
                            // do nothing.
                        }

                        throw new RuntimeException("CopyCon Failure: no deep copy mechanism found for " + f.getName());
                    case IGNORE:
                        break;
                    case SHALLOW:
                        f.set(thing, f.get(right));
                        break;
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("CopyCon Failure: ",e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("CopyCon Failure: ",e);
        } catch (InstantiationException e) {
            throw new RuntimeException("CopyCon Failure: ",e);
        }


    }
}
