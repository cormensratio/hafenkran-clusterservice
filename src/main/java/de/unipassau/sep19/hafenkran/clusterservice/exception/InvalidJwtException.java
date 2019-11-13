package de.unipassau.sep19.hafenkran.clusterservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class InvalidJwtException extends RuntimeException {

    public InvalidJwtException(Class resourceType, String claim, Throwable err) {
        super(String.format("Error: The given token does not contain a claim of type %s with name %s.", resourceType,
                claim), err);
    }

    public InvalidJwtException(Class resourceType, String claim) {
        this(resourceType, claim, null);
    }

}
