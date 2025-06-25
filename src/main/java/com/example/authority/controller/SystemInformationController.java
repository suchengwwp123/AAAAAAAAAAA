package com.example.authority.controller;

import com.example.authority.common.Result;
import com.example.authority.entity.dto.SystemInformation;
import io.swagger.v3.oas.annotations.tags.Tag;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import oshi.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @program: authority-2026.0.2
 * @ClassName:SystemInformationController
 * @description: 系统参数控制器
 * @author:dyy
 * @Version 3.0
 **/
@RestController
@Tag(name = "系统参数前端控制器")
@RequestMapping("/system")
public class SystemInformationController {

    @GetMapping("/resource")
    public Result getSystemInfo() {
        SystemInfo systemInfo = new SystemInfo();
        Map<String, Integer> result = new HashMap<>();

        // CPU 占用率
        CentralProcessor processor = systemInfo.getHardware().getProcessor();
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        Util.sleep(1000); // 等待1秒进行采样
        long[] ticks = processor.getSystemCpuLoadTicks();

        long totalCpu = 0;
        long idleCpu = ticks[CentralProcessor.TickType.IDLE.getIndex()] - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
        for (int i = 0; i < ticks.length; i++) {
            totalCpu += (ticks[i] - prevTicks[i]);
        }
        int cpuUsage = (int) ((1.0 - idleCpu * 1.0 / totalCpu) * 100);

        // 内存占用率
        GlobalMemory memory = systemInfo.getHardware().getMemory();
        long totalMem = memory.getTotal();
        long usedMem = totalMem - memory.getAvailable();
        int memUsage = (int) ((usedMem * 1.0 / totalMem) * 100);

        // 磁盘占用率
        OperatingSystem os = systemInfo.getOperatingSystem();
        FileSystem fs = os.getFileSystem();
        List<OSFileStore> fileStores = fs.getFileStores();
        long totalDisk = 0;
        long usableDisk = 0;
        for (OSFileStore store : fileStores) {
            totalDisk += store.getTotalSpace();
            usableDisk += store.getUsableSpace();
        }
        int diskUsage = totalDisk > 0 ? (int) (((totalDisk - usableDisk) * 1.0 / totalDisk) * 100) : 0;

        // 返回 int 百分比
        result.put("cpuUsage", cpuUsage);
        result.put("memUsage", memUsage);
        result.put("diskUsage", diskUsage);

        return Result.success(result);
    }

    /**
     * 获取系统参数信息（原接口保留）
     *
     * @return
     */
    @GetMapping
    public Result getSystemInformation() {
        return Result.success(new SystemInformation());
    }
}
