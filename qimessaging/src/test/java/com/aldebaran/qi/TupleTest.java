/*
**  Copyright (C) 2015 Aldebaran Robotics
**  See COPYING for the license
*/
package com.aldebaran.qi;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class TupleTest {
    public AnyObject proxy = null;
    public AnyObject obj = null;
    public Session s = null;
    public Session client = null;
    public ServiceDirectory sd = null;

    @Before
    public void setUp() throws Exception {
        sd = new ServiceDirectory();
        s = new Session();
        client = new Session();

        // Get Service directory listening url.
        String url = sd.listenUrl();

        // Create new QiMessaging generic object
        DynamicObjectBuilder ob = new DynamicObjectBuilder();

        // Get instance of ReplyService
        QiService reply = new ReplyService();

        // Register event 'Fire'
        ob.advertiseSignal("fire::(i)");
        ob.advertiseMethod("reply::s(s)", reply, "Concatenate given argument with 'bim !'");
        ob.advertiseMethod("answer::s()", reply, "Return given argument");
        ob.advertiseMethod("add::i(iii)", reply, "Return sum of arguments");
        ob.advertiseMethod("info::(sib)(sib)", reply, "Return a tuple containing given arguments");
        ob.advertiseMethod("answer::i(i)", reply, "Return given parameter plus 1");
        ob.advertiseMethod("answerFloat::f(f)", reply, "Return given parameter plus 1");
        ob.advertiseMethod("answerBool::b(b)", reply, "Flip given parameter and return it");
        ob.advertiseMethod("abacus::{ib}({ib})", reply, "Flip all booleans in map");
        ob.advertiseMethod("echoFloatList::[m]([f])", reply, "Return the exact same list");
        ob.advertiseMethod("createObject::o()", reply, "Return a test object");

        // Connect session to Service Directory
        s.connect(url).sync();

        // Register service as serviceTest
        obj = ob.object();
        assertTrue("Service must be registered", s.registerService("serviceTest", obj) > 0);

        // Connect client session to service directory
        client.connect(url).sync();

        // Get a proxy to serviceTest
        proxy = client.service("serviceTest").get();
        assertNotNull(proxy);
    }

    @After
    public void tearDown() {
        obj = null;
        proxy = null;

        s.close();
        client.close();

        s = null;
        client = null;
        sd = null;
    }

    @Test
    public void testOutOfBoundException() {
        Tuple tuple = new Tuple(0);
        boolean exceptionRaised = false;

        try {
            tuple.get(4);
        } catch (Exception e) {
            exceptionRaised = true;
            System.out.println("Exception catched : " + e.getMessage());
        }

        assertTrue("Exception must have been thrown", exceptionRaised);
    }

    @Test
    public void testTuple() {
        Tuple tuple = new Tuple(3);
        String str = new String("plafbim");
        Integer i = new Integer(42);
        Boolean b = new Boolean(true);

        try {
            tuple.<String>set(0, str);
        } catch (Exception e) {
            fail("Exception must not be thrown (string)");
        }

        try {
            tuple.<Integer>set(1, i);
        } catch (Exception e) {
            fail("Exception must not be thrown (integer)");
        }

        try {
            tuple.<Boolean>set(2, b);
        } catch (Exception e) {
            fail("Exception must not be thrown (boolean)");
        }

    }

    @Test
    public void testValue() {
        Tuple tuple = Tuple.of("42", 42, true);
        String str = null;
        Integer i = null;
        Boolean b = null;

        try {
            str = (String) tuple.get(0);
        } catch (Exception e) {
            fail("Exception must not be thrown : " + e.getMessage());
        }

        assertEquals("42", str);

        try {
            i = (Integer) tuple.get(1);
        } catch (Exception e) {
            fail("Exception must not be thrown : " + e.getMessage());
        }

        assertEquals(42, i.intValue());

        try {
            b = (Boolean) tuple.get(2);
        } catch (Exception e) {
            fail("Exception must not be thrown : " + e.getMessage());
        }

        assertTrue("Boolean value must be true : " + b, b);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIndexOutOfBoundsException() throws IndexOutOfBoundsException, ClassCastException, IllegalArgumentException, IllegalAccessException {
        Tuple tuple = Tuple.of("42", 42, null);

        tuple.get(42);
    }


    @Test(expected = ClassCastException.class)
    public void testClassCastException() throws IndexOutOfBoundsException, ClassCastException, IllegalArgumentException, IllegalAccessException {
        Tuple tuple = Tuple.of("42", 42, new HashMap<Integer, String>());

        @SuppressWarnings("unused")
        ReplyService tmp = (ReplyService) tuple.get(0);
    }

    @Test
    public void testCallTuple() {

        Tuple ret = null;
        try {
            ret = proxy.<Tuple>call("info", "42", 42, true).get();
        } catch (Exception e) {
            fail("Call must be successful : " + e.getMessage());
        }

        String str = null;
        Integer i = null;
        Boolean b = null;

        try {
            str = (String) ret.get(0);
        } catch (Exception e) {
            fail("Exception must not be thrown : " + e.getMessage());
        }

        try {
            i = (Integer) ret.get(1);
        } catch (Exception e) {
            fail("Exception must not be thrown : " + e.getMessage());
        }

        try {
            b = (Boolean) ret.get(2);
        } catch (Exception e) {
            fail("Exception must not be thrown : " + e.getMessage());
        }

        assertEquals("42", str);
        assertEquals(42, i.intValue());
        assertTrue(b);
    }

}
