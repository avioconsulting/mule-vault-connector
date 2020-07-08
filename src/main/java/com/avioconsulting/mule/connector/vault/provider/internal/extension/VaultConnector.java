package com.avioconsulting.mule.connector.vault.provider.internal.extension;

import com.avioconsulting.mule.connector.vault.provider.internal.configuration.VaultConfiguration;
import com.avioconsulting.mule.connector.vault.provider.internal.error.VaultErrorType;
import com.avioconsulting.mule.connector.vault.provider.api.parameter.proxy.VaultNtlmProxyConfigImpl;
import com.avioconsulting.mule.connector.vault.provider.api.parameter.proxy.VaultProxyConfig;
import com.avioconsulting.mule.connector.vault.provider.api.parameter.proxy.VaultProxyConfigImpl;
import org.mule.runtime.api.meta.Category;
import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.extension.api.annotation.Configurations;
import org.mule.runtime.extension.api.annotation.SubTypeMapping;
import org.mule.runtime.extension.api.annotation.dsl.xml.Xml;
import org.mule.runtime.extension.api.annotation.error.ErrorTypes;
import org.mule.runtime.extension.api.annotation.license.RequiresEnterpriseLicense;


/**
 * This is the main class of the extension, it is the entry point from which configurations, connection providers,
 * operations and sources are going to be declared.
 */
@Xml(prefix = "vault")
@Extension(name = "Vault", category = Category.CERTIFIED, vendor = "AVIO Consulting")
@RequiresEnterpriseLicense(allowEvaluationLicense = true)
@ErrorTypes(VaultErrorType.class)
@Configurations(VaultConfiguration.class)
@SubTypeMapping(baseType = VaultProxyConfig.class, subTypes = {VaultProxyConfigImpl.class, VaultNtlmProxyConfigImpl.class})
public class VaultConnector {
    private VaultConnector() {}
}
