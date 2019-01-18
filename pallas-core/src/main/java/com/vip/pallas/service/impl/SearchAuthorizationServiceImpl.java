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

package com.vip.pallas.service.impl;

import com.vip.pallas.mybatis.entity.SearchAuthorization;
import com.vip.pallas.mybatis.repository.SearchAuthorizationRepository;
import com.vip.pallas.service.SearchAuthorizationService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * Created by owen on 08/01/2018.
 */
@Service
public class SearchAuthorizationServiceImpl implements SearchAuthorizationService {

    @Resource
    private SearchAuthorizationRepository authRepo;

    @Override
    public void addOrUpdateAuthorization(SearchAuthorization authorization) {
        if (authorization.getId() == null) {
            authorization.setCreateTime(new Date());
            authRepo.insert(authorization);
        } else {
            authRepo.update(authorization);
        }
    }

    @Override
    public void setEnabled(Long id, boolean enabled) {
        authRepo.setEnable(id, enabled);
    }

    @Override
    public List<SearchAuthorization> selectAll() {
        return authRepo.selectAll();
    }

    @Override
    public void deleteById(Long id) {
        authRepo.delete(id);
    }

    @Override
    public SearchAuthorization findByToken(String token) {
        return authRepo.findByToken(token);
    }

    @Override
    public SearchAuthorization findById(Long id) {
        return authRepo.findById(id);
    }
}