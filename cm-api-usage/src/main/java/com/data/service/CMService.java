package com.data.service;

import com.cloudera.api.DataView;
import com.cloudera.api.model.*;
import com.cloudera.api.v10.ServicesResourceV10;
import com.cloudera.api.v11.RolesResourceV11;
import com.cloudera.api.v18.ServicesResourceV18;
import com.cloudera.api.v30.RootResourceV30;
import com.data.util.CMClientUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
//获取cloudera-manager上所有集群的所有服务的状态
public class CMService {
    
    static RootResourceV30 apiRoot= CMClientUtil.getApiRoot();

    public static void main(String[] args) {
        getAllService();
    }

    public static void getAllService(){
        log.info("开始测试的时间为{},**************开始测试集群服务运行状态**************", LocalDateTime.now());
        ApiClusterList apiClusterList = apiRoot.getClustersResource().readClusters(DataView.SUMMARY);
        for(ApiCluster apiCluster:apiClusterList){
            log.info("集群名称：{}",apiCluster.getDisplayName());
            log.info("CDH 版本：{}-{}",apiCluster.getVersion(),apiCluster.getFullVersion());
            ServicesResourceV10 servicesResourceV10 = apiRoot.getClustersResource().getServicesResource(apiCluster.getName());
            List<ApiService> apiServices = servicesResourceV10.readServices(DataView.FULL).getServices();
            log.info("集群总共有：{} 个service 在运行",apiServices.size());
            for(ApiService apiService:apiServices){
                Service service = formatService(apiService);
                log.info("***********************************");
                log.info("service 名称 {}",service.getName());
                log.info("service 类型 {}",service.getType());
                for(Agent agent:service.getAgentList()) {
                    log.info("节点名称 {}", agent.getName());
                    log.info("节点状态 {}", agent.getStatus());
                }
                log.info("***********************************");
            }
        }
        log.info("结束测试的时间为{},**************结束测试集群服务运行状态**************",LocalDateTime.now());
    }

    public static Service formatService(ApiService apiService){
        Service service = new Service();
        List<Agent> agents = new ArrayList<>();
        service.setName(apiService.getName());
        service.setType(apiService.getType());
        for(ApiHealthCheck apiHealthCheck:apiService.getHealthChecks()){
            Agent agent =new Agent();
            agent.setName(apiHealthCheck.getName());
            agent.setStatus(apiHealthCheck.getSummary());
            agents.add(agent);
        }
        service.setAgentList(agents);
        return service;
    }

    public static void getAllServiceRoles(){
        log.info("开始测试的时间为{},**************开始测试集群各个服务的roles运行状态**************",LocalDateTime.now());
        ApiClusterList apiClusterList = apiRoot.getClustersResource().readClusters(DataView.SUMMARY);
        for(ApiCluster apiCluster:apiClusterList){
            log.info("集群名称：{}",apiCluster.getDisplayName());
            log.info("CDH 版本：{}-{}",apiCluster.getVersion(),apiCluster.getFullVersion());
            ServicesResourceV18 servicesResourceV18 = apiRoot.getClustersResource().getServicesResource(apiCluster.getName());
            List<ApiService> apiServices = servicesResourceV18.readServices(DataView.FULL).getServices();
            log.info("集群总共有：{} 个service 在运行",apiServices.size());
            for(ApiService apiService:apiServices){
                RolesResourceV11 rolesResourceV11 = servicesResourceV18.getRolesResource(apiService.getName());
                log.info("---------------------服务名称是{}---------------------",apiService.getName());
                for(ApiRole apiRole :rolesResourceV11.readRoles()){
                    log.info("***************************",apiRole.getName());
                    log.info("role名称 {}",apiRole.getName());
                    log.info("role类型 {}",apiRole.getType());
                    log.info("所属集群 {}",apiRole.getServiceRef().getClusterName());
                    log.info("所属服务 {}",apiRole.getServiceRef().getServiceName());
                    log.info("主机ID {}",apiRole.getHostRef().getHostId());
                    log.info("roleUrl {}",apiRole.getRoleUrl());
                    log.info("role状态 {}",apiRole.getRoleState());
                    log.info("运行状态总结 {}",apiRole.getHealthSummary());
                    log.info("entityStatus {}",apiRole.getEntityStatus());
                    log.info("roleConfigGroupName {}",apiRole.getRoleConfigGroupRef().getRoleConfigGroupName());
                    log.info("configStalenessStatus {}",apiRole.getConfigStalenessStatus());
                    log.info("haStatus {}",apiRole.getHaStatus());
                    for(ApiHealthCheck apiHealthCheck:apiRole.getHealthChecks()){
                        log.info("health check name {}",apiHealthCheck.getName());
                        log.info("health check summary {}",apiHealthCheck.getSummary());
                        log.info("health check suppressed {}",apiHealthCheck.getSuppressed());
                    }
                    log.info("***************************");
                }
                log.info("--------------------------------------------------------",apiService.getName());
            }
        }
        log.info("结束测试的时间为{},**************结束测试集群各个服务的roles运行状态**************",LocalDateTime.now());
    }

}

@Data
@AllArgsConstructor
@NoArgsConstructor
class Service {
   private String name;
   private String type;
   private List<Agent> agentList = new ArrayList<>();
}


@Data
@AllArgsConstructor
@NoArgsConstructor
class Agent {
   private String name;
   private ApiHealthSummary status;
}