package com.avioconsulting.mule.vault.api.client.response;

import java.util.HashMap;
import java.util.Map;

public class LogicalResponse {
    private Map<String,String> data = new HashMap<>();
    private String leaseId;
    private Long leaseDuration;
    private String requestId;
    private Boolean renewable;

    public LogicalResponse() {
        super();
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }

    public String getLeaseId() {
        return leaseId;
    }

    public void setLeaseId(String leaseId) {
        this.leaseId = leaseId;
    }

    public Long getLeaseDuration() {
        return leaseDuration;
    }

    public void setLeaseDuration(Long leaseDuration) {
        this.leaseDuration = leaseDuration;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Boolean getRenewable() {
        return renewable;
    }

    public void setRenewable(Boolean renewable) {
        this.renewable = renewable;
    }
}
