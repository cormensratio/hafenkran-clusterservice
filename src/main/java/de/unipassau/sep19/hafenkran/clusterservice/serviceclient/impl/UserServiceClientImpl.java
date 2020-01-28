package de.unipassau.sep19.hafenkran.clusterservice.serviceclient.impl;

import de.unipassau.sep19.hafenkran.clusterservice.serviceclient.ServiceClient;
import de.unipassau.sep19.hafenkran.clusterservice.serviceclient.UserServiceClient;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * {@inheritDoc}
 */
@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class UserServiceClientImpl implements UserServiceClient {

    private final ServiceClient serviceClient;

    @Value("${user-service-uri}")
    private String basePath;

    @Value("${service-user.secret}")
    private String serviceSecret;

    /**
     * {@inheritDoc}
     */
    public void sendDeleteUserToUserService(@NonNull UUID userId, @NonNull boolean deleteUser) {
        serviceClient.post(basePath + "/users/delete/{id}" + deleteUser, userId, String.class, null);
    }

}