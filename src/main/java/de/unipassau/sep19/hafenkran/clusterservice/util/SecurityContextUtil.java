package de.unipassau.sep19.hafenkran.clusterservice.util;

import de.unipassau.sep19.hafenkran.clusterservice.dto.UserDTO;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

public class SecurityContextUtil {

    public static UserDTO getCurrentUserDTO(){
        final UserDTO currentUser;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof UsernamePasswordAuthenticationToken) {
            UsernamePasswordAuthenticationToken authToken = (UsernamePasswordAuthenticationToken) auth;
            currentUser = (UserDTO) authToken.getPrincipal();
        } else {
            throw new RuntimeException("Invalid user session");
        }
        return currentUser;
    }
}
