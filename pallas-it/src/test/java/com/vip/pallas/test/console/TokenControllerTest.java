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

package com.vip.pallas.test.console;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.vip.pallas.console.vo.ResultVO;
import com.vip.pallas.mybatis.entity.SearchAuthorization;
import com.vip.pallas.service.SearchAuthorizationService;
import com.vip.pallas.test.base.BaseSpringEsTest;
import com.vip.pallas.utils.JsonUtil;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TokenControllerTest extends BaseSpringEsTest {

    private static String tokenString = null;
    private static SearchAuthorization token = null;

    private static SearchAuthorizationService searchAuthorizationService;

    @Test
    public void test1GenerateToken() throws Exception{
        String responseBody = callGetApiAsString("/token/security/token.json");
        assertThat(responseBody).isNotEmpty();
        JSONObject jsonObject = JSON.parseObject(responseBody);
        tokenString = jsonObject.getString("data");
        assertThat(tokenString).isNotEmpty();
    }

    @Test
    public void test2AddToekn() throws Exception{
        token = new SearchAuthorization();
        token.setClientToken(tokenString);
        token.setTitle("this is test token");
        token.setEnabled(true);

        assertThat(callRestApi("/token/insert.json", JsonUtil.toJson(token))).isNull();

    }

    @Test
    public void test3List() throws Exception{
        ResultVO<List> resultVO =  callGetApi("/token/list.json", List.class);
        assertThat(resultVO).isNotNull();
        assertThat(resultVO.getData()).isNotNull();
    }

//    @Test
//    public void test4GetTokenPrivilege() throws Exception {
//        SearchAuthorization searchAuthorization = searchAuthorizationService.findByToken(tokenString);
//        assertThat(searchAuthorization).isNotNull();
//        token.setId(searchAuthorization.getId());
//
//        ResultVO<List> resultVO =  callGetApi("/token/token_privileges.json?id=" + token.getId(), List.class);
//        assertThat(resultVO.getStatus()).isEqualTo(HttpStatus.OK.value());
//    }
}