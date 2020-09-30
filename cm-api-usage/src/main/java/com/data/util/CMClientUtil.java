package com.data.util;

import com.cloudera.api.ClouderaManagerClientBuilder;
import com.cloudera.api.v30.RootResourceV30;

/**
 * @author : yangx
 * @date : 2020/9/28 20:29
 * @description :
 */
public class CMClientUtil {

    private static String cdhApiPath = "utility";
    private static String cdhApiPort = "7180";
    private static String cdhApiUserName = "cyzx";
    private static String cdhApiPassword = "wwcy@123.com";

    private static RootResourceV30 apiRoot;

    public static RootResourceV30 getApiRoot(){
        if(apiRoot!=null){
            return apiRoot;
        }
        RootResourceV30 apiRoot1 =
                new ClouderaManagerClientBuilder().withHost(cdhApiPath)
                        .withPort(Integer.parseInt(cdhApiPort)).withUsernamePassword(cdhApiUserName, cdhApiPassword)
                        .build()
                        .getRootV30();
        apiRoot=apiRoot1;
        return apiRoot1;
    }
}
