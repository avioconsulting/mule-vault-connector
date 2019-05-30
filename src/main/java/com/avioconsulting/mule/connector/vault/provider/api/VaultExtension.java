package com.avioconsulting.mule.connector.vault.provider.api;

import com.avioconsulting.mule.connector.vault.provider.api.error.VaultErrors;
import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.extension.api.annotation.Configurations;
import org.mule.runtime.extension.api.annotation.dsl.xml.Xml;
import org.mule.runtime.extension.api.annotation.error.ErrorTypes;


/**
 * This is the main class of the extension, it is the entry point from which configurations, connection providers,
 * operations and sources are going to be declared.
 */
@Xml(prefix = "vault")
@Extension(name = "Vault")
@ErrorTypes(VaultErrors.class)
@Configurations(VaultConfiguration.class)
public class VaultExtension {

}
