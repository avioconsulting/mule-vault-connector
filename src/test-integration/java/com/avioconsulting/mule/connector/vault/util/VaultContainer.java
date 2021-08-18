package com.avioconsulting.mule.connector.vault.util;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.Capability;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

public class VaultContainer implements TestRule {

    private static final Logger logger = LoggerFactory.getLogger(VaultContainer.class);

    public final static String CURRENT_WORKING_DIRECTORY = System.getProperty("user.dir");
    public final static String SSL_DIRECTORY = CURRENT_WORKING_DIRECTORY + File.separator + "ssl";
    public final static String CERT_PEMFILE = SSL_DIRECTORY + File.separator + "root-cert.pem";

    public final static String CLIENT_CERT_PEMFILE = SSL_DIRECTORY + File.separator + "client-cert.pem";
    public final static String CLIENT_PRIVATE_KEY_PEMFILE = SSL_DIRECTORY + File.separator + "client-privatekey.pem";
    public final static String CLIENT_KEYSTORE = SSL_DIRECTORY + File.separator + "keystore.jks";
    public final static String CLIENT_KEYSTORE_PASSWORD = "password";
    public final static String CLIENT_TRUSTSTORE = SSL_DIRECTORY + File.separator + "truststore.jks";

    public final static String CONTAINER_STARTUP_SCRIPT = "/vault/config/startup.sh";
    public final static String CONTAINER_CONFIG_FILE = "/vault/config/config.json";
    public final static String CONTAINER_OPENSSL_CONFIG_FILE = "/vault/config/libressl.conf";
    public final static String CONTAINER_SSL_DIRECTORY = "/vault/config/ssl";
    public final static String CONTAINER_CERT_PEMFILE = CONTAINER_SSL_DIRECTORY + "/vault-cert.pem";
    public final static String CONTAINER_CLIENT_CERT_PEMFILE = CONTAINER_SSL_DIRECTORY + "/client-cert.pem";
    public final static String CONTAINER_WEB_POLICY_FILE = "/vault/config/web_policy.hcl";

    private final GenericContainer container;

    private String unsealKey;
    private String rootToken;
    private String cipherText;
    private boolean kv2Enabled = false;

    private static File[] getResourceFolderFiles (String folder) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL url = loader.getResource(folder);
        String path = url.getPath();
        return new File(path).listFiles();
    }

    public VaultContainer() {
        container = new GenericContainer("vault:1.4.2")
                .withClasspathResourceMapping("startup.sh", CONTAINER_STARTUP_SCRIPT, BindMode.READ_ONLY)
                .withClasspathResourceMapping("config.json", CONTAINER_CONFIG_FILE, BindMode.READ_ONLY)
                .withClasspathResourceMapping("libressl.conf", CONTAINER_OPENSSL_CONFIG_FILE, BindMode.READ_ONLY)
                .withClasspathResourceMapping("web_policy.hcl", CONTAINER_WEB_POLICY_FILE, BindMode.READ_ONLY)
                .withEnv("VAULT_VERSION", "1.4.2")
                .withFileSystemBind(SSL_DIRECTORY, CONTAINER_SSL_DIRECTORY, BindMode.READ_WRITE)
                .withCreateContainerCmdModifier(new Consumer<CreateContainerCmd>() {
                    @Override
                    public void accept(final CreateContainerCmd createContainerCmd) {
                        createContainerCmd.withCapAdd(Capability.IPC_LOCK);
                    }
                })
                .withExposedPorts(8200, 8280)
                .withCommand("/bin/sh " + CONTAINER_STARTUP_SCRIPT)
                .waitingFor(Wait.forHttp("/v1/sys/health").forStatusCode(501)); // 501 is the "uninitialized" status code
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return container.apply(base, description);
    }

    public void initAndUnsealVault() throws IOException, InterruptedException {
        final Slf4jLogConsumer logConsumer = new Slf4jLogConsumer(logger);
        container.followOutput(logConsumer);

        // Initialize the Vault server
        final Container.ExecResult initResult = runCommand("vault", "operator", "init", "-ca-cert=" +
                CONTAINER_CERT_PEMFILE, "-key-shares=1", "-key-threshold=1", "-format=json");
        for (String line : initResult.getStdout().replaceAll(System.lineSeparator(), "").split(",")) {
            if (line.contains("unseal_keys_b64")) {
                this.unsealKey = line.split(":")[1].
                        replace("[", "").
                        replace("]","").
                        replace("\"","").
                        trim();
            } else if (line.contains("root_token")) {
                this.rootToken = line.split(":")[1].
                        replace("\"","").
                        replace("}", "").
                        trim();
            }
        }

        logger.info(String.format("Unseal Key: %s", this.unsealKey));
        logger.info(String.format("Root token: %s", this.rootToken));

        // Unseal the Vault server
        runCommand("vault", "operator", "unseal", "-ca-cert=" + CONTAINER_CERT_PEMFILE, unsealKey);
    }

    public void setupSampleSecret() throws IOException, InterruptedException {
        runCommand("vault", "kv", "put", "-ca-cert=" + CONTAINER_CERT_PEMFILE, "secret/test/mysecret", "att1=test_value1",
                "att2=test_value2");
    }

    public void enableKvSecretsV2() throws IOException, InterruptedException {
        if (!kv2Enabled) {
            runCommand("vault", "login", "-ca-cert=" + CONTAINER_CERT_PEMFILE, rootToken);
            runCommand("vault", "secrets", "enable", "-ca-cert=" + CONTAINER_CERT_PEMFILE, "-version=2", "-path=secret", "kv");
            kv2Enabled = true;
        }
    }

    public void setupTransitEngine() throws IOException, InterruptedException {
        runCommand("vault", "login", "-ca-cert=" + CONTAINER_CERT_PEMFILE, rootToken);
        runCommand("vault", "secrets", "enable", "-ca-cert=" + CONTAINER_CERT_PEMFILE, "-path=transit", "transit");
        runCommand("vault", "write", "-ca-cert=" + CONTAINER_CERT_PEMFILE, "-f", "transit/keys/testKey");
        final Container.ExecResult result = runCommand("vault", "write", "-ca-cert=" + CONTAINER_CERT_PEMFILE, "transit/encrypt/testKey", "plaintext=cGxhaW50ZXh0Cg==", "-format=json");
        for (String line : result.getStdout().replaceAll(System.lineSeparator(), "").split(",")) {
            if (line.contains("ciphertext")) {
                this.cipherText = line.
                        replace("\"data\": {", "").
                        trim().
                        replace("\"ciphertext\": \"", "").
                        trim().
                        replace("\"","").
                        replace("}","").
                        trim();
            }
        }
        logger.info(String.format("CIPHERTEXT: %s", this.cipherText));
        runCommand("vault", "secrets", "list", "-ca-cert=" + CONTAINER_CERT_PEMFILE);
    }

    /**
     * Prepares the Vault server for testing of the TLS Certificate auth backend (i.e. mounts the backend and registers
     * the certificate and private key for client auth).
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public void setupBackendCert() throws IOException, InterruptedException {
        runCommand("vault", "login", "-ca-cert=" + CONTAINER_CERT_PEMFILE, rootToken);

        runCommand("vault", "auth", "enable", "-ca-cert=" + CONTAINER_CERT_PEMFILE, "cert");
        runCommand("vault", "policy", "write", "-ca-cert=" + CONTAINER_CERT_PEMFILE, "web", CONTAINER_WEB_POLICY_FILE);
        runCommand("vault", "write", "-ca-cert=" + CONTAINER_CERT_PEMFILE, "auth/cert/certs/web", "display_name=web",
                "policies=web", "certificate=@" + CONTAINER_CLIENT_CERT_PEMFILE, "ttl=3600");
    }

    private Container.ExecResult runCommand(final String... command) throws IOException, InterruptedException {
        logger.info("Command: {}", String.join(" ", command));
        final Container.ExecResult result = this.container.execInContainer(command);
        final String out = result.getStdout();
        final String err = result.getStderr();
        if (out != null && !out.isEmpty()) {
            logger.info("Command stdout: {}", result.getStdout());
        }
        if (err != null && !err.isEmpty()) {
            logger.info("Command stderr: {}", result.getStderr());
        }
        return result;
    }

    public String getAddress() {
        return String.format("https://%s:%d", container.getContainerIpAddress(), container.getMappedPort(8200));
    }

    public String getHttpAddress() {
        return String.format("http://%s:%d", container.getContainerIpAddress(), container.getMappedPort(8280));
    }

    public String getRootToken() {
        return rootToken;
    }

    public String getCipherText() {
        return cipherText;
    }
}
