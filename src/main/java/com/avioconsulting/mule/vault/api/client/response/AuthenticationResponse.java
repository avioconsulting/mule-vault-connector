package com.avioconsulting.mule.vault.api.client.response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuthenticationResponse {

    private String accessor;
    private String clientToken;
    private String entityId;
    private Long leaseDuration;
    private Map<String, String> metadata = new HashMap<>();
    private Boolean orphan;
    private List<String> policies = new ArrayList<>();
    private Boolean renewable;
    private String tokenType;

    public AuthenticationResponse() {
        super();
    }

    public String getAccessor() {
        return accessor;
    }

    public void setAccessor(String accessor) {
        this.accessor = accessor;
    }

    public String getClientToken() {
        return clientToken;
    }

    public void setClientToken(String clientToken) {
        this.clientToken = clientToken;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public Long getLeaseDuration() {
        return leaseDuration;
    }

    public void setLeaseDuration(Long leaseDuration) {
        this.leaseDuration = leaseDuration;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public Boolean getOrphan() {
        return orphan;
    }

    public void setOrphan(Boolean orphan) {
        this.orphan = orphan;
    }

    public List<String> getPolicies() {
        return policies;
    }

    public void setPolicies(List<String> policies) {
        this.policies = policies;
    }

    public Boolean getRenewable() {
        return renewable;
    }

    public void setRenewable(Boolean renewable) {
        this.renewable = renewable;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
}
