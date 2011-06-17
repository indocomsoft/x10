/*
 *  This file is part of the X10 project (http://x10-lang.org).
 *
 *  This file is licensed to You under the Eclipse Public License (EPL);
 *  You may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      http://www.opensource.org/licenses/eclipse-1.0.php
 *
 *  (C) Copyright IBM Corporation 2006-2011.
 */

package x10.x10rt;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeserializationDispatcher {

    public static final int NULL_ID = 0;
    public static final int STRING_ID = 1;
    public static final int FLOAT_ID = 2;
    public static final int DOUBLE_ID = 3;
    public static final int INTEGER_ID = 4;
    public static final int BOOLEAN_ID = 5;
    public static final int BYTE_ID = 6;
    public static final int SHORT_ID = 7;
    public static final int LONG_ID = 8;
    public static final int CHARACTER_ID = 9;
    public static final String NULL_VALUE = "__NULL__";

    private static final int INCREMENT_SIZE = 10;

    // Should start issuing id's from 1 cause the id 0 is used to indicate a null value.
    // We first increment i before issuing the id hence initialize to NULL_ID
    private static int i = NULL_ID;

    private static List<String> idToClassName = new ArrayList<String>();
    private static Map<String, Integer> classNameToId = new HashMap<String, Integer>();
//    private static Map<Integer, X10JavaSerializable> idToInstance = new HashMap<Integer, X10JavaSerializable>();

    public static int addDispatcher(String className) {
        if (i == NULL_ID) {
            idToClassName.add(NULL_ID, NULL_VALUE);
            add("java.lang.String");
            add("java.lang.Float");
            add("java.lang.Double");
            add("java.lang.Integer");
            add("java.lang.Boolean");
            add("java.lang.Byte");
            add("java.lang.Short");
            add("java.lang.Character");
        }
        i++;
        idToClassName.add(i, className);
        return i;
    }

    private static void add(String str) {
        i++;
        classNameToId.put(str, i);
        idToClassName.add(i, str);
    }

    public static Object getInstanceForId(int i, X10JavaDeserializer deserializer) throws IOException {

        if (i == X10JavaDeserializer.ref) {
            return deserializer.getObjectAtPosition(deserializer.readInt());
        } else if (i <=8) {
            Object obj = null;
            switch(i) {
                case NULL_ID:
                    return null;
                case STRING_ID:
                     obj =  deserializer.readStringValue();
                    break;
                case FLOAT_ID:
                     obj = deserializer.readFloat();
                    break;
                case DOUBLE_ID:
                     obj = deserializer.readDouble();
                    break;
                case INTEGER_ID:
                      obj = deserializer.readInt();
                    break;
                case BOOLEAN_ID:
                     obj = deserializer.readBoolean();
                    break;
                case BYTE_ID:
                      obj = deserializer.readByte();
                    break;
                case SHORT_ID:
                    obj = deserializer.readShort();
                    break;
                case  CHARACTER_ID:
                     obj = deserializer.readChar();
                    break;
                case LONG_ID:
                     obj = deserializer.readLong();
                    break;
            }
            deserializer.record_reference(obj);
            return obj;
        }

        final String className = idToClassName.get(i);
        try {
            Class<?> clazz = Class.forName(className);
            Method method = clazz.getMethod("_deserializer", X10JavaDeserializer.class);
            return method.invoke(null, deserializer);
        } catch (ClassNotFoundException e) {
            // This should never happen
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            // This should never happen
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            // This should never happen
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            // This should never happen
            throw new RuntimeException(e);
        }
    }

    public static String getClassNameForID(int id) {
        return idToClassName.get(id);
    }

    public static int getIDForClassName(String str) {
        return classNameToId.get(str);
    }
}
