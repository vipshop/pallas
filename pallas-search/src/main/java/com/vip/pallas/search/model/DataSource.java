package com.vip.pallas.search.model;

import java.util.Date;

public class DataSource {
    private Long id;

    private Long indexId;

    private String ip;

    private String port;

    private String dbname;

    private String username;

    private String password;

    private String tableName;

    private Date createTime;

    private Date updateTime;

    private String description;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIndexId() {
        return indexId;
    }

    public void setIndexId(Long indexId) {
        this.indexId = indexId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip == null ? null : ip.trim();
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port == null ? null : port.trim();
    }

    public String getDbname() {
        return dbname;
    }

    public void setDbname(String dbname) {
        this.dbname = dbname == null ? null : dbname.trim();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username == null ? null : username.trim();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password == null ? null : password.trim();
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName == null ? null : tableName.trim();
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    @Override
    public boolean equals(Object obj) {
    	return this.getId().equals(((DataSource)obj).getId());
    }
    
    @Override
    public int hashCode() {
    	return getId().intValue();
    }
    
	@Override
	public String toString() {
		return "Index's DataSource = [id=" + id + ", indexId=" + indexId + ", ip=" + ip
				+ ", port=" + port + ", dbname=" + dbname + ", username="
				+ username + ", password=" + password + ", tableName="
				+ tableName + ", createTime=" + createTime + ", updateTime="
				+ updateTime + ", description=" + description + "]";
	}
    
    
}