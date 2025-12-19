package com.xycm.poc.api.resp;

import java.util.Set;

public class UserInfoResponse {

    private SysUser user;

    private Unit unit;

    private Set<String> roles;

    private Set<String> permissions;

    private Boolean isDefaultModifyPwd;

    private Boolean isPasswordExpired;

    private Integer hiddenTrouble;

    public SysUser getUser() {
        return user;
    }

    public void setUser(SysUser user) {
        this.user = user;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }

    public Boolean getDefaultModifyPwd() {
        return isDefaultModifyPwd;
    }

    public void setDefaultModifyPwd(Boolean defaultModifyPwd) {
        isDefaultModifyPwd = defaultModifyPwd;
    }

    public Boolean getPasswordExpired() {
        return isPasswordExpired;
    }

    public void setPasswordExpired(Boolean passwordExpired) {
        isPasswordExpired = passwordExpired;
    }

    public Integer getHiddenTrouble() {
        return hiddenTrouble;
    }

    public void setHiddenTrouble(Integer hiddenTrouble) {
        this.hiddenTrouble = hiddenTrouble;
    }
}
