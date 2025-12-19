package com.xycm.poc.api.resp;

public class LoginResponse {

    private String access_token;

    private Long expires_in;

    private String pocUser;

    private String pocPassword;

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public Long getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(Long expires_in) {
        this.expires_in = expires_in;
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
}
