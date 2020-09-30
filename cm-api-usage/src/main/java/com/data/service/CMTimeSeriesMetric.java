package com.data.service;

import com.alibaba.fastjson.JSON;
import com.cloudera.api.model.*;
import com.cloudera.api.v11.TimeSeriesResourceV11;
import com.cloudera.api.v30.RootResourceV30;
import com.data.bean.Resource;
import com.data.constant.Metrics;
import com.data.util.CMClientUtil;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
//获取cloudera-manager中所有监控图表的时序数据
public class CMTimeSeriesMetric {

    static RootResourceV30 apiRoot = CMClientUtil.getApiRoot();

    public static void main(String[] args) {
        System.out.println(getResources());
    }

    public static Map<String,List<Resource>> getResources(){
        HashMap<String, List<Resource>> map = new HashMap<>();
        map.put(Metrics.MEM,computeMemory());
        map.put(Metrics.DISK,computeDisk());
        map.put(Metrics.CPU,computeCpu());
        System.out.println(map);
        return map;
    }


    /**
     * @author : yangx
     * @date : 2020.09.29 20:16
     * @description : 计算cdh的disk：已用disk、总disk,单位:GB
     * @param :
     * @return :
     **/
    private static List<Resource> computeDisk(){

        String diskQuery=String.format("select %s,%s WHERE category=HOST",Metrics.DISK_USED_COMPUTE_RULE,Metrics.DISK_TOTAL_COMPUTE_RULE);

        List<Metric> allMetric = getServiceMetrics(diskQuery,LocalDateTime.now().toString(), "now");

        Map<String, List<Metric>> metricGroupBy = allMetric.stream().collect(
                Collectors.groupingBy(Metric::getMetricName));

        Double diskUsed = 0.0;
        Double diskTotal = 0.0;

        for (Map.Entry<String, List<Metric>> metrics : metricGroupBy.entrySet()) {
            String metricName = metrics.getKey();
            Double tempDisk = 0.0;
            for (Metric metric : metrics.getValue()) {
                //该dataList只有一个元素的
                Data data = metric.getData().get(0);
                tempDisk += data.getValue();
            }
            if (StringUtils.equals(metricName, Metrics.DISK_USED_COMPUTE_RULE)) {
                diskUsed = tempDisk;
            } else {
                diskTotal = tempDisk;
            }
        }

        diskUsed = new BigDecimal(diskUsed).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        diskTotal = new BigDecimal(diskTotal).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

        Resource used = new Resource(Metrics.DISK_USED, diskUsed, "GB");
        Resource total = new Resource(Metrics.DISK_TOTAL,diskTotal, "GB");

        List<Resource> list = new ArrayList<>();

        list.add(used);
        list.add(total);

        return list;
    }

    /**
     * @author : yangx
     * @date : 2020.09.29 19:46
     * @description : 计算cdh的cpu：已用cpu核心数、总cpu核心数，单位:个
     * @param :
     * @return :
     **/
    private static List<Resource> computeCpu() {

        ArrayList<Host> allHost = CMHost.getAllHost();

        long cpuUsed = 0L;
        long cpuTotal = 0L;
        for (Host host : allHost) {
            long numCores = host.getNumCores();
            cpuTotal += numCores;

            String cpuQuery=String.format("select cpu_user_rate / getHostFact(numCores, 1) , cpu_system_rate / getHostFact(numCores, 1) , cpu_nice_rate / getHostFact(numCores, 1) , cpu_iowait_rate / getHostFact(numCores, 1) , cpu_irq_rate / getHostFact(numCores, 1) , cpu_soft_irq_rate / getHostFact(numCores, 1) , cpu_steal_rate / getHostFact(numCores, 1)  where hostId=%s",host.getHostId());
            List<Metric> allMetric = getServiceMetrics(cpuQuery,LocalDateTime.now().toString(), "now");

            Double temUsedRatio = 0.0;
            for (Metric metric : allMetric) {
                //该dataList只有一个元素的
                System.out.println(metric);
                Data data = metric.getData().get(0);
                temUsedRatio += data.getValue();
            }
//            System.out.println("hostName:"+hostName+",temUsedRatio:"+temUsedRatio);
            double numCoresUsed = temUsedRatio * numCores;
            cpuUsed += numCoresUsed;
        }

        Resource used = new Resource(Metrics.CPU_USED, cpuUsed, "num");
        Resource total = new Resource(Metrics.CPU_TOTAL, cpuTotal, "num");

        List<Resource> list = new ArrayList<>();

        list.add(used);
        list.add(total);

        return list;
    }


    /**
     * @param :
     * @return :
     * @author : yangx
     * @date : 2020.09.29 18:56
     * @description : 计算出此刻cdh集群：使用的内存和总内存,单位:GB
     **/
    private static List<Resource> computeMemory() {

        String memQuery = String.format("select %s,%s", Metrics.MEM_USED_COMPUTE_RULE, Metrics.MEM_TOTAL_COMPUTE_RULE);

        List<Metric> allMetric = getServiceMetrics(
                memQuery, LocalDateTime.now().toString(), "now");

        Double memUsed = 0.0;
        Double memTotal = 0.0;

        Map<String, List<Metric>> metricGroupBy = allMetric.stream().collect(
                Collectors.groupingBy(Metric::getMetricName));

        for (Map.Entry<String, List<Metric>> metrics : metricGroupBy.entrySet()) {
            String metricName = metrics.getKey();
            Double tempMem = 0.0;
            for (Metric metric : metrics.getValue()) {
                //该dataList只有一个元素的
                Data data = metric.getData().get(0);
                tempMem += data.getValue();
            }
            if (StringUtils.equals(metricName, Metrics.MEM_USED_COMPUTE_RULE)) {
                memUsed = tempMem;
            } else {
                memTotal = tempMem;
            }
        }

        memUsed = new BigDecimal(memUsed).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        memTotal = new BigDecimal(memTotal).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

        Resource used = new Resource(Metrics.MEM_USED, memUsed, "GB");
        Resource total = new Resource(Metrics.MEM_TOTAL, memTotal, "GB");

        List<Resource> list = new ArrayList<>();

        list.add(used);
        list.add(total);

        return list;
    }

    private static List<Metric> formatApiTimeSeriesResponse(List<ApiTimeSeriesResponse> apiTimeSeriesResponseList) {
        List<Metric> metrics = new ArrayList<>();
        for (ApiTimeSeriesResponse apiTimeSeriesResponse : apiTimeSeriesResponseList) {

            List<ApiTimeSeries> apiTimeSeriesList = apiTimeSeriesResponse.getTimeSeries();
            for (ApiTimeSeries apiTimeSeries : apiTimeSeriesList) {
                Metric metric = new Metric();
                metric.setMetricName(apiTimeSeries.getMetadata().getMetricName());
                metric.setEntityName(apiTimeSeries.getMetadata().getEntityName());
                metric.setStartTime(apiTimeSeries.getMetadata().getStartTime().toString());
                metric.setEndTime(apiTimeSeries.getMetadata().getEndTime().toString());

                List<ApiTimeSeriesData> timeSeriesDataList = apiTimeSeries.getData();

                //他默认按照时间排序的，我只取最新的，即最后一条
                //ps：cdh最细的粒度是按照10分钟聚合的
                ApiTimeSeriesData apiTimeSeriesData = timeSeriesDataList.get(timeSeriesDataList.size() - 1);

                List<Data> dataList = new ArrayList<>();
                Data data = new Data();
                data.setTimestamp(apiTimeSeriesData.getTimestamp().toString());
                data.setType(apiTimeSeriesData.getType());
                data.setValue(apiTimeSeriesData.getValue());
                dataList.add(data);

                metric.setData(dataList);
                metrics.add(metric);
            }
        }
        return metrics;
    }

    private static List<Metric> getServiceMetrics(String query, String startTime, String endTime) {
        TimeSeriesResourceV11 timeSeriesResourceV11 = apiRoot.getTimeSeriesResource();
        String[] params = new String[]{query, startTime, endTime};
        log.info("query sql is {} ,startTime is {} ,endTime is now", params);

        log.info("开始测试的时间为{},**************开始查询某个服务点位状态**************", LocalDateTime.now());
        ApiTimeSeriesResponseList response = timeSeriesResourceV11.queryTimeSeries(query, startTime, endTime);
        List<ApiTimeSeriesResponse> apiTimeSeriesResponseList = response.getResponses();
        List<Metric> metrics = formatApiTimeSeriesResponse(apiTimeSeriesResponseList);
        log.info("查询时间序列点位:{}", JSON.toJSON(metrics));
        log.info("结束测试的时间为{},**************结束查询某个服务点位状态**************", LocalDateTime.now());
        return metrics;
    }

}

@lombok.Data
class Metric {
    private String metricName;
    private String entityName;
    private String startTime;
    private String endTime;
    List<Data> data = new ArrayList<>();
}

@lombok.Data
class Data {
    private String timestamp;
    private Double value;
    private String type;
}