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

import java.util.Arrays;
import java.util.List;

import org.junit.AfterClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.http.HttpStatus;

import com.alibaba.fastjson.JSONObject;
import com.vip.pallas.console.vo.PageResultVO;
import com.vip.pallas.console.vo.ResultVO;
import com.vip.pallas.console.vo.UserVO;
import com.vip.pallas.test.base.BaseSpringEsTest;
import com.vip.pallas.utils.JsonUtil;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UserRoleControllerTest extends BaseSpringEsTest {

    private static String keywords = "user_name_123";
    private static Long id = null;
    @Test
    public void test1UdpateUser() throws Exception{
        UserVO userVO = new UserVO();
        userVO.setEmail("");
        userVO.setUsername(keywords);
        userVO.setPassword("password");
        userVO.setRealName("real_name");
        userVO.setRoleNames(Arrays.asList("Guest"));

        assertThat(callRestApi("/authorization/user/update.json", JsonUtil.toJson(userVO))).isNull();

    }

    @Test
    public void test2Page() throws Exception {
        ResultVO<PageResultVO> resultVO =  callGetApi("/authorization/user/page.json?currentPage=1&pageSize=10&keywords=" + keywords, PageResultVO.class);
        assertThat(resultVO.getStatus()).isEqualTo(HttpStatus.OK.value());
        PageResultVO pageResultVO = resultVO.getData();
        assertThat(pageResultVO).isNotNull();
        List<JSONObject> jsonObjects =  pageResultVO.getList();
        assertThat(jsonObjects).isNotNull();
        assertThat(jsonObjects.size()).isEqualTo(1);
        id = jsonObjects.get(0).getLong("id");

    }

    @Test
    public void test3Role() throws Exception{
        ResultVO<PageResultVO> resultVO =  callGetApi("/authorization/role/page.json?keywords=" + keywords, PageResultVO.class);
        assertThat(resultVO.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @AfterClass
    public static void cleanData() throws Exception {
        assertThat(callGetApi("/authorization/user/delete/" + id + ".json")).isNull();
    }
}