package de.unipassau.sep19.hafenkran.clusterservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ResourceNotFoundException extends ResponseStatusException {

    public ResourceNotFoundException(Class resourceType, String attribute, String identifier, Throwable err) {
        super(HttpStatus.NOT_FOUND,
                String.format("Error: Resource of type %s with '%s'='%s' not found!", resourceType.getName(), attribute,
                        identifier), err);
    }

    public ResourceNotFoundException(Class resourceType, String attribute, String identifier) {
        this(resourceType, attribute, identifier, null);
    }

    public ResourceNotFoundException(Class resourceType) {
        super(HttpStatus.NOT_FOUND, String.format("Error: Resource of type %s not found!", resourceType.getName()));
    }

}
