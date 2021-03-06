package com.aldebaran.qi.serialization;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.aldebaran.qi.AnyObject;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.QiStruct;
import com.aldebaran.qi.Tuple;

/**
 * Describe a method.<br>
 * It contains the method name, return type and parameters type.<br>
 * For choose the best method that corresponds to a search one, we compute a "distance" between methods:
 * <ul>
 *  <li>This "distance" is build for when match exactly, the distance is 0.</li>
 *  <li>If the two methods have different name or different number of parameters the distance is {@link Integer#MAX_VALUE "infinite"}.</li>
 *  <li>The distance between primitive and their associated Object (For example int &lt;-&gt; java.lang.Integer, boolean &lt;-&gt; java.lang.Boolean, ...) is {@link #DISTANCE_PRIMITIVE_OBJECT}.</li>
 *  <li>The distance between two numbers (double, float, ...) is {@link #DISTANCE_NUMBERS}.</li>
 *  <li>The distance (for returned value only) between a type and a Future that embed this type is {@link #DISTANCE_FUTURE}.</li>
 *  <li>The distance with a {@link Tuple} and {@link QiStruct} is {@link #DISTANCE_TUPLE_STRUCT}</li>
 *  <li>For others case the distance becomes {@link Integer#MAX_VALUE "infinite"}</li>
 * </ul>
 * By example for libqi signature "call::s(i)":
 * <table border="1">
 *  <tr><th>Method Java</th><th>Distance</th></tr>
 *  <tr><td>String call(int i)</td><td>0 = 0 (Distance String and 's') + 0 (Distance int and 'i')</td></tr>
 *  <tr><td>String call(Integer i)</td><td>1 = 0 (Distance String and 's') + 1 (Distance Integer and 'i')</td></tr>
 *  <tr><td>Future&lt;String&gt; call(int i)</td><td>100 = 100 (Distance Future&lt;String&gt; and 's') + 0 (Distance int and 'i')</td></tr>
 *  <tr><td>Future&lt;String&gt; call(Integer i)</td><td>101 = 100 (Distance Future&lt;String&gt; and 's') + 1 (Distance Integer and 'i')</td></tr>
 * </table>
 */
public class MethodDescription {
    /**
     * Couple of next read index and read class.
     */
    static class ClassIndex {
        /**
         * Next index to read.
         */
        final int index;
        /**
         * Read class
         */
        final Class<?> claz;

        /**
         * Create the couple.
         *
         * @param index Next index to read.
         * @param claz  Read class.
         */
        ClassIndex(final int index, final Class<?> claz) {
            this.index = index;
            this.claz = claz;
        }

        /**
         * String representation for debug purpose.
         *
         * @return String representation.
         */
        @Override
        public String toString() {
            return this.index + " :" + this.claz.getName();
        }
    }

    /**
     * "Distance" between primitive type and its Object representation
     */
    private static final int DISTANCE_PRIMITIVE_OBJECT = 1;
    /**
     * "Distance" between numbers: need expand or truncate the value to do the
     * conversion
     */
    private static final int DISTANCE_NUMBERS = 10;
    /**
     * "Distance" between Future and real type (For returned value only)
     */
    private static final int DISTANCE_FUTURE = 100;
    /**
     * "Distance" between Tuple and associated QIStruct
     */
    private static final int DISTANCE_TUPLE_STRUCT = 1000;
    /**
     * "Distance" between any object and something else : WARNING dangerous, but
     * not have the choice for now
     */
    private static final int DISTANCE_ANY_OBJECT = 100000;
    /**
     * "Distance" between compatible types like List and ArrayList, Map and Hashmap, ...
     */
    private static final int DISTANCE_COMPATIBLE= 100;

    /**
     * Read the next class described by characters array at given offset.<br>
     * It returns a couple of read class and next index to read the rest of the
     * characters array.
     *
     * @param offset     Offset where start to read.
     * @param characters Contains a JNI signature.
     * @return Couple of read class and next index to read.
     * @throws IllegalArgumentException If characters array not a valid JNI signature.
     */
    static ClassIndex nextClassIndex(int offset, final char[] characters) {
        Class<?> claz;

        switch (characters[offset]) {
            case 'V':
                claz = void.class;
                break;
            case 'Z':
                claz = boolean.class;
                break;
            case 'B':
                claz = byte.class;
                break;
            case 'C':
                claz = char.class;
                break;
            case 'S':
                claz = short.class;
                break;
            case 'I':
                claz = int.class;
                break;
            case 'J':
                claz = long.class;
                break;
            case 'F':
                claz = float.class;
                break;
            case 'D':
                claz = double.class;
                break;
            case 'L': {
                final int length = characters.length;
                offset++;
                final int start = offset;

                while (offset < length && characters[offset] != ';') {
                    offset++;
                }

                if (offset >= length) {
                    throw new IllegalArgumentException(new String(characters) + " not valid JNI signature");
                }

                String string = new String(characters, start, offset - start);
                string = string.replace('/', '.');

                try {
                    claz = Class.forName(string);
                } catch (final ClassNotFoundException e) {
                    e.printStackTrace();
                    throw new IllegalArgumentException(new String(characters) + " not valid JNI signature", e);
                }
            }
            break;
            case '[': {
                int count = 0;

                while (characters[offset] == '[') {
                    offset++;
                    count++;
                }

                final int[] sizes = new int[count];

                for (int i = 0; i < count; i++) {
                    sizes[i] = 1;
                }

                final ClassIndex classIndex = MethodDescription.nextClassIndex(offset, characters);
                final Object array = Array.newInstance(classIndex.claz, sizes);
                claz = array.getClass();
                offset = classIndex.index - 1;
            }
            break;
            default:
                throw new IllegalArgumentException(new String(characters) + " not valid JNI signature");
        }

        return new ClassIndex(offset + 1, claz);
    }

    /**
     * Compute method description with method name and JNI signature.
     *
     * @param methodName   Method name.
     * @param signatureJNI JNI signature.
     * @return Method description created.
     * @throws IllegalArgumentException If signatureJNI not a valid JNI signature.
     */
    public static MethodDescription fromJNI(final String methodName, final String signatureJNI) {
        final char[] characters = signatureJNI.toCharArray();

        if (characters[0] != '(') {
            throw new IllegalArgumentException(signatureJNI + " not valid JNI signature");
        }

        final List<Class<?>> parametersType = new ArrayList<Class<?>>();
        Class<?> returnType = null;
        final int length = characters.length;
        boolean isParameter = true;
        int offset = 1;
        ClassIndex classIndex;

        while (offset < length) {
            switch (characters[offset]) {
                case ')':
                    isParameter = false;
                    offset++;
                    break;
                default:
                    classIndex = MethodDescription.nextClassIndex(offset, characters);
                    offset = classIndex.index;

                    if (isParameter) {
                        parametersType.add(classIndex.claz);
                    } else if (returnType == null) {
                        returnType = classIndex.claz;
                    } else {
                        throw new IllegalArgumentException(signatureJNI + " not valid JNI signature");
                    }
            }
        }

        if (returnType == null) {
            throw new IllegalArgumentException(signatureJNI + " not valid JNI signature");
        }

        return new MethodDescription(methodName, returnType, parametersType.toArray(new Class[parametersType.size()]));
    }

    /**
     * Method name.
     */
    private final String methodName;
    /**
     * Method return type.
     */
    private final Class<?> returnType;
    /**
     * Method parameters types.
     */
    private final Class<?>[] parametersType;

    /**
     * Create method description.
     *
     * @param methodName     Method name.
     * @param returnType     Method return type.
     * @param parametersType Method parameters types.
     */
    public MethodDescription(final String methodName, final Class<?> returnType, final Class<?>[] parametersType) {
        this.methodName = methodName;
        this.returnType = returnType;
        this.parametersType = parametersType;
    }

    /**
     * Method name.
     *
     * @return Method name.
     */
    public String getMethodName() {
        return this.methodName;
    }

    /**
     * Method return type.
     *
     * @return Method return type.
     */
    public Class<?> getReturnType() {
        return this.returnType;
    }

    /**
     * Method parameters types.
     *
     * @return Method parameters types.
     */
    public Class<?>[] getParametersType() {
        return this.parametersType;
    }

    /**
     * Compute the "distance" between the given method with this description.<br>
     * If "distance" is 0, it means the given method is exactly the method
     * description.<br>
     * If "distance" is {@link Integer#MAX_VALUE}, if means the given method is
     * completely different and incompatible.<br>
     * For other values of "distance" it means the given method is compatible to
     * method description. More the value is near 0, more the compatibility is
     * easy.
     *
     * @param method Method to measure the "distance" with.
     * @return The computed "distance".
     */
    public int distance(final Method method) {
        if (!this.methodName.equals(method.getName())) {
            return Integer.MAX_VALUE;
        }

        final Class<?>[] parameters = method.getParameterTypes();
        final int length = parameters.length;

        if (length != this.parametersType.length) {
            return Integer.MAX_VALUE;
        }

        int distance = MethodDescription.distance(this.returnType, method.getReturnType(), true);

        for (int index = 0; index < length; index++) {
            distance = MethodDescription.addLimited(distance,
                    MethodDescription.distance(this.parametersType[index], parameters[index], false));
        }

        return distance;
    }

    /**
     * Add two positive (or zero) integers.<br>
     * The addition result guarantees to be limited to {@link Integer#MAX_VALUE}
     * .<br>
     * It avoids the overflow issue.
     *
     * @param integer1 First integer.
     * @param integer2 Second integer.
     * @return The addition.
     */
    private static int addLimited(final int integer1, final int integer2) {
        assert integer1 >= 0 && integer2 >= 0;

        if (integer1 >= Integer.MAX_VALUE - integer2) {
            return Integer.MAX_VALUE;
        }

        return integer1 + integer2;
    }

    /**
     * Compute the "distance" between two class.<br>
     * If "distance" is 0, it means the classes are the same class.<br>
     * If "distance" is {@link Integer#MAX_VALUE}, if means the classes are
     * completely different and incompatible.<br>
     * For other values of "distance" it means the given the classes are
     * compatible. More the value is near 0, more the compatibility is easy.
     *
     * @param class1
     *            One class.
     * @param class2
     *            Other class.
     * @param acceptFuture
     *            Indicates if future is accepted for compute distance
     * @return Computed "distance".
     */
    private static int distance(final Class<?> class1, final Class<?> class2, boolean acceptFuture) {
        if (class1.equals(class2)) {
            return 0;
        }

        if (SignatureUtilities.isVoid(class1) && SignatureUtilities.isVoid(class2)) {
            return MethodDescription.DISTANCE_PRIMITIVE_OBJECT;
        }

        if (SignatureUtilities.isBoolean(class1) && SignatureUtilities.isBoolean(class2)) {
            return MethodDescription.DISTANCE_PRIMITIVE_OBJECT;
        }

        if (SignatureUtilities.isCharacter(class1) && SignatureUtilities.isCharacter(class2)) {
            return MethodDescription.DISTANCE_PRIMITIVE_OBJECT;
        }

        if (SignatureUtilities.isByte(class1) && SignatureUtilities.isByte(class2)) {
            return MethodDescription.DISTANCE_PRIMITIVE_OBJECT;
        }

        if (SignatureUtilities.isShort(class1) && SignatureUtilities.isShort(class2)) {
            return MethodDescription.DISTANCE_PRIMITIVE_OBJECT;
        }

        if (SignatureUtilities.isInteger(class1) && SignatureUtilities.isInteger(class2)) {
            return MethodDescription.DISTANCE_PRIMITIVE_OBJECT;
        }

        if (SignatureUtilities.isLong(class1) && SignatureUtilities.isLong(class2)) {
            return MethodDescription.DISTANCE_PRIMITIVE_OBJECT;
        }

        if (SignatureUtilities.isFloat(class1) && SignatureUtilities.isFloat(class2)) {
            return MethodDescription.DISTANCE_PRIMITIVE_OBJECT;
        }

        if (SignatureUtilities.isDouble(class1) && SignatureUtilities.isDouble(class2)) {
            return MethodDescription.DISTANCE_PRIMITIVE_OBJECT;
        }

        if (SignatureUtilities.isNumber(class1) && SignatureUtilities.isNumber(class2)) {
            return MethodDescription.DISTANCE_NUMBERS;
        }

        if (acceptFuture && (Future.class.isAssignableFrom(class1) || Future.class.isAssignableFrom(class2))) {
            return MethodDescription.DISTANCE_FUTURE;
        }

        if ((Tuple.class.isAssignableFrom(class1) && class2.isAnnotationPresent(QiStruct.class))
                || (class1.isAnnotationPresent(QiStruct.class) && Tuple.class.isAssignableFrom(class2))) {
            return MethodDescription.DISTANCE_TUPLE_STRUCT;
        }

        if (AnyObject.class.isAssignableFrom(class1) || AnyObject.class.isAssignableFrom(class2)) {
            return MethodDescription.DISTANCE_ANY_OBJECT;
        }

        if(class1.isAssignableFrom(class2) || class2.isAssignableFrom(class1)) {
            return MethodDescription.DISTANCE_COMPATIBLE;
        }

        return Integer.MAX_VALUE;
    }

    /**
     * String description for debug purpose
     *
     * @return String description for debug purpose
     */
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.methodName);
        stringBuilder.append('(');

        if (this.parametersType != null) {
            boolean first = true;

            for (Class<?> clazz : this.parametersType) {
                if (!first) {
                    stringBuilder.append(", ");
                }

                first = false;
                stringBuilder.append(clazz.getName());
            }
        }

        stringBuilder.append("):");

        if (SignatureUtilities.isVoid(this.returnType)) {
            stringBuilder.append("void");
        }
        else {
            stringBuilder.append(this.returnType.getName());
        }

        return stringBuilder.toString();
    }
}
