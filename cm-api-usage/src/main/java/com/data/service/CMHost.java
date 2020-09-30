package com.data.service;

import com.alibaba.fastjson.JSON;
import com.cloudera.api.DataView;
import com.cloudera.api.model.ApiHost;
import com.cloudera.api.model.ApiRoleRef;
import com.cloudera.api.v10.HostsResourceV10;
import com.cloudera.api.v18.RootResourceV18;
import com.data.util.CMClientUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
//获取cloudera-manager中每个集群的每个主机的详细信息：
public class CMHost {

    static RootResourceV18 apiRoot= CMClientUtil.getApiRoot();

    public static void main(String[] args) {
        getAllHost();
    }
    
    public static ArrayList<Host> getAllHost(){
        log.info("开始测试的时间为{},**************开始测试集群主机运行状态**************", LocalDateTime.now());
        HostsResourceV10 hostsResourceV10 = apiRoot.getHostsResource();
        List<ApiHost> hostList = hostsResourceV10.readHosts(DataView.SUMMARY).getHosts();
        log.info("总共有 {} 台主机组成集群",hostList.size());

        ArrayList<Host> hosts = new ArrayList<>();
        for(ApiHost apiHost:hostList){
            log.info("---------------------------------------------");
            Host host = formatHost(hostsResourceV10.readHost(apiHost.getHostId()));
            log.info("主机Id : {}",host.getHostId());
            log.info("主机名： {}",host.getHostName());
            log.info("主机IP： {}",host.getIpAddress());
            log.info("主机线程数：{}",host.getNumCores());
            log.info("上次上报心跳时间 ：{}",host.getLastHeart());
            log.info("核心数：{}",host.getNumPhysicalCores());
            log.info("机架：{}",host.getRack());
            log.info("内存（G）：{}",host.getTotalPhysMemBytes());
            log.info("进程：{}", JSON.toJSON(host.getServices()));
            log.info("---------------------------------------------");
            hosts.add(host);
        }
        log.info("结束测试的时间为{},**************结束测试集群主机运行状态**************",LocalDateTime.now());
        return hosts;
    }

    public static Host formatHost(ApiHost apiHost){
        Host host = new Host();
        List<String> services = new ArrayList<>();
        host.setHostId(apiHost.getHostId());
        host.setHostName(apiHost.getHostname());
        host.setIpAddress(apiHost.getIpAddress());
        host.setNumCores(apiHost.getNumCores());
        host.setNumPhysicalCores(apiHost.getNumPhysicalCores());
        host.setLastHeart(apiHost.getLastHeartbeat().toString());
        host.setRack(apiHost.getRackId());
        host.setTotalPhysMemBytes(apiHost.getTotalPhysMemBytes()/1073741824);
        for(ApiRoleRef apiRoleRef:apiHost.getRoleRefs()){
            services.add(apiRoleRef.getRoleName());
        }
        host.setServices(services);
        return host;
    }
}


@Data
@AllArgsConstructor
@NoArgsConstructor
class Host {
    private String hostId;
    private String hostName;
    private String ipAddress;
    private String rack;
    private String lastHeart;
    private List<String> services = new ArrayList<>();
    private long numCores;
    private long numPhysicalCores;
    private long totalPhysMemBytes;
}