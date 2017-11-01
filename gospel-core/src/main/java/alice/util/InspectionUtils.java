package alice.util;

import java.lang.reflect.Method;


/**
 * Utility methods for reflective operations.
 *
 * @author Michele Mannino
 */
public class InspectionUtils {

    /**
     * @param type:       class to be inspected
     * @param methodName: name of method
     * @param parms:      array of params
     * @author Michele Mannino
     */
    public static Method searchForMethod(Class<?> type, String methodName, Class<?>[] parms) {
        Method[] methods = type.getMethods();
        for (Method method : methods) {
            // Has to be named the same of course.
            if (!method.getName().equals(methodName))
                continue;

            Class<?>[] types = method.getParameterTypes();

            // Does it have the same number of arguments that we're looking for.
            if (types.length != parms.length)
                continue;

            // Check for type compatibility
            if (InspectionUtils.areTypesCompatible(types, parms))
                return method;
        }
        return null;
    }

    /**
     * Returns true if all classes in the sources list are assignment compatible
     * with the targets list.  In other words, if all targets[n].isAssignableFrom( sources[n] )
     * then this method returns true.
     * Any null values in sources are considered wild-cards and will skip the
     * isAssignableFrom check as if it passed.
     */
    private static boolean areTypesCompatible(Class<?>[] targets, Class<?>[] sources) {
        if (targets.length != sources.length)
            return (false);

        for (int i = 0; i < targets.length; i++) {
            if (sources[i] == null)
                continue;

            if (targets[i].isInterface()) {
                Class<?>[] interfaces = sources[i].getInterfaces();
                for (Class<?> in : interfaces) {
                    if (targets[i].equals(in))
                        return true;
                }
            }

            if (!translateFromPrimitive(targets[i]).isAssignableFrom(translateFromPrimitive(sources[i])))
                return false;
        }
        return true;
    }

    /**
     * If this specified class represents a primitive type (int, float, etc.) then
     * it is translated into its wrapper type (Integer, Float, etc.).  If the
     * passed class is not a primitive then it is just returned.
     */
    private static Class<?> translateFromPrimitive(Class<?> primitive) {
        if (!primitive.isPrimitive())
            return (primitive);

        if (Boolean.TYPE.equals(primitive))
            return (Boolean.class);
        if (Character.TYPE.equals(primitive))
            return (Character.class);
        if (Byte.TYPE.equals(primitive))
            return (Byte.class);
        if (Short.TYPE.equals(primitive))
            return (Short.class);
        if (Integer.TYPE.equals(primitive))
            return (Integer.class);
        if (Long.TYPE.equals(primitive))
            return (Long.class);
        if (Float.TYPE.equals(primitive))
            return (Float.class);
        if (Double.TYPE.equals(primitive))
            return (Double.class);

        throw new RuntimeException("Error translating type:" + primitive);
    }
}
