package com.nuance.cobra.test;


import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@SuppressWarnings({"WeakerAccess", "java:S3011"})
public class TestUtils
{
    private TestUtils() { throw new IllegalStateException("Utility class"); }

    /**
     * Inject a value into a static field of a class. This won't always work for final fields because since
     * they are final the compiler and JVM may cache or take some other shortcut that would cause it not to
     * see the new value.
     *
     * @param <T>
     *            The captured type of the value set on the field so that it can return the previous value
     *            with the same type.
     * @param c
     *            The class whose field should be set
     * @param fieldName
     *            The name of the field to set
     * @param val
     *            The new value
     * @return The previous value of the field
     * @throws NoSuchFieldException
     *             If no field with that name exists in the object
     * @throws IllegalAccessException
     *             This shouldn't happen but it would indicate that there was a problem setting the field.
     */
    public static <T> T inject(Class<?> c, String fieldName, T val) throws NoSuchFieldException,
                                                                           IllegalAccessException
    {
        return inject(null, c, fieldName, val);
    }

    /**
     * Inject a value into an object. This version allows setting fields from superclasses that exist in
     * subclasses This won't always work for final fields because since they are final the compiler and JVM
     * may cache or take some other shortcut that would cause it not to see the new value.
     *
     * @param <T>
     *            The captured type of the value set on the field so that it can return the previous value
     *            with the same type.
     * @param obj
     *            The object to set the value in or null to set a static variable
     * @param c
     *            The class whose field should be set
     * @param fieldName
     *            The name of the field to set
     * @param val
     *            The new value
     * @return The previous value of the field
     * @throws NoSuchFieldException
     *             If no field with that name exists in the object
     * @throws IllegalAccessException
     *             This shouldn't happen but it would indicate that there was a problem setting the field.
     */
    @SuppressWarnings("unchecked")
    public static <T> T inject(Object obj, Class<?> c, String fieldName, T val) throws NoSuchFieldException,
                                                                                       IllegalAccessException
    {

        Field field = getField(c, fieldName);
        field.setAccessible(true);

        T prevVal = (T) field.get(obj);

        // Only necessary for static final
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(obj, val);

        return prevVal;
    }

    /**
     * Return the value of a field.
     *
     * @param c
     *            The class to read the value from
     * @param obj
     *            The instance to read the value from or null for static fields
     * @param fieldName
     *            The name of the field to read
     * @return The value of the field
     * @throws NoSuchFieldException
     *             If no field with that name exists in the object
     * @throws IllegalAccessException
     *             This shouldn't happen but it would indicate that there was a problem reading the field.
     */
    public static Object getFieldValue(Class<?> c, Object obj, String fieldName) throws NoSuchFieldException,
                                                                                        IllegalAccessException
    {
        Field field = getField(c, fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }

    /**
     * Return the field object of a class. This method looks up the class hierarchy until it finds a field
     * with the name requested.
     *
     * @param c
     *            The class to read the value from
     * @param fieldName
     *            The name of the field to read
     * @return The field
     * @throws NoSuchFieldException
     *             If no field with that name exists in the object
     */
    public static Field getField(Class<?> c, String fieldName) throws NoSuchFieldException
    {
        Field f;
        try
        {
            f = c.getDeclaredField(fieldName);
        }
        catch (NoSuchFieldException ex)
        {
            Class<?> superClass = c.getSuperclass();
            if (superClass != null)
            {
                f = getField(superClass, fieldName);
            }
            else
            {
                throw ex;
            }
        }
        return f;
    }

}
