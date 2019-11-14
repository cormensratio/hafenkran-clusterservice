package de.unipassau.sep19.hafenkran.clusterservice.config;

import de.unipassau.sep19.hafenkran.clusterservice.dto.UserDTO;
import de.unipassau.sep19.hafenkran.clusterservice.util.JwtTokenUtil;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * A {@link OncePerRequestFilter} for verifying the JWT sent in the header of a client request.
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {

    @NonNull
    private final JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        final String requestTokenHeader = request.getHeader("Authorization");
        UserDTO userDto = null;
        String jwtToken = null;

        // Retrieve the userId from the JWT if it exists
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                userDto = jwtTokenUtil.getUserDTOFromToken(jwtToken);
            } catch (IllegalArgumentException e) {
                System.out.println("Unable to get JWT Token");
            } catch (ExpiredJwtException e) {
                System.out.println("JWT Token has expired");
            }
        }

        // Set the UserDto as authentication object for the current session if the user is authenticated with a token
        if (userDto != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtTokenUtil.validateToken(jwtToken)) {
                JwtAuthentication jwtAuthentication = new JwtAuthentication(userDto);
                SecurityContextHolder.getContext().setAuthentication(jwtAuthentication);
            }
        }
        chain.doFilter(request, response);
    }

}