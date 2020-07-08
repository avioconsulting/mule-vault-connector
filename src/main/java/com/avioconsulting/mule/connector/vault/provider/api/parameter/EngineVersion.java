package com.avioconsulting.mule.connector.vault.provider.api.parameter;

/**
 * Enumeration of the acceptable secrets engine versions
 */
public enum EngineVersion {
    V1 (1),
    V2 (2)
    ;

    private final Integer versionNumber;

    EngineVersion(Integer i) {
        this.versionNumber = i;
    }

    public Integer getEngineVersionNumber() {
        return this.versionNumber;
    }
}
