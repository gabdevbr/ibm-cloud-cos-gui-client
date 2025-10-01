package com.ibm.cos.client.util;

/**
 * Utility class for formatting file sizes
 */
public class FileUtils {
    
    private FileUtils() {
        // Utility class
    }
    
    /**
     * Formats a file size in bytes to a human-readable string
     * @param size the size in bytes
     * @return formatted size string
     */
    public static String formatSize(long size) {
        if (size < 1024) {
            return size + " B";
        }
        if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        }
        if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        }
        return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
    }
}