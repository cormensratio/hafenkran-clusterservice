package de.unipassau.sep19.hafenkran.clusterservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

public class InvalidJwtException extends ResponseStatusException {

    public InvalidJwtException(Class resourceType, String claim, Throwable err) {
        super(HttpStatus.UNAUTHORIZED, String.format("Error: The given token does not contain a claim of type %s with name %s.", resourceType,
                claim), err);
    }

    public InvalidJwtException(Class resourceType, String claim) {
        this(resourceType, claim, null);
    }

}
