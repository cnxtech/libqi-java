/*
 * * Copyright (C) 2015 Softbank Robotics * See COPYING for the license
 */

package com.aldebaran.qi;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import com.aldebaran.qi.serialization.QiSerializer;
import com.aldebaran.qi.util.UtilReflexion;

/**
 * Class that provides type erasure on objects. It represents an object
 * (understandable by the messaging layer) that has shared semantics and that
 * can contain methods, signals and properties.
 * <p>
 * This class is typically used in {@link Session} when retrieving a remote
 * service by its name. The service itself is not retrieved, but a reference to
 * it, in order to call its methods, connect to its signals and access its
 * properties. It is also used when creating and registering a new
 * {@link QiService}.
 * <p>
 * Use {@link DynamicObjectBuilder} to create an instance of this class.
 *
 * @see DynamicObjectBuilder
 */
public class AnyObject implements Comparable<AnyObject> {

    static {
        // Loading native C++ libraries.
        if (!EmbeddedTools.LOADED_EMBEDDED_LIBRARY) {
            EmbeddedTools loader = new EmbeddedTools();
            loader.loadEmbeddedLibraries();
        }
    }

    /**
     * _obj->ptrUid; Compare 2 {@link AnyObject}. <br>
     * The return value is: <br>
     * <table border=1>
     * <tr>
     * <th>comparison</th>
     * <th>result</th>
     * </tr>
     * <tr>
     * <td>object1 &lt; object2</td>
     * <th>-1</th>
     * </tr>
     * <tr>
     * <td>object1 == object2</td>
     * <th>0</th>
     * </tr>
     * <tr>
     * <td>object1 &gt; object2</td>
     * <th>1</th>
     * </tr>
     * </table>
     *
     * @param object1
     *            Any object reference 1
     * @param object2
     *            Any object reference 2
     * @return Comparison value
     */
    private static native int compare(long object1, long object2);

    /**
     * Hash {@link AnyObject}. <br>
     * Returns a hash code value for this AnyObject
     */
    private static native int hash(long object);

    private final long _p;

    private native long property(long pObj, String property) throws DynamicCallException;

    private native long setProperty(long pObj, String property, Object value) throws DynamicCallException;

    private native long asyncCall(long pObject, String method, Object[] args) throws DynamicCallException;

    private native String metaObjectToString(long pObject);

    private native void destroy(long pObj);

    private native long disconnect(long pObject, long subscriberId) throws RuntimeException;

    private native long connectSignal(long pObject, String signalName, QiSignalListener listener);

    private native long disconnectSignal(long pObject, long subscriberId);

    private native void post(long pObject, String name, Object[] args);

    public static native Object decodeJSON(String str);

    public static native String encodeJSON(Object obj);

    /**
     * AnyObject constructor is not public, user must use DynamicObjectBuilder.
     *
     * @see DynamicObjectBuilder
     */
    AnyObject(long _p) {
        this._p = _p;
    }

    public Future<Void> setProperty(QiSerializer serializer, String property, Object o) {
        try {
            // convert custom structs to tuples if necessary
            Object convertedProperty = serializer.serialize(o);
            return new Future<Void>(setProperty(_p, property, convertedProperty));
        }
        catch (QiConversionException e) {
            throw new QiRuntimeException(e);
        }
    }

    public Future<Void> setProperty(String property, Object o) {
        return setProperty(QiSerializer.getDefault(), property, o);
    }

    public <T> Future<T> property(String property) {
        return new Future<T>(property(_p, property));
    }

    /**
     * Retrieve the value of {@code property} asynchronously. Tuples will be
     * converted to structs in the result, according to the {@code targetType}.
     *
     * @param targetType
     *            the target result type
     * @param property
     *            the property
     * @return a future to the converted result
     */
    public <T> Future<T> getProperty(final QiSerializer serializer, final Type targetType, String property) {
        return property(property).andThenApply(new Function<Object, T>() {
            @Override
            public T execute(Object value) throws Throwable {
                return (T) serializer.deserialize(value, targetType);
            }
        });
    }

    public <T> Future<T> getProperty(Type targetType, String property) {
        return getProperty(QiSerializer.getDefault(), targetType, property);
    }

    public <T> Future<T> getProperty(QiSerializer serializer, Class<T> targetType, String property) {
        // Specialization to benefit from type inference when targetType is a
        // Class
        return getProperty(serializer, (Type) targetType, property);
    }

    public <T> Future<T> getProperty(Class<T> targetType, String property) {
        // Specialization to benefit from type inference when targetType is a
        // Class
        return getProperty(QiSerializer.getDefault(), (Type) targetType, property);
    }

    /**
     * Perform asynchronous call and return Future return value
     *
     * @param method
     *            Method name to call
     * @param args
     *            Arguments to be forward to remote method
     * @return Future method return value
     * @throws DynamicCallException
     */
    public <T> Future<T> call(String method, Object... args) {
        // Nulls checks
        // Recursive search, the "null" can hide at any deep
        final Stack stack = new Stack();

        if (args != null) {
            for (final Object object : args) {
                stack.push(object);
            }
        }

        while (!stack.isEmpty()) {
            final Object object = stack.pop();

            if (object != null) {
                if (object instanceof Tuple) {
                    final Tuple tuple = (Tuple) object;
                    final int size = tuple.size();

                    for (int index = 0; index < size; index++) {
                        final Object element = tuple.get(index);

                        if (element == null) {
                            throw new NullPointerException("One field of a tuple is null!");
                        }

                        stack.push(element);
                    }
                }
                else if (object instanceof Map) {
                    final Map map = (Map) object;

                    for (final Object element : map.entrySet()) {
                        final Entry entry = (Entry) element;
                        stack.push(entry.getKey());
                        stack.push(entry.getValue());
                    }
                }
                else if (object instanceof List) {
                    final List list = (List) object;

                    for (final Object element : list) {
                        stack.push(element);
                    }
                }
            }
        }

        // Do the call
        return new Future<T>(asyncCall(_p, method, args));
    }

    /**
     * Convert structs in {@code args} to tuples if necessary, then call
     * {@code method} asynchronously. Tuples will be converted to structs in the
     * result, according to the {@code targetType}.
     *
     * @param targetType
     *            the target result type
     * @param method
     *            the method
     * @param args
     *            the method arguments
     * @return a future to the converted result
     */
    public <T> Future<T> call(final QiSerializer serializer, final Type targetType, String method, Object... args) {
        try {
            Object[] convertedArgs = (Object[]) serializer.serialize(args);
            return this.call(method, convertedArgs).andThenApply(new Function<Object, T>() {
                @Override
                public T execute(Object value) throws Throwable {
                    return (T) serializer.deserialize(value, targetType);
                }
            });
        }
        catch (QiConversionException e) {
            throw new QiRuntimeException(e);
        }
    }

    public <T> Future<T> call(final Type targetType, String method, Object... args) {
        return call(QiSerializer.getDefault(), targetType, method, args);
    }

    public <T> Future<T> call(QiSerializer serializer, Class<T> targetType, String method, Object... args) {
        // Specialization to benefit from type inference when targetType is a
        // Class
        return call(serializer, (Type) targetType, method, args);
    }

    public <T> Future<T> call(Class<T> targetType, String method, Object... args) {
        // Specialization to benefit from type inference when targetType is a
        // Class
        return call((Type) targetType, method, args);
    }

    public QiSignalConnection connect(String signalName, QiSignalListener listener) {
        long futurePtr = connectSignal(_p, signalName, listener);
        return new QiSignalConnection(this, new Future<Long>(futurePtr));
    }

    public QiSignalConnection connect(final QiSerializer serializer, String signalName, final Object annotatedSlotContainer,
            String slotName) {
        final Method method = findSlot(annotatedSlotContainer, slotName);

        if (method == null)
            throw new QiSlotException("Slot \"" + slotName + "\" not found in " + annotatedSlotContainer.getClass().getName()
                    + " (did you forget the @QiSlot annotation?)");

        return connect(signalName, new QiSignalListener() {
            @Override
            public void onSignalReceived(Object... args) {
                Object[] convertedArgs = null;
                try {
                    method.setAccessible(true);
                    // convert tuples to custom structs if necessary
                    try {
                        convertedArgs = serializer.deserialize(args, method.getGenericParameterTypes());
                    }
                    catch (Exception exception) {
                        exception.printStackTrace();

                        final Class<?>[] parametersTypes = method.getParameterTypes();
                        final int length = parametersTypes.length;
                        convertedArgs = new Object[length];
                        Object toConvert;
                        final int limit = Math.min(length, args == null ? 0 : args.length);

                        // Fill parameters received
                        for (int index = 0; index < limit; index++) {
                            toConvert = args[index];

                            if (toConvert == null) {
                                convertedArgs[index] = UtilReflexion.defaultValue(parametersTypes[index]);
                            }
                            else {
                                try {
                                    convertedArgs[index] = serializer.deserialize(args[index], parametersTypes[index]);
                                }
                                catch (Exception ignored) {
                                    convertedArgs[index] = toConvert;
                                }
                            }
                        }

                        // Fill missing parameters with default value
                        for (int index = limit; index < length; index++) {
                            convertedArgs[index] = UtilReflexion.defaultValue(parametersTypes[index]);
                        }
                    }

                    method.invoke(annotatedSlotContainer, convertedArgs);
                }
                catch (IllegalAccessException e) {
                    e.printStackTrace();
                    throw new QiSlotException(e);
                }
                catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    String message = "Cannot call method " + method + " with parameter types "
                            + Arrays.toString(getTypes(convertedArgs));
                    throw new QiSlotException(message, e);
                }
                catch (InvocationTargetException e) {
                    e.printStackTrace();
                    throw new QiSlotException(e);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    throw new QiSlotException(e);
                }
            }
        });
    }

    public QiSignalConnection connect(String signalName, final Object annotatedSlotContainer, String slotName) {
        return connect(QiSerializer.getDefault(), signalName, annotatedSlotContainer, slotName);
    }

    private static Class<?>[] getTypes(Object[] values) {
        Class<?>[] types = new Class[values.length];
        for (int i = 0; i < types.length; ++i) {
            Object value = values[i];
            types[i] = value == null ? null : value.getClass();
        }
        return types;
    }

    Future<Void> disconnect(QiSignalConnection connection) {
        return connection.getFuture().andThenCompose(new Function<Long, Future<Void>>() {
            @Override
            public Future<Void> execute(Long value) throws Throwable {
                return new Future<Void>(disconnectSignal(_p, value));
            }
        });
    }

    /**
     * Disconnect a previously registered callback.
     *
     * @param subscriberId
     *            id returned by connect()
     */
    @Deprecated
    public long disconnect(long subscriberId) {
        return disconnect(_p, subscriberId);
    }

    /**
     * Post an event advertised with advertiseEvent method.
     *
     * @param eventName
     *            Name of the event to trigger.
     * @param args
     *            Arguments sent to callback
     * @see DynamicObjectBuilder#advertiseSignal(long, String)
     */
    public void post(String eventName, Object... args) {
        this.post(QiSerializer.getDefault(), eventName, args);
    }

    /**
     * Post an event advertised with advertiseEvent method.
     *
     * @param qiSerializer
     *            Serializer to use
     * @param eventName
     *            Name of the event to trigger.
     * @param args
     *            Arguments sent to callback
     * @see DynamicObjectBuilder#advertiseSignal(long, String)
     */
    public void post(QiSerializer qiSerializer, String eventName, Object... args) {
        final Object[] transformed;

        if (args == null) {
            transformed = null;
        }
        else {
            transformed = new Object[args.length];

            for (int index = args.length - 1; index >= 0; index--) {
                try {
                    transformed[index] = qiSerializer.serialize(args[index]);
                }
                catch (Exception exception) {
                    transformed[index] = args[index];
                }
            }
        }

        post(_p, eventName, transformed);
    }

    @Override
    public String toString() {
        return metaObjectToString(_p);
    }

    /**
     * Called by garbage collector Finalize is overriden to manually delete C++
     * data
     */
    @Override
    protected void finalize() throws Throwable {
        destroy(_p);
        super.finalize();
    }

    private static Method findSlot(Object annotatedSlotContainer, String slotName) {
        Class<?> clazz = annotatedSlotContainer.getClass();
        Method slot = null;
        for (Method method : clazz.getDeclaredMethods()) {
            QiSlot qiSlot = method.getAnnotation(QiSlot.class);

            if (qiSlot == null)
                // not a slot
                continue;

            String name = qiSlot.value();

            if (name.isEmpty())
                // no name defined in QiSlot, use the method name
                name = method.getName();

            if (slotName.equals(name)) {
                if (slot != null)
                    throw new QiSlotException("More than one slot with name \"" + slotName + "\" in " + clazz.getName());
                slot = method;
                // continue iteration to detect duplicated slot names
            }
        }
        return slot;
    }

    /**
     * Indicates if given object is equals to this AnyObject
     * @param object Object to compare with
     * @return {@code true} if given object is equals
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (null == object || !AnyObject.class.equals(object.getClass())) {
            return false;
        }

        return AnyObject.compare(this._p, ((AnyObject) object)._p) == 0;
    }

    /**
     * Compare this AnyObject with a given one.<br>
     * Comparison result is :
     * <table border=1>
     *  <tr><th>Comparison</th><th>Result</th></tr>
     *  <tr><td><center><b>this</b> &lt; anyObject</center></td><th>-1</th></tr>
     *  <tr><td><center><b>this</b> == anyObject</center></td><th>0</th></tr>
     *  <tr><td><center><b>this</b> &gt; anyObject</center></td><th>1</th></tr>
     * </table>
     * @param anyObject AnyObject to compare with
     * @return Comparison result
     */
    @Override
    public int compareTo(AnyObject anyObject) {
        return AnyObject.compare(this._p, anyObject._p);
    }

    /**
     * Returns a hash code value for this AnyObject.
     */
    @Override
    public int hashCode() {
        return AnyObject.hash(this._p);
    }
}
