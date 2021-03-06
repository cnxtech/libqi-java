package com.aldebaran.qi;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class PropertyTest {

    private <T> void constructWithAValue(T value) {
        Property<T> prop = new Property<T>(value);
        try {
            assertEquals(value, prop.getValue().get());
        } catch (ExecutionException e) {
            fail(e.getMessage());
        }
    }

    private <T> void constructWithAValueAndClass(Class<T> cls, T value) {
        Property<T> prop = new Property<T>(cls, value);
        try {
            assertEquals(value, prop.getValue().get());
        } catch (ExecutionException e) {
            fail(e.getMessage());
        }
    }

    private void assertThrowsNullPointerException(Function<Void, Void> function) {
        try {
            function.execute(null);
        }
        catch (NullPointerException ex) {
            // Success.
        }
        catch (Throwable throwable) {
            fail("The call threw an exception that was not " +
                    "NullPointerException: " + throwable.getMessage());
        }
    }

    @Test
    public void constructedWithNullValueThrows() {
        assertThrowsNullPointerException(new Function<Void, Void>() {
            @Override
            public Void execute(Void v) throws Throwable {
                new Property<Integer>((Integer)null);
                return null;
            }
        });
    }

    @Test
    public void constructedWithNullClassThrows() {
        assertThrowsNullPointerException(new Function<Void, Void>() {
            @Override
            public Void execute(Void v) throws Throwable {
                new Property<Integer>((Class<Integer>) null);
                return null;
            }
        });
    }

    @Test
    public void constructedWithNullValueOrClassThrows() {
        final Integer value = 42;
        final Class<Integer> cls = (Class<Integer>)value.getClass();

        assertThrowsNullPointerException(new Function<Void, Void>() {
            @Override
            public Void execute(Void v) {
                new Property<Integer>((Class<Integer>) null, value);
                return null;
            }
        });
        assertThrowsNullPointerException(new Function<Void, Void>() {
            @Override
            public Void execute(Void v) {
                new Property<Integer>(cls, null);
                return null;
            }
        });
    }

    @Test
    public void constructedWithAStringValueSucceeds() {
        String value = "Hello world !";
        constructWithAValue(value);
        //noinspection unchecked
        constructWithAValueAndClass((Class<String>)value.getClass(),
                value);
    }

    @Test
    public void constructedWithAIntegerValueSucceeds() {
        Integer value = 42;
        constructWithAValue(value);
        //noinspection unchecked
        constructWithAValueAndClass((Class<Integer>)value.getClass(),
                value);
    }

    @Test
    public void constructedWithABooleanValueSucceeds() {
        Boolean value = true;
        constructWithAValue(value);
        //noinspection unchecked
        constructWithAValueAndClass((Class<Boolean>)value.getClass(),
                value);
    }

    @Test
    public void constructedWithADoubleValueSucceeds() {
        Double value = 3.14;
        constructWithAValue(value);
        //noinspection unchecked
        constructWithAValueAndClass((Class<Double>)value.getClass(),
                value);
    }

    @Test
    public void constructedWithAFloatValueSucceeds() {
        Float value = 3.14f;
        constructWithAValue(value);
        //noinspection unchecked
        constructWithAValueAndClass((Class<Float>)value.getClass(),
                value);
    }

    @Test
    public void constructedWithAListValueSucceeds() {
        List value = Arrays.asList("This is", "an array", "of values");
        constructWithAValue(value);
        //noinspection unchecked
        constructWithAValueAndClass((Class<List>)value.getClass(),
                value);
    }

    @Test
    public void constructedWithAMapValueSucceds() {
        HashMap<Integer, String> value = new HashMap<Integer, String>();
        value.put(42, "forty two");
        value.put(13, "thirteen");
        value.put(9000, "way too many");
        constructWithAValue(value);

        //noinspection unchecked
        constructWithAValueAndClass((Class<HashMap>)value.getClass(),
                value);
    }

    @Test
    public void constructedWithATupleValueSucceeds() {
        Tuple value = Tuple.of(42, "zwei und vierzig", true, 42.0);
        constructWithAValue(value);
        //noinspection unchecked
        constructWithAValueAndClass((Class<Tuple>)value.getClass(),
                value);
    }

    @Test
    public void setPropertyWithNullValueThrows() {
        final Property<Integer> prop = new Property<Integer>(42);
        assertThrowsNullPointerException(new Function<Void, Void>() {
            @Override
            public Void execute(Void v) {
                prop.setValue(null);
                return null;
            }
        });
    }
}
