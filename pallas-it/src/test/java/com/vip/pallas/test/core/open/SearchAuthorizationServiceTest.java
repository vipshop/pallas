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

package com.vip.pallas.test.core.open;

import static com.vip.pallas.mybatis.entity.SearchAuthorization.AUTHORIZATION_CAT_CLUSTERALL;
import static com.vip.pallas.mybatis.entity.SearchAuthorization.AUTHORIZATION_CAT_INDEXALL;
import static com.vip.pallas.mybatis.entity.SearchAuthorization.AUTHORIZATION_PRIVILEGE_READONLY;
import static com.vip.pallas.mybatis.entity.SearchAuthorization.AUTHORIZATION_PRIVILEGE_WRITE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Resource;

import org.junit.Test;

import com.vip.pallas.mybatis.entity.SearchAuthorization;
import com.vip.pallas.service.SearchAuthorizationService;
import com.vip.pallas.test.base.BaseSpringEsTest;

/**
 * Created by owen on 08/01/2018.
 */
public class SearchAuthorizationServiceTest extends BaseSpringEsTest {

    @Resource
    private SearchAuthorizationService service;

    @Test
    public void testInsert() throws Exception {

        service.selectAll().stream().filter(t -> t.getTitle().equals("ForUT")).map(t -> t.getId()).forEach(service::deleteById);

        SearchAuthorization auth = new SearchAuthorization();
        auth.setEnabled(true);
        auth.setClientToken("AAAAAAABBBBBBBCCCCCCCDDDDDDEEEEEE====");
        auth.setTitle("ForUT");

        //cluster privilege
        SearchAuthorization.AuthorizationItem item = new SearchAuthorization.AuthorizationItem();
        item.setId(1L);
        item.setName("testCluster");
        Map<String, List<String>> privileges = new HashMap<>();
        privileges.put(AUTHORIZATION_CAT_CLUSTERALL, toList(AUTHORIZATION_PRIVILEGE_READONLY, AUTHORIZATION_PRIVILEGE_WRITE));
        item.setPrivileges(privileges);


        //index1 privilege
        SearchAuthorization.AuthorizationItem item1 = new SearchAuthorization.AuthorizationItem();
        item1.setId(10001L);
        item1.setName("index-1");
        privileges = new HashMap<>();
        privileges.put(AUTHORIZATION_CAT_INDEXALL, toList(AUTHORIZATION_PRIVILEGE_READONLY, AUTHORIZATION_PRIVILEGE_WRITE));
        item1.setPrivileges(privileges);

        SearchAuthorization.AuthorizationItem item2 = new SearchAuthorization.AuthorizationItem();
        item2.setId(10002L);
        item2.setName("index-2");
        privileges = new HashMap<>();
        privileges.put(AUTHORIZATION_CAT_INDEXALL, toList(AUTHORIZATION_PRIVILEGE_READONLY));
        item2.setPrivileges(privileges);
        item.setIndexPrivileges(toList(item1, item2));

        auth.setAuthorizationItems(SearchAuthorization.toXContent(toList(item)));
        service.addOrUpdateAuthorization(auth);
    }

    private <T> List<T> toList(T... t) {
        return Stream.of(t).collect(Collectors.toList());
    }


    @Test
    public void testSetEnable() throws Exception {
    	testInsert();
        SearchAuthorization au = service.selectAll().stream().filter(t -> t.getTitle().equals("ForUT")).findFirst().get();
        service.setEnabled(au.getId(), false);
        au = service.selectAll().stream().filter(t -> t.getTitle().equals("ForUT")).findFirst().get();
        assertFalse(au.isEnabled());
        service.setEnabled(au.getId(), true);
        au = service.selectAll().stream().filter(t -> t.getTitle().equals("ForUT")).findFirst().get();
        assertTrue(au.isEnabled());
    }

    @Test
    public void testUpdate() throws Exception {
    	testInsert();
        SearchAuthorization au = service.selectAll().stream().filter(t -> t.getTitle().equals("ForUT")).findFirst().get();
        au.setClientToken("kkkkkkkk");
        service.addOrUpdateAuthorization(au);
        au = service.selectAll().stream().filter(t -> t.getTitle().equals("ForUT")).findFirst().get();
        assertEquals(au.getClientToken(), "kkkkkkkk");
    }

    @Test
    public void testDelete() throws Exception {
    	testInsert();
        SearchAuthorization au = service.selectAll().stream().filter(t -> t.getTitle().equals("ForUT")).findFirst().get();
        service.deleteById(au.getId());
    }


}