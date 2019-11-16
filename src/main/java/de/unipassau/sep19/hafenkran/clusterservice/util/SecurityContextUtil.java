package de.unipassau.sep19.hafenkran.clusterservice.util;

import de.unipassau.sep19.hafenkran.clusterservice.config.JwtAuthentication;
import de.unipassau.sep19.hafenkran.clusterservice.dto.UserDTO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * A utility method for extracting information about the current user stored in the {@link org.springframework.security.core.context.SecurityContext}.
 */
public class SecurityContextUtil {

    /**
     * Retrieves the {@link UserDTO} for the current user.
     *
     * @return {@link UserDTO} with information about the current user.
     */
    public static UserDTO getCurrentUserDTO() {
        final UserDTO currentUser;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthentication) {
            JwtAuthentication authToken = (JwtAuthentication) auth;
            currentUser = (UserDTO) authToken.getDetails();
        } else {
            throw new RuntimeException("Invalid user session");
        }
        return currentUser;
    }
}
