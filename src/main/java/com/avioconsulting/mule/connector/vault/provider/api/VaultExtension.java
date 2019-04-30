package com.avioconsulting.mule.connector.vault.provider.api;

import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.extension.api.annotation.Configurations;
import org.mule.runtime.extension.api.annotation.dsl.xml.Xml;


/**
 * This is the main class of the extension, it is the entry point from which configurations, connection providers,
 * operations and sources are going to be declared.
 */
@Xml(prefix = "vault")
@Extension(name = "Vault")
@Configurations(VaultConfiguration.class)
public class VaultExtension {

}
