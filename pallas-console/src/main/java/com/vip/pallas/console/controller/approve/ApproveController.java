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

package com.vip.pallas.console.controller.approve;

import com.vip.pallas.bean.ApproveState;
import com.vip.pallas.bean.IndexOperationEventName;
import com.vip.pallas.bean.IndexOperationEventType;
import com.vip.pallas.console.utils.AuditLogUtil;
import com.vip.pallas.console.utils.SessionUtil;
import com.vip.pallas.console.utils.AuthorizeUtil;
import com.vip.pallas.console.vo.ApproveOp;
import com.vip.pallas.console.vo.PageResultVO;
import com.vip.pallas.exception.BusinessLevelException;
import com.vip.pallas.exception.PallasException;
import com.vip.pallas.mybatis.entity.Approve;
import com.vip.pallas.mybatis.entity.IndexOperation;
import com.vip.pallas.mybatis.entity.Page;
import com.vip.pallas.mybatis.entity.SearchTemplate;
import com.vip.pallas.service.ApproveService;
import com.vip.pallas.service.IndexOperationService;
import com.vip.pallas.service.IndexService;
import com.vip.pallas.service.SearchTemplateService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;
import java.util.*;

@Validated
@RestController
@RequestMapping("/approve")
public class ApproveController{
    private static Logger logger = LoggerFactory.getLogger(ApproveController.class);

    @Autowired
    private ApproveService approveService;

    @Autowired
    private IndexOperationService indexOperationService;

    @Resource
    private SearchTemplateService templateService;

    @Resource
    private IndexService indexService;

    @RequestMapping(path = "/apply/page.json", method = {RequestMethod.GET, RequestMethod.POST})
	public PageResultVO<Approve> queryPage(HttpServletRequest req,
			@RequestParam(required = false, defaultValue = "1") @Min(value = 0, message = "currentPage必须为正数") Integer currentPage,
			@RequestParam(required = false, defaultValue = "10") @Min(value = 0, message = "pageSize必须为正数") Integer pageSize,
			@RequestParam(required = false) Integer state, @RequestParam(required = false) String conditions,
			@RequestParam(required = false) String clusterId) {

        Page<Approve> page = new Page<>();
        page.setPageNo(currentPage);
        page.setPageSize(pageSize);

        PageResultVO<Approve> resultVO = new PageResultVO<>();

        String currentUser = SessionUtil.getLoginUser(req);

        List<Approve> list = approveService.findApplyPage(page, currentUser, state, conditions, clusterId);
        List<String> privileges = AuthorizeUtil.loadPrivileges();
        if(privileges != null && (privileges.contains("template.approve") || privileges.contains("template.all"))){
            resultVO.setAllPrivilege(true);
        }

        resultVO.setList(list);
        resultVO.setTotal(page.getTotalRecord());
        resultVO.setPageCount(page.getTotalPage());

        return resultVO;
    }

    @RequestMapping(path = "/apply/cancel.json", method = RequestMethod.POST)
    public void cancel(@RequestBody ApproveOp params, HttpServletRequest req)throws Exception{
        Long id =  params.getId();
        if(id == null) {
            throw new BusinessLevelException(500, "id不能为空");
        }
        Approve approve = approveService.findById(id);
        if (null == approve) {
        	throw new BusinessLevelException(500, "approve不存在");
        }
        
        if(approve.getApproveState() != (byte) ApproveState.PENDING_APPROVE.getValue()){
            throw new PallasException("模板【" + id + "】不处于待审核状态");
        }
        
        if (!SessionUtil.getLoginUser(req).equals(approve.getApplyUser())) {
        	throw new BusinessLevelException(403, "无权限操作");
        }

        approve.setApproveState((byte)ApproveState.NOT_COMMITED.getValue());

        approveService.update(approve);
    }

    @RequestMapping(path = "/approve/page.json", method = {RequestMethod.GET})
    public PageResultVO<Approve> queryApproPage(@RequestParam(required = false, defaultValue = "1") @Min(value = 0, message = "currentPage必须为正数") Integer currentPage,
                                                @RequestParam(required = false, defaultValue = "10") @Min(value = 0, message = "pageSize必须为正数") Integer pageSize,
                                                @RequestParam(required = false) Integer state,
                                                @RequestParam(required = false) String conditions,
                                                @RequestParam(required = false) String clusterId) {
        Page<Approve> page = new Page<>();
        page.setPageNo(currentPage);
        page.setPageSize(pageSize);

        PageResultVO<Approve> resultVO = new PageResultVO<>();

        List<String> privileges = AuthorizeUtil.loadPrivileges();
        if(privileges != null && !(privileges.contains("template.approve") || privileges.contains("template.all"))){
            resultVO.setList(Collections.emptyList());
        } else {
            resultVO.setList(approveService.findApprovePage(page, state, conditions, clusterId));
        }

        resultVO.setTotal(page.getTotalRecord());
        resultVO.setPageCount(page.getTotalPage());

        return resultVO;
    }

    @RequestMapping(path = "/approve.json", method = RequestMethod.POST)
    public void approve(HttpServletRequest req, @RequestBody ApproveOp params) throws Exception{
        if(!AuthorizeUtil.authorizeTemplateApprovePrivilege(req)){
        	throw new BusinessLevelException(403, "无权限操作");
        }

        String ids = params.getIds();
        if(ObjectUtils.isEmpty(ids)){
            throw new BusinessLevelException(500, "id不能为空");
        }

        String state =  params.getState();
        if(ObjectUtils.isEmpty(state)){
            throw new BusinessLevelException(500, "state不能为空");
        }

        String note =  params.getNote();
        if(ObjectUtils.isEmpty(note)){
            throw new BusinessLevelException(500, "note不能为空");
        }

        String currentUser = SessionUtil.getLoginUser(req);

        String[] idsArray = ids.split(",");

        for (String id: idsArray) {
            approve(Long.valueOf(id), state, note, currentUser);
        }

    }

    private void approve(Long id, String state, String note, String approveUser) throws Exception {
        approveService.approve(id, state, note, approveUser);

        if (Integer.parseInt(state) == ApproveState.ON_LINEED.getValue()){
            try {
                SearchTemplate template = templateService.findById(approveService.findById(id).getRelateId());

                Long indexId = template.getIndexId();

                AuditLogUtil.log("update search template: name - {0}, indexId - {1}, indexName - {2}", template.getTemplateName(), indexId, indexService.findById(indexId).getIndexName());
                IndexOperation record = new IndexOperation();
                record.setEventDetail(template.toString());
                record.setEventName(IndexOperationEventName.UPDATE_TEMPLATE);
                record.setEventType(IndexOperationEventType.TEMPLATE_EVENT);
                record.setIndexId(indexId);
                record.setOperationTime(new Date());
                record.setOperator(approveUser);
                indexOperationService.insert(record);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

}