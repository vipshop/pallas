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

public class Role{
	
	private Long id;
	private String roleName;
	private String description;
	private String createdBy;
	private String lastUpdatedBy;
	private Date createTime;
	private Date updateTime;
	private Boolean isDeleted;
		
	public void setId(Long id){
		this.id = id;
	}
	
	public Long getId(){
		return this.id;
	}
		
	public void setRoleName(String roleName){
		this.roleName = roleName;
	}
	
	public String getRoleName(){
		return this.roleName;
	}
		
	public void setDescription(String description){
		this.description = description;
	}
	
	public String getDescription(){
		return this.description;
	}
		
	public void setCreatedBy(String createdBy){
		this.createdBy = createdBy;
	}
	
	public String getCreatedBy(){
		return this.createdBy;
	}
		
	public void setLastUpdatedBy(String lastUpdatedBy){
		this.lastUpdatedBy = lastUpdatedBy;
	}
	
	public String getLastUpdatedBy(){
		return this.lastUpdatedBy;
	}
		
	public void setCreateTime(Date createTime){
		this.createTime = createTime;
	}
	
	public Date getCreateTime(){
		return this.createTime;
	}
		
	public void setUpdateTime(Date updateTime){
		this.updateTime = updateTime;
	}
	
	public Date getUpdateTime(){
		return this.updateTime;
	}
		
	public void setIsDeleted(Boolean isDeleted){
		this.isDeleted = isDeleted;
	}
	
	public Boolean getIsDeleted(){
		return this.isDeleted;
	}
		
		
}