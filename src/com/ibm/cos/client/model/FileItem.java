package com.ibm.cos.client.model;

import java.util.Date;

/**
 * Represents a file or folder item in the object storage
 */
public class FileItem {
    private final String name;
    private final ItemType type;
    private final long size;
    private final Date lastModified;
    
    public enum ItemType {
        FILE, FOLDER
    }
    
    public FileItem(String name, ItemType type, long size, Date lastModified) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null");
        }
        
        this.name = name;
        this.type = type;
        this.size = size;
        this.lastModified = lastModified != null ? new Date(lastModified.getTime()) : null;
    }
    
    public String getName() {
        return name;
    }
    
    public ItemType getType() {
        return type;
    }
    
    public long getSize() {
        return size;
    }
    
    public Date getLastModified() {
        return lastModified != null ? new Date(lastModified.getTime()) : null;
    }
    
    public boolean isFile() {
        return type == ItemType.FILE;
    }
    
    public boolean isFolder() {
        return type == ItemType.FOLDER;
    }
}