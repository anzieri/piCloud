package com.example.piCloud.Metrics;

import java.io.File;
import java.util.ArrayList;
import org.springframework.stereotype.Service;

@Service
public class StorageUsageTracker {
    public static ArrayList<Double> getDriveSpaceUsed(String drivePath) {
        File file = new File(drivePath);
        double totalSpace = file.getTotalSpace() / 1.073741824E9D;
        double freeSpace = file.getFreeSpace() / 1.073741824E9D;
        double usedSpace = totalSpace - freeSpace;
        ArrayList<Double> space = new ArrayList<>();
        space.add(Double.valueOf(totalSpace));
        space.add(Double.valueOf(freeSpace));
        space.add(Double.valueOf(usedSpace));
        return space;
    }
}

