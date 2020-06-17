package com.avioconsulting.mule.connector.vault.provider.api;

import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import static java.lang.System.lineSeparator;
import java.io.Serializable;

public class VaultResponseAttributes implements Serializable {

    private static final long serialVersionUID = 6732018957718386183L;
    private MultiMap<String,String> headers;
    private int statusCode;
    private String reasonPhrase;

    public VaultResponseAttributes(HttpResponse httpResponse) {
        this.statusCode = httpResponse.getStatusCode();
        this.reasonPhrase = httpResponse.getReasonPhrase();
        if (httpResponse.getHeaders() != null) {
            this.headers = httpResponse.getHeaders().toImmutableMultiMap();
        }
    }

    public MultiMap<String,String> getHeaders() {
        return headers;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("statusCode=");
        sb.append(statusCode);
        sb.append(lineSeparator());
        sb.append("reasonPhrase=");
        sb.append(reasonPhrase);
        sb.append(lineSeparator());
        for (String key : headers.keySet()) {
            sb.append(key);
            sb.append("=");
            sb.append(headers.get(key));
            sb.append(lineSeparator());
        }
        return sb.toString();
    }
}
