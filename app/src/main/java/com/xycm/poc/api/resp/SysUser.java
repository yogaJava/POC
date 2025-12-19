package com.xycm.poc.api.resp;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 用户对象 sys_user
 *
 * @author shupf
 */
public class SysUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 部门ID
     */
    private String deptId;

    /**
     * 开发平台ID
     */
    private String openId;

    /**
     * 用户账号
     */
    private String userName;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 手机号码
     */
    private String phonenumber;

    /**
     * 用户性别
     */
    private String sex;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 资质地址
     */
    private String aptitudeUrl;

    /**
     * 证书等级
     */
    private String certLevel;

    /**
     * 取证时间
     */
    private String forensicsTime;

    /**
     * 资质有效期（年， -1长期）
     */
    private Integer aptitudeLife;

    /**
     * 密码
     */
    private String password;

    /**
     * 账号状态（0正常 1停用）
     */
    private String status;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    private String delFlag;

    /**
     * 签名图片地址
     */
    private String signPng;

    /**
     * 最后登录IP
     */
    private String loginIp;

    /**
     * 最后登录时间
     */
    private String loginString;

    /**
     * 密码最后更新时间
     */
    private String pwdUpStringString;

    /**
     * 角色组
     */
    private List<String> roleIds;

    /**
     * 岗位组
     */
    private String[] postIds;

    /**
     * 角色ID
     */
    private String roleId;

    /**
     * 搜索值
     */

    private String searchValue;

    /**
     * 创建者
     */
    private String createBy;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 更新者
     */
    private String upStringBy;

    /**
     * 更新时间
     */
    private String upStringTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 人脸ID
     */
    private String faceId;

    private String pocUser;

    private String pocPassword;

    private String pucUser;

    private String pucPassword;

    /**
     * poc 登录状态
     */
    private String pocStatus;

    /**
     * puc 登录状态
     */
    private String pucStatus;

    /**
     * 请求参数
     */
    private Map<String, Object> params;

    /**
     * 当前线路编码
     */
    private String currentLineCode;

    /**
     * 当前线路名称
     */
    private String currentLineName;

    public SysUser() {

    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDeptId() {
        return deptId;
    }

    public void setDeptId(String deptId) {
        this.deptId = deptId;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getAptitudeUrl() {
        return aptitudeUrl;
    }

    public void setAptitudeUrl(String aptitudeUrl) {
        this.aptitudeUrl = aptitudeUrl;
    }

    public String getCertLevel() {
        return certLevel;
    }

    public void setCertLevel(String certLevel) {
        this.certLevel = certLevel;
    }

    public String getForensicsTime() {
        return forensicsTime;
    }

    public void setForensicsTime(String forensicsTime) {
        this.forensicsTime = forensicsTime;
    }

    public Integer getAptitudeLife() {
        return aptitudeLife;
    }

    public void setAptitudeLife(Integer aptitudeLife) {
        this.aptitudeLife = aptitudeLife;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public String getSignPng() {
        return signPng;
    }

    public void setSignPng(String signPng) {
        this.signPng = signPng;
    }

    public String getLoginIp() {
        return loginIp;
    }

    public void setLoginIp(String loginIp) {
        this.loginIp = loginIp;
    }

    public String getLoginString() {
        return loginString;
    }

    public void setLoginString(String loginString) {
        this.loginString = loginString;
    }

    public String getPwdUpStringString() {
        return pwdUpStringString;
    }

    public void setPwdUpStringString(String pwdUpStringString) {
        this.pwdUpStringString = pwdUpStringString;
    }

    public List<String> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<String> roleIds) {
        this.roleIds = roleIds;
    }

    public String[] getPostIds() {
        return postIds;
    }

    public void setPostIds(String[] postIds) {
        this.postIds = postIds;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getSearchValue() {
        return searchValue;
    }

    public void setSearchValue(String searchValue) {
        this.searchValue = searchValue;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpStringBy() {
        return upStringBy;
    }

    public void setUpStringBy(String upStringBy) {
        this.upStringBy = upStringBy;
    }

    public String getUpStringTime() {
        return upStringTime;
    }

    public void setUpStringTime(String upStringTime) {
        this.upStringTime = upStringTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getFaceId() {
        return faceId;
    }

    public void setFaceId(String faceId) {
        this.faceId = faceId;
    }

    public String getPocUser() {
        return pocUser;
    }

    public void setPocUser(String pocUser) {
        this.pocUser = pocUser;
    }

    public String getPocPassword() {
        return pocPassword;
    }

    public void setPocPassword(String pocPassword) {
        this.pocPassword = pocPassword;
    }

    public String getPucUser() {
        return pucUser;
    }

    public void setPucUser(String pucUser) {
        this.pucUser = pucUser;
    }

    public String getPucPassword() {
        return pucPassword;
    }

    public void setPucPassword(String pucPassword) {
        this.pucPassword = pucPassword;
    }

    public String getPocStatus() {
        return pocStatus;
    }

    public void setPocStatus(String pocStatus) {
        this.pocStatus = pocStatus;
    }

    public String getPucStatus() {
        return pucStatus;
    }

    public void setPucStatus(String pucStatus) {
        this.pucStatus = pucStatus;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public String getCurrentLineCode() {
        return currentLineCode;
    }

    public void setCurrentLineCode(String currentLineCode) {
        this.currentLineCode = currentLineCode;
    }

    public String getCurrentLineName() {
        return currentLineName;
    }

    public void setCurrentLineName(String currentLineName) {
        this.currentLineName = currentLineName;
    }
}
