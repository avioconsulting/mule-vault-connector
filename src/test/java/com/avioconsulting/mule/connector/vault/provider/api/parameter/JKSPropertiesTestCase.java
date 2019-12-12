package com.avioconsulting.mule.connector.vault.provider.api.parameter;

import junit.framework.TestCase;
import org.junit.Test;

public class JKSPropertiesTestCase extends TestCase {

    @Test
    public void testToString() {
        JKSProperties props = new JKSProperties();
        props.setKeyStoreFile("testFile");
        props.setKeyStorePassword("password");

        assertEquals("testFile", props.getKeyStoreFile());
        assertEquals("password", props.getKeyStorePassword());

        assertEquals("keyStoreFile: testFile, keyStorePassword: password", props.toString());
    }

}
