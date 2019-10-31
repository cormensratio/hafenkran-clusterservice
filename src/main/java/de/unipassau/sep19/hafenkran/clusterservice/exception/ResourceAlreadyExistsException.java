package de.unipassau.sep19.hafenkran.clusterservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ResourceAlreadyExistsException extends RuntimeException {

    public ResourceAlreadyExistsException(Class resourceType, String attribute, String identifier, Throwable err) {
        super(String.format("Error: Resource of type %s with '%s'='%s' already exists!", resourceType.getName(), attribute, identifier), err);
    }

    public ResourceAlreadyExistsException(Class resourceType, String attribute, String identifier) {
        this(resourceType, attribute, identifier, null);
    }

}