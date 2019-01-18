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

package com.vip.pallas.mybatis.repository;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.vip.pallas.mybatis.entity.User;
import com.vip.pallas.mybatis.entity.ext.UserExtRoles;

@Repository
public interface UserRepository{

  	int deleteByPrimaryKey(Long id);
	
	User selectByPrimaryKey(Long id);
	
    int updateByPrimaryKey(User user);

    int updateByPrimaryKeySelective(User user);

  	int insert(User user);
  	
	int insertSelective(User user);

	User selectById(Long id);
	
    User selectByUsername(@Param("username") String username);
    
    int selectCountBykeywords(@Param("keywords") String keywords);
    
	List<UserExtRoles> selectUserBykeywords(@Param("keywords") String keywords, @Param("offset") int offset,
			@Param("pageSize") int pageSize);
}