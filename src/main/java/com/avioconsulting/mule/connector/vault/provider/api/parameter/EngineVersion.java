package com.avioconsulting.mule.connector.vault.provider.api.parameter;

public enum EngineVersion {
    v1 (1),
    v2 (2)
    ;

    private final Integer engineVersion;

    private EngineVersion(Integer i) {
        this.engineVersion = i;
    }

    public Integer getEngineVersionNumber() {
        return this.engineVersion;
    }
}
