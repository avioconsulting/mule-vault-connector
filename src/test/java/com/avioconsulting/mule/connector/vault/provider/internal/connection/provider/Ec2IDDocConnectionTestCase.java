package com.avioconsulting.mule.connector.vault.provider.internal.connection.provider;

import org.junit.Rule;
import org.junit.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class Ec2IDDocConnectionTestCase extends MuleArtifactFunctionalTestCase {

    @Rule
    public MockServerRule mockServerRule = new MockServerRule(this);
    private MockServerClient mockClient;

    protected String getConfigFile() {
        System.setProperty("vaultUrl", String.format("https://%s:%d", mockServerRule.getClient().remoteAddress().getHostString(), mockServerRule.getClient().remoteAddress().getPort()));
        System.setProperty("CUSTOM_IMDS", String.format("http://%s:%d/latest/dynamic/instance-identity/pkcs7", mockServerRule.getClient().remoteAddress().getHostString(), mockServerRule.getClient().remoteAddress().getPort()));
        mockClient
                .withSecure(true)
                .when(
                        request()
                            .withMethod("POST")
                            .withPath("/v1/auth/aws/login")
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
        mockClient
                .withSecure(false)
                .when(
                    request()
                        .withMethod("GET")
                        .withPath("/latest/dynamic/instance-identity/pkcs7")
                ).respond(
                    response()
                        .withStatusCode(200)
                        .withBody("gNVBAgTAldBMRAwDgYDVQQHEwdTZWF0dGxlMQ8wDQYDVQQKEwZBbWF6\n" +
                        "b24xFDASBgNVBAsTC0lBTSBDb25zb2xlMRIwEAYDVQQDEwlUZXN0Q2lsYWMxHzAd\n" +
                        "BgkqhkiG9w0BCQEWEG5vb25lQGFtYXpvbi5jb20wHhcNMTEwNDI1MjA0NTIxWhcN\n" +
                        "MTIwNDI0MjA0NTIxWjCBiDELMAkGA1UEBhMCVVMxCzAJBgNVBAgTAldBMRAwDgYD\n" +
                        "VQQHEwdTZWF0dGxlMQ8wDQYDVQQKEwZBbWF6b24xFDASBgNVBAsTC0lBTSBDb25z\n" +
                        "b2xlMRIwEAYDVQQDEwlUZXN0Q2lsYWMxHzAdBgkqhkiG9w0BCQEWEG5vb25lQGFt\n" +
                        "YXpvbi5jb20wgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAMaK0dn+a4GmWIWJ\n" +
                        "21uUSfwfEvySWtC2XADZ4nB+BLYgVIk60CpiwsZ3G93vUEIO3IyNoH/f0wYK8m9T\n" +
                        "rDHudUZg3qX4waLG5M43q7Wgc/MbQITxOUSQv7c7ugFFDzQGBzZswY6786m86gpE\n" +
                        "Ibb3OhjZnzcvQAaRHhdlQWIMm2nrAgMBAAEwDQYJKoZIhvcNAQEFBQADgYEAtCu4\n" +
                        "nUhVVxYUntneD9+h8Mg9q6q+au")
        );

        return "mule_config/test-mule-ec2-iddoc-auth-config.xml";
    }

    @Test
    public void testEc2IdDocConnection() throws Exception {
        String payloadValue = ((String) flowRunner("getSecretFlow")
                .run()
                .getMessage()
                .getPayload()
                .getValue());

        assertThat(payloadValue, containsString("test_value1"));
    }

    @Test
    public void testEc2MetadataConnection() throws Exception {
        String payloadValue = ((String) flowRunner("getSecretMDSFlow")
                .run()
                .getMessage()
                .getPayload()
                .getValue());

        assertThat(payloadValue, containsString("test_value1"));
    }
}
