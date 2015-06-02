package com.solidparts.warehouse.dto;

/**
 * Created by geidnert on 28/05/15.
 */
public class ItemDTO {
    private long cacheID;
    int guid;
    int count;
    String name;
    String description;
    byte[] image;
    String qrCode;

    public int getGuid() {
        return guid;
    }

    public void setGuid(int guid) {
        this.guid = guid;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public  byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public long getCacheID() {
        return cacheID;
    }

    public void setCacheID(long cacheID) {
        this.cacheID = cacheID;
    }

    @Override
    public String toString() {
        return "ItemDTO{" +
                "cacheID=" + cacheID +
                ", guid=" + guid +
                ", count=" + count +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", image='" + image + '\'' +
                ", qrCode='" + qrCode + '\'' +
                '}';
    }
}
