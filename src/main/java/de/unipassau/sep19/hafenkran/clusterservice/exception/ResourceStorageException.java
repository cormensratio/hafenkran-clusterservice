package de.unipassau.sep19.hafenkran.clusterservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ResourceStorageException extends ResponseStatusException {

    public ResourceStorageException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    public ResourceStorageException(String message, Throwable cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message, cause);
    }
}