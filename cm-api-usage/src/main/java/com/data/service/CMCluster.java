package com.data.service;

import com.cloudera.api.DataView;
import com.cloudera.api.model.ApiCluster;
import com.cloudera.api.model.ApiClusterList;
import com.cloudera.api.v30.RootResourceV30;
import com.data.util.CMClientUtil;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
//获取cloudera-manager中所有集群的信息
public class CMCluster {
    
    static RootResourceV30 apiRoot= CMClientUtil.getApiRoot();

    public static void main(String[] args) {
        getAllCluster();
    }

    public static void getAllCluster() {
        log.info("开始测试的时间为{},**************开始测试获取ClouderaManager集群信息**************", LocalDateTime.now());
        ApiClusterList apiClusterList = apiRoot.getClustersResource().readClusters(DataView.FULL);
        log.info("ClouderaManager 共管理了{}个集群", apiClusterList.getClusters().size());
        System.out.println("ClouderaManager 共管理几个集群："+apiClusterList.getClusters().size());
        for (ApiCluster apiCluster : apiClusterList) {
            ApiCluster apiCluster1 = apiRoot.getClustersResource().readCluster(apiCluster.getName());
            log.info("集群名称 {}", apiCluster1.getName());
            log.info("集群显示名称 {}", apiCluster1.getDisplayName());
            log.info("CDH 版本：{}-{}", apiCluster1.getVersion(), apiCluster.getFullVersion());
            log.info("ClusterUrl {}", apiCluster1.getClusterUrl());
            log.info("HostUrl {}", apiCluster1.getHostsUrl());
            log.info("Cluster Uuid {}", apiCluster1.getUuid());
            log.info("集群运行状态 {}", apiCluster1.getEntityStatus());
        }
        log.info("结束测试的时间为{},**************结束测试获取ClouderaManager集群信息**************", LocalDateTime.now());
    }

}