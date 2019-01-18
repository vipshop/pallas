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

import static com.vip.pallas.mybatis.entity.SearchTemplate.TYPE_MACRO;
import static com.vip.pallas.mybatis.entity.SearchTemplate.TYPE_TEMPLATE;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vip.pallas.bean.ApproveState;
import com.vip.pallas.exception.PallasException;
import com.vip.pallas.mybatis.entity.Approve;
import com.vip.pallas.mybatis.entity.Page;
import com.vip.pallas.mybatis.entity.SearchTemplate;
import com.vip.pallas.mybatis.repository.ApproveRepository;
import com.vip.pallas.service.ApproveService;
import com.vip.pallas.service.SearchTemplateService;


@Service
@Transactional(rollbackFor=Exception.class)
public class ApproveServiceImpl implements ApproveService {

    @Resource
    private SearchTemplateService templateService;

    @Resource
    private ApproveRepository approveRepository;

    @Override
    public void insert(Approve approve) throws PallasException {
        approveRepository.insert(approve);
    }

    @Override
    public void update(Approve approve) throws PallasException {
        approveRepository.updateByPrimaryKeySelective(approve);
    }

    @Override
    public Approve findById(Long id) {
        return approveRepository.selectByPrimaryKey(id);
    }

    @Override
    public void deleteById(Long id) throws PallasException {
        approveRepository.deleteByPrimaryKey(id);
    }

    @Override
    public List<Approve> findAll() {
        return approveRepository.selectAll();
    }

    @Override
    public List<Approve> findApplyPage(Page<Approve> page, String applyUser, Integer state, String conditions, String clusterId) {
        Map<String, Object> params = new HashMap<>();
        params.put("applyUser", applyUser);
        if(state != null){
			params.put("state", String.valueOf(state));
		}
        params.put("conditions", conditions);
        params.put("clusterId", clusterId);
        page.setParams(params);

        return approveRepository.selectApplyPage(page);
    }

    @Override
    public List<Approve> findApprovePage(Page<Approve> page, Integer state, String conditions, String clusterId) {
        Map<String, Object> params = new HashMap<String, Object>();
		if(state != null){
			params.put("state", String.valueOf(state));
		}
        params.put("conditions", conditions);
        params.put("clusterId", clusterId);
        page.setParams(params);

        return approveRepository.selectApprovePage(page);
    }

    @Override
    public void approve(Long id, String state, String note, String approveUser) throws Exception {
        Approve approve = approveRepository.selectByPrimaryKey(id);
        if (approve.getApproveState() == ApproveState.PENDING_APPROVE.getValue()){
            if(Integer.parseInt(state) == ApproveState.ON_LINEED.getValue()){
                templateService.updateAfterApprove(approve.getApplyUser(), approve.getNote(), templateService.findById(approve.getRelateId()));
            }
            Date nowDate = new Date();
            approve.setApproveState(Byte.valueOf(state));
            approve.setApproveOpinion(note);
            approve.setApproveTime(nowDate);
            approve.setApproveUser(approveUser);
            approve.setUpdateTime(nowDate);
            this.update(approve);
        }else {
            throw new PallasException("模板【" + id + "】不处于待审核状态");
        }
    }

    @Override
    public boolean isInApprove(Long templateId){
        if(approveRepository.getCountByTemplateIdAndState(templateId, ApproveState.PENDING_APPROVE.getValue()) > 0){
            return true;
        }

        SearchTemplate searchTemplate = templateService.findById(templateId);

        if(searchTemplate.getType() == TYPE_TEMPLATE){
            List<SearchTemplate> allMacroByTemplateId = templateService.findAllMacroByTemplateId(templateId);
            if(allMacroByTemplateId != null){
                return allMacroByTemplateId.stream()
                        .anyMatch((SearchTemplate x) -> approveRepository.getCountByTemplateIdAndState(x.getId(), ApproveState.PENDING_APPROVE.getValue()) > 0);
            }
        }else if(searchTemplate.getType() == TYPE_MACRO){
            List<SearchTemplate> allTemplateByMacroId = templateService.findAllTemplateByMacroId(templateId);
            if(allTemplateByMacroId != null){
                return allTemplateByMacroId.stream()
                        .anyMatch((SearchTemplate x) -> approveRepository.getCountByTemplateIdAndState(x.getId(), ApproveState.PENDING_APPROVE.getValue()) > 0);
            }
        }

        return false;
    }
}