package com.xycm.poc.constants;

import com.xycm.poc.util.SignUtil;

/**
 * 常量类
 */
public class Constants {

    private static final String appKey = "kcg5aMzfF92wkYgVuc";

    private static final String appSecret = "Df0z7uIVJt+Dti823OCxeO0GoZcakJ998ht+OLdEVsm8B3M6kaofrcHKOkF4Kuj/Qawse";

    public static String getAppSecret() {
        return SignUtil.decryptData(appKey, appSecret);
    }

    public static final String BASE_URL = "https://cqxf.shupf.cn:8083/test-api/";

    public static final String H5_URL = "https://cqxf.shupf.cn:8083";
    public static final String DEBUG_H5_URL = "http://192.168.0.101:9090";

    public final static String LOGIN_UDN = "50120202@poc.com";

    public final static String LOGIN_PWD = "cq123456";

    public final static String SERVER = "113.204.49.3:8062";

    /**
     * 成功标记
     */
    public static final Integer SUCCESS = 200;

    /**
     * 失败标记
     */
    public static final Integer FAIL = 500;


}
