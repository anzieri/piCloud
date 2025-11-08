package com.example.piCloud.Metrics;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MetricsService {

    public double getDriveUsage(String drivePath) {
        ArrayList<Double> storagedetails = StorageUsageTracker.getDriveSpaceUsed(drivePath);
        log.info(String.format("%.1fGB ", new Object[] { storagedetails.get(2) }));
        double answer = ((Double)storagedetails.get(2)).doubleValue();
        return answer;
    }

    @Scheduled(fixedRate = 60000L)
    public List<Double> monitorStorage(String drivePath) {
        ArrayList<Double> storagedetails = StorageUsageTracker.getDriveSpaceUsed(drivePath);
        double usedSpaceGB = ((Double)storagedetails.get(2)).doubleValue();
        double freeSpaceGB = ((Double)storagedetails.get(1)).doubleValue();
        double totalSpaceGB = ((Double)storagedetails.get(0)).doubleValue();
        log.info(String.format("%.1fGB of %.1f used", new Object[] { Double.valueOf(usedSpaceGB), Double.valueOf(totalSpaceGB) }));
        return storagedetails;
    }
}
