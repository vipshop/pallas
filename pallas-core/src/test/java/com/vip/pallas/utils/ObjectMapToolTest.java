/**
 * Copyright 2019 vip.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.vip.pallas.utils;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.*;

import static com.vip.pallas.utils.ObjectMapTool.*;

/**
 * Created by owen on 7/7/2017.
 */
public class ObjectMapToolTest extends TestCase {

    @Test
    public void testParseValues() {

        assertEquals(Integer.valueOf(1), parseInt("1"));

        assertEquals(null, parseInt("a"));

        assertEquals(Float.valueOf("1.1"), parseFloat("1.1"));

        assertEquals(null, parseFloat("1.1a"));

        assertEquals(Double.valueOf("1.2"), parseDouble("1.2"));

        assertEquals(null, parseDouble("1.1a"));

        assertEquals(Long.valueOf(1l), parseLong("1"));

        assertEquals(null, parseLong("1.1a"));
    }

    @Test
    public void testIs() {

        assertTrue(isEmpty((Object)null));

        assertTrue(isEmpty((String)null));

        assertFalse(isEmpty(new Object()));

        assertFalse(isEmpty("a"));

        assertFalse(isEmpty((Object)"a"));

        assertTrue(isEmpty((Object[])null));

        Object[] objs = new Object[1];
        objs[0] = "1";
        assertFalse(isEmpty(objs));

        ArrayList<String> list = new ArrayList<>();
        list.add("a");

        assertTrue(isNotEmpty(list));

        assertTrue(isNullString("null"));

    }

    @Test
    public void testRemoveNullItem() {
        Map<String, String> map = new HashMap<>();
        map.put("1","1");
        map.put("2", null);

        removeNullItem(map);

        assertEquals(1, map.size());

    }

    @Test
    public void testGetDate() {
        Map<String, String> map = new HashMap<>();
        map.put("date","2017-01-01 11:11:11");
        Date date = getDate(map, "date");
        assertEquals(117, date.getYear());

        Long l =  getDateMs(map, "date");
        assertEquals(Long.valueOf("1483240271000"), l);
    }

    @Test
    public void testGetLong() {
        Map<String, String> map = new HashMap<>();
        map.put("long", "1");
        assertEquals(Long.valueOf("1"), getLong(map, "long"));
    }

    @Test
    public void testGetLongList() {
        Map<String, String> map = new HashMap<>();
        map.put("long", "[1,2,3]");
        List<Long> list = getLongList(map, "long");
        assertEquals(Long.valueOf("3"), list.get(2));
    }

    @Test
    public void testString() {
        Map<String, String> map = new HashMap<>();
        map.put("string", "1");
        assertEquals("1", getString(map, "string"));
        map.put("stringList", "[1,2,3]");
        List<String> list = getStringList(map, "stringList");
        assertEquals("3", list.get(2));

        Set<String> set = getStringSet(map, "stringList");
        assertEquals(3, set.size());
    }

    @Test
    public void testDouble() {
        Map<String, String> map = new HashMap<>();
        map.put("double", "1.1");
        assertEquals(Double.valueOf("1.1"), getDouble(map, "double"));

        map.put("doubleList", "[1.1,1.2]");
        assertEquals(Double.valueOf("1.2"), getDoubleList(map, "doubleList").get(1));
    }

    @Test
    public void testFloat() {
        Map<String, String> map = new HashMap<>();
        map.put("float", "1.1");
        assertEquals(Float.valueOf("1.1"), getFloat(map, "float"));
    }

    @Test
    public void testGetInteger() {
        Map<String, String> map = new HashMap<>();
        map.put("int", "1");
        assertEquals(Integer.valueOf("1"), getInteger(map, "int"));

        assertEquals(Integer.valueOf("2"), getInteger(map, "int2", 2));

        map.put("intList", "[1,2,3]");
        assertEquals(Integer.valueOf("3"), getIntegerList(map, "intList").get(2));
    }

    @Test
    public void testGetBoolean() {
        Map<String, String> map = new HashMap<>();
        map.put("bool", "true");
        assertTrue(getBoolean(map, "bool"));

        assertFalse(getBoolean(map, "bool-not-exist"));
    }

    @Test
    public void testGetByte() {
        Map<String, String> map = new HashMap<>();
        map.put("byte", "1");
        assertEquals(Byte.valueOf("1"), getByte(map, "byte"));

        assertNull(getByte(map, "byte-not-exist"));
    }

    @Test
    public void testGetShort() {
        Map<String, String> map = new HashMap<>();
        map.put("short", "1");
        assertEquals(Short.valueOf("1"), getShort(map, "short"));

        assertNull(getShort(map, "short-not-exist"));
    }

    @Test
    public void testGetObject() throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("obj", "1");
        assertEquals("1", getObject(map, "obj", String.class));

        assertNull(getObject(map, "obj-not-exist", String.class));
    }




}