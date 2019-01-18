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

package com.vip.pallas.mybatis.entity;

import java.util.Date;

import javax.validation.constraints.NotBlank;

/**
 * Created by owen on 12/9/2017.
 */
public class PluginUpgrade {

    public static final int UPGRADE_STATUS_CREATE = 0; //创建（暂时没用）

    public static final int UPGRADE_STATUS_NEED_APPROVAL = 1; //待审批

    public static final int UPGRADE_STATUS_DENY = 2; //审批不通过

    public static final int UPGRADE_STATUS_CANCEL = 3; //取消

    public static final int UPGRADE_STATUS_DONE = 4; //标记完成

    public static final int UPGRADE_STATUS_DOWNLOAD = 5; //开始下载

    public static final int UPGRADE_STATUS_DOWNLOAD_DONE = 51; //下载完成

    public static final int UPGRADE_STATUS_UPGRADE = 6; //升级

    public static final int UPGRADE_STATUS_UPGRADE_DONE = 61; //升级完成

    public static final int UPGRADE_STATUS_REMOVE = 7; //移除插件

    private long id;

    @NotBlank(message = "clusterId不能为空")
    private String clusterId;

    @NotBlank(message = "pluginName不能为空")
    private String pluginName;

    @NotBlank(message = "pluginVersion不能为空")
    private String pluginVersion;

    private int pluginType;

    private String packagePath;

    private String applyUser;

    private Date applyTime;

    private String approveUser;

    private Date approveTime;

    private Date createTime;

    private Date updateTime;

    private int state;

    @NotBlank(message = "note不能为空")
    private String note;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getPluginName() {
        return pluginName;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    public String getPluginVersion() {
        return pluginVersion;
    }

    public void setPluginVersion(String pluginVersion) {
        this.pluginVersion = pluginVersion;
    }

    public int getPluginType() {
        return pluginType;
    }

    public void setPluginType(int pluginType) {
        this.pluginType = pluginType;
    }

    public String getPackagePath() {
        return packagePath;
    }

    public void setPackagePath(String packagePath) {
        this.packagePath = packagePath;
    }

    public String getApplyUser() {
        return applyUser;
    }

    public void setApplyUser(String applyUser) {
        this.applyUser = applyUser;
    }

    public Date getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(Date applyTime) {
        this.applyTime = applyTime;
    }

    public String getApproveUser() {
        return approveUser;
    }

    public void setApproveUser(String approveUser) {
        this.approveUser = approveUser;
    }

    public Date getApproveTime() {
        return approveTime;
    }

    public void setApproveTime(Date approveTime) {
        this.approveTime = approveTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isFinished() {
        return state == UPGRADE_STATUS_CREATE || state == UPGRADE_STATUS_DENY || state == UPGRADE_STATUS_CANCEL || state == UPGRADE_STATUS_DONE;
    }
}