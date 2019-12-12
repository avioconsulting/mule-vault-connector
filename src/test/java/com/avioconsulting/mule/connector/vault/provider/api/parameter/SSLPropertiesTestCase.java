package com.avioconsulting.mule.connector.vault.provider.api.parameter;

import junit.framework.TestCase;
import org.junit.Test;

public class SSLPropertiesTestCase extends TestCase {

    @Test
    public void testSSLProperties() {
        SSLProperties props = new SSLProperties();
        props.setPemFile("pemFile");
        props.setTrustStoreFile("trustStore");

        assertEquals("pemFile", props.getPemFile());
        assertEquals("trustStore", props.getTrustStoreFile());
    }
}
