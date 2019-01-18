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

package com.vip.pallas.mybatis.interceptor;

import com.vip.pallas.mybatis.entity.Page;
import com.vip.pallas.mybatis.interceptor.PageInterceptor;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by owen on 12/7/2017.
 */
public class PageInterceptorTest extends TestCase {

    private PageInterceptor interceptor = new PageInterceptor();

    @Test
    public void testDataType() {
        interceptor.setDatabaseType("mysql");
        assertEquals("mysql", interceptor.getDatabaseType());

        try{
            interceptor.setDatabaseType("postgresql");
        } catch (PageInterceptor.PageNotSupportException e) {
            assertTrue(e.getMessage().contains("Page not support for the type of database,"));
        }

    }

    @Test
    public void testExtractRealParameterObject() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = interceptor.getClass().getDeclaredMethod("extractRealParameterObject", Object.class);
        method.setAccessible(true);

        Map<String, String> map = new HashMap<>();
        map.put("0", "0");
        map.put("1", "1");
        Object obj = method.invoke(interceptor, map);
        assertEquals("0",obj.toString());
    }

    @Test
    public void testBuildOraclePageSql() {
        Page page = new Page();
        page.setPageNo(2);
        page.setPageSize(10);
        String res = interceptor.buildOraclePageSql(page, "test_table");
        assertEquals("select * from (select u.*, rownum r from (test_table) u where rownum < 21) where r >= 11", res);
    }


}