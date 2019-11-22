package de.unipassau.sep19.hafenkran.clusterservice.util;

import de.unipassau.sep19.hafenkran.clusterservice.dto.UserDTO;
import de.unipassau.sep19.hafenkran.clusterservice.exception.InvalidJwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.RequiredTypeException;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.function.Function;

/**
 * A utility class for working with JWTs.
 */
@Component
public class JwtTokenUtil implements Serializable {

    private final String jwtSecret;

    public JwtTokenUtil(@Value("${jwt.secret}") String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    /**
     * Reads the {@link UUID} expected in the subject of the given JWT.
     *
     * @param token the JWT from where to read the id of the user.
     * @return the {@link UUID} in the subject.
     */
    public UUID getUserIdFromToken(@NonNull @NotEmpty String token) {
        return UUID.fromString(getClaimFromToken(token, Claims::getSubject));
    }

    /**
     * Retrieves the {@link UserDTO} from the given JWT.
     * Throws a {@link InvalidJwtException} if the token does not contain the UserDTO.
     *
     * @param token the JWT from where to read the {@link UserDTO}
     * @return the submitted {@link UserDTO}
     */
    public UserDTO getUserDTOFromToken(@NonNull @NotEmpty String token) {
        final Claims claims = getAllClaimsFromToken(token);
        final UserDTO userDTO;
        try {
            LinkedHashMap userInformation = claims.get("user", LinkedHashMap.class);
            userDTO = new UserDTO(
                    UUID.fromString(userInformation.get("id").toString()),
                    userInformation.get("name").toString(),
                    userInformation.get("email").toString(),
                    userInformation.get("isAdmin").toString().equals("true")
            );
        } catch (RequiredTypeException | IllegalArgumentException e) {
            throw new InvalidJwtException(UserDTO.class, "user", e);
        }

        return userDTO;
    }

    private Date getExpirationDateFromToken(@NonNull @NotEmpty String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    private <T> T getClaimFromToken(@NonNull @NotEmpty String token, @NonNull Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(@NonNull @NotEmpty String token) {
        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(@NonNull @NotEmpty String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * Validates the JWT whether it is expired
     *
     * @param token the JWT
     * @return {@code true} if token is valid
     */
    public Boolean validateToken(@NonNull @NotEmpty String token) {
        return !isTokenExpired(token);
    }

}