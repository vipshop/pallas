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

import java.util.*;

import com.vip.pallas.mybatis.entity.SearchTemplate;
import junit.framework.TestCase;
import org.junit.Test;

import static com.vip.pallas.utils.TemplateParamsExtractUtil.getParams;
import static com.vip.pallas.utils.TemplateParamsExtractUtil.renderESTemplateBody;
import static com.vip.pallas.utils.TemplateParamsExtractUtil.walk;

/**
 * Created by owen on 5/6/2017.
 */
public class TemplateParamsExtractUtilTest extends TestCase {

    @Test
    public void testNormal () {

        String s = "{{#a}}{{a}}{{/a}}{{#b}}{{/b}}{{^c}}{{/c}}{{null}}";
        Map<String, Object> map = new HashMap<>();
        walk(s, 0, s.length()-1, "", map, null);
        assertEquals(map.size(), 3);
    }

    @Test
    public void testWithToJson () {
        String s = "{{#a}}{{#toJson}}a.list{{/toJson}}{{/a}}";
        Map<String, Object> map = new HashMap<>();
        walk(s, 0, s.length()-1, "", map, null);

        assertNotNull(((Map<String, Object>)map.get("a")).get("list"));

    }

    @Test
    public void testWithWrongEndTag () {
        String s = "{{#a}}{{#toJson}}a.list{{/toJson}}";
        Map<String, Object> map = new HashMap<>();
        walk(s, 0, s.length()-1, "", map, null);
        //System.out.println(map);
        assertTrue(map.containsKey("错误:a"));
    }

    @Test
    public void testListComplexObjects () {
        String s = "{{#a}}{{#a.list}}{{a.list.b}},{{a.list.c}}{{/a.list}}{{/a}}";
        Map<String, Object> map = new HashMap<>();
        Set<String> nestedObjects = new HashSet<>();
        walk(s, 0, s.length()-1, "", map, nestedObjects);
        //System.out.println(map);
        assertEquals(2, ((Map<String, List<Map>>)map.get("a")).get("list").get(0).size());
        assertEquals(2, nestedObjects.size());
    }

    @Test
    public void testRenderBody () {
        String s = "{{#a}}{{#a.list}}{{a.list.b}},{{a.list.c}}{{/a.list}}{{/a}}";
        String result = renderESTemplateBody(s, new LinkedList<>());
        assertEquals("{\n" +
                "\"template\":\"{{#a}}{{#a.list}}{{b}},{{c}}{{/a.list}}{{/a}}\"\n" +
                "}",
                result);
    }

    @Test
    public void testMultiSameTags () {
        String s = "{{#a}}{{#a}}{{/a}}{{#c}}{{/c}}{{/a}}";
        Map<String, Object> map = new HashMap<>();
        walk(s, 0, s.length()-1, "", map, null);
        assertEquals(map.size(), 2);
    }

    @Test
    public void testGetParams () {
        SearchTemplate t = new SearchTemplate();
        t.setTemplateName("test");
        t.setContent("{{#b}}{{/b}}{{^c}}{{/c}}");
        List<SearchTemplate> list = new LinkedList<>();
        list.add(t);
        String s = "{{#a}}{{a}}{{/a}}##__test__##{{null}}";
        Map<String, Object> map = getParams(s, list);
        assertEquals(map.size(), 3);
    }
}