package com.avioconsulting.mule.vault.api.client.response;

import java.util.ArrayList;
import java.util.List;

public class ErrorResponse {
    List<String> errors = new ArrayList<>();

    public ErrorResponse() {
        super();
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

}
