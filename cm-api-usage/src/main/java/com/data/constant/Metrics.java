package com.data.constant;

public class Metrics {
    public static final String MEM_USED_COMPUTE_RULE="physical_memory_used/(1024*1024*1024)";
    public static final String MEM_TOTAL_COMPUTE_RULE="physical_memory_total/(1024*1024*1024)";

    public static final String DISK_USED_COMPUTE_RULE="total_capacity_used_across_filesystems/(1024*1024*1024)";
    public static final String DISK_TOTAL_COMPUTE_RULE="total_capacity_across_filesystems/(1024*1024*1024)";

    public static final String CPU_USER_COMPUTE_RULE="cpu_user_rate / getHostFact(numCores, 1) * 100";
    public static final String CPU_SYSTEM_COMPUTE_RULE="cpu_system_rate / getHostFact(numCores, 1) * 100";



    public static final String MEM="mem";
    public static final String DISK="disk";
    public static final String CPU="cpu";

    public static final String MEM_USED="memUSed";
    public static final String DISK_USED="diskUSed";
    public static final String CPU_USED="cpuUSed";

    public static final String MEM_TOTAL="memTotal";
    public static final String DISK_TOTAL="diskTotal";
    public static final String CPU_TOTAL="cpuTotal";

}
