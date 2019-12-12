package com.avioconsulting.mule.connector.vault.provider.api.connection.provider;

import org.junit.Rule;
import org.junit.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class TlsConnectionTestCase extends MuleArtifactFunctionalTestCase {

    private static String LOOKUP_RESPONSE = "{\"data\":{\"accessor\":\"8609694a-cdbc-db9b-d345-e782dbb562ed\",\"creation_time\":1523979354,\"creation_ttl\":2764800,\"display_name\":\"ldap2-tesla\",\"entity_id\":\"7d2e3179-f69b-450c-7179-ac8ee8bd8ca9\",\"expire_time\":\"2018-05-19T11:35:54.466476215-04:00\",\"explicit_max_ttl\":0,\"id\":\"cf64a70f-3a12-3f6c-791d-6cef6d390eed\",\"identity_policies\":[\"dev-group-policy\"],\"issue_time\":\"2018-04-17T11:35:54.466476078-04:00\",\"meta\":{\"username\":\"tesla\"},\"num_uses\":0,\"orphan\":true,\"path\":\"auth/ldap2/login/tesla\",\"policies\":[\"default\",\"testgroup2-policy\"],\"renewable\":false,\"ttl\":2764790}}";

    @Rule
    public MockServerRule mockServerRule = new MockServerRule(this);
    private MockServerClient mockClient;

    @Override
    protected String getConfigFile() {
        // Set vaultUrl and vaultToken properties so they can be used in the Mule config file
        System.setProperty("vaultUrl", String.format("https://%s:%d", mockServerRule.getClient().remoteAddress().getHostString(), mockServerRule.getClient().remoteAddress().getPort()));

        mockClient
                .withSecure(true)
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/v1/auth/token/lookup-self")
                                .withHeader("X-Vault-Token", "MOCK_TOKEN")
                ).respond(
                response()
                        .withStatusCode(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(LOOKUP_RESPONSE)
        );
        mockClient
                .withSecure(true)
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/v1/auth/cert/login")
                ).respond(
                response()
                        .withStatusCode(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"lease_id\":\"\",\"renewable\":false,\"data\":null,\"auth\":{\"client_token\":\"MOCK_TOKEN\",\"accessor\":\"18bb8f89-826a-56ee-c65b-1736dc5ea27d\",\"policies\":[\"web\",\"stage\"],\"lease_duration\":3600,\"renewable\":true}}")
        );
        mockClient
                .withSecure(true)
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/v1/secret/data/test/mysecret")
                                .withHeader("X-Vault-Token", "MOCK_TOKEN")
                ).respond(
                response()
                        .withStatusCode(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"request_id\": \"69411f4b-fb02-171a-f66c-485f106e7f5c\",\"lease_id\": \"\",\"renewable\": false,\"lease_duration\": 0,\"data\": {\"data\": {\"att1\": \"test_value1\",\"att2\": \"test_value2\"},\"metadata\": {\"created_time\": \"2019-04-24T23:03:18.63231658Z\",\"deletion_time\": \"\",\"destroyed\": false,\"version\": 1}},\"wrap_info\": null,\"warnings\": null,\"auth\": null}")

        );

        return "mule_config/test-mule-tls-auth-config.xml";
    }

    @Test
    public void testJksConfig() throws Exception {

        String payloadValue = ((String) flowRunner("getSecretFlowJksConfig")
                .run()
                .getMessage()
                .getPayload()
                .getValue());

        assertThat(payloadValue, containsString("test_value1"));
    }

    @Test
    public void testPemConfig() throws Exception {

        String payloadValue = ((String) flowRunner("getSecretFlowPemConfig")
                .run()
                .getMessage()
                .getPayload()
                .getValue());

        assertThat(payloadValue, containsString("test_value1"));
    }

    @Test
    public void testTrustStoreConfig() throws Exception {

        String payloadValue = ((String) flowRunner("getSecretFlowTrustStore")
                .run()
                .getMessage()
                .getPayload()
                .getValue());

        assertThat(payloadValue, containsString("test_value1"));
    }
}
