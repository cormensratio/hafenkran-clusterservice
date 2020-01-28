package de.unipassau.sep19.hafenkran.clusterservice.serviceclient;

import lombok.NonNull;

import java.util.UUID;

/**
 * A service for communicating with the UserService.
 */
public interface UserServiceClient {

    /**
     * Pushes the given boolean, if the user should be deleted or not, to the UserService.
     *
     * @param deleteUser The boolean, if the user should be completely deleted.
     */
    void sendDeleteUserToUserService(@NonNull UUID userId, @NonNull boolean deleteUser);
}