package com.starryassociates.core.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "CAN_INFO")
public class CanInfo {

    @Id
    @Column(name = "CAN_CODE", length = 7, nullable = false)
    private String canCode;

    @Column(name = "CAN_DESC", length = 30)
    private String canDesc;

    @Column(name = "FY_BEGIN", length = 4)
    private String fyBegin;

    @Column(name = "FY_END", length = 4)
    private String fyEnd;

    @Column(name = "PROJECT_NO", length = 20)
    private String projectNo;

    @Column(name = "ADMINCODE", length = 8)
    private String adminCode;

    @Column(name = "IS_ACTIVE")
    private Boolean isActive;

    @Column(name = "LAST_UPDATED")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdated;

    // Getters and Setters

    public String getCanCode() {
        return canCode;
    }

    public void setCanCode(String canCode) {
        this.canCode = canCode;
    }

    public String getCanDesc() {
        return canDesc;
    }

    public void setCanDesc(String canDesc) {
        this.canDesc = canDesc;
    }

    public String getFyBegin() {
        return fyBegin;
    }

    public void setFyBegin(String fyBegin) {
        this.fyBegin = fyBegin;
    }

    public String getFyEnd() {
        return fyEnd;
    }

    public void setFyEnd(String fyEnd) {
        this.fyEnd = fyEnd;
    }

    public String getProjectNo() {
        return projectNo;
    }

    public void setProjectNo(String projectNo) {
        this.projectNo = projectNo;
    }

    public String getAdminCode() {
        return adminCode;
    }

    public void setAdminCode(String adminCode) {
        this.adminCode = adminCode;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
