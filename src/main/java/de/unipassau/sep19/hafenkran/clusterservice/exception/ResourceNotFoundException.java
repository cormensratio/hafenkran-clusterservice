package de.unipassau.sep19.hafenkran.clusterservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(Class resourceType, String attribute, String identifier, Throwable err) {
        super(String.format("Error: Resource of type %s with '%s'='%s' not found!", resourceType.getName(), attribute, identifier), err);
    }

    public ResourceNotFoundException(Class resourceType, String attribute, String identifier) {
        this(resourceType, attribute, identifier, null);
    }

    public ResourceNotFoundException(Class resourceType){
        super(String.format("Error: Resource of type %s not found!", resourceType.getName()));
    }

}
