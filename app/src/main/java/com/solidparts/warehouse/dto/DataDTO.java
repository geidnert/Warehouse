package com.solidparts.warehouse.dto;

import java.io.Serializable;

/**
 * Created by geidnert on 25/06/15.
 */
public class DataDTO implements Serializable {
    private int appVersion;

    public int getLatestAppVersion() {
        return appVersion;
    }

    public void setLatestAppVersion(int appVersion) {
        this.appVersion = appVersion;
    }

    @Override
    public String toString() {
        return "AppDataDto{" +
                "appVersion=" + appVersion +
                '}';
    }
}
