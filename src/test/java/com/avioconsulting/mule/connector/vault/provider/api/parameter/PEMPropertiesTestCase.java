package com.avioconsulting.mule.connector.vault.provider.api.parameter;

import junit.framework.TestCase;
import org.junit.Test;

public class PEMPropertiesTestCase extends TestCase {

    @Test
    public void testGettersAndSetters() {
        PEMProperties props = new PEMProperties();

        props.setClientKeyPemFile("key");
        props.setClientPemFile("file");

        assertEquals("file", props.getClientPemFile());
        assertEquals("key", props.getClientKeyPemFile());
    }

    @Test
    public void testToString() {
        PEMProperties props = new PEMProperties();

        props.setClientKeyPemFile("key");
        props.setClientPemFile("file");

        assertEquals("clientPemFile: file, clientKeyPemFile: key", props.toString());
    }
}
