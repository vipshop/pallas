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

package com.vip.pallas.service;

import com.vip.pallas.exception.PallasException;
import com.vip.pallas.mybatis.entity.Approve;
import com.vip.pallas.mybatis.entity.Page;

import java.util.List;

public interface ApproveService {

    void insert(Approve approve) throws PallasException;

    void update(Approve approve) throws PallasException;

    Approve findById(Long id);

    void deleteById(Long id) throws PallasException;

    List<Approve> findAll();

    List<Approve> findApplyPage(Page<Approve> page, String applyUser, Integer state, String conditions, String clusterId);

    List<Approve> findApprovePage(Page<Approve> page, Integer state, String conditions, String clusterId);

    void approve(Long id, String state, String note, String approveUser) throws Exception;

    boolean isInApprove(Long templateId);
}