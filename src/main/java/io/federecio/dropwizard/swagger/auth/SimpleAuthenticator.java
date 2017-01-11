// Copyright (C) 2014 Federico Recio
/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.federecio.dropwizard.swagger.auth;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * @author jay
 */

public class SimpleAuthenticator implements Authenticator<BasicCredentials, SwaggerUser> {

    /**
     * Valid users with mapping user -> roles and user->password (hash)
     */
    private Map<String, Set<String>> userRolesMap;
    private Map<String, String> userPasswordMap;

    public SimpleAuthenticator(List<UserConfig> users) {
        this.userRolesMap = new HashMap<>();
        this.userPasswordMap = new HashMap<>();
        for (UserConfig currentConfig : users) {
            userRolesMap.put(currentConfig.getName(), new HashSet<>(currentConfig.getRoles()));
            userPasswordMap.put(currentConfig.getName(), currentConfig.getPassword());

        }
    }

    @Override
    public Optional<SwaggerUser> authenticate(BasicCredentials credentials) throws AuthenticationException {
        if (userPasswordMap.containsKey(credentials.getUsername()) && userPasswordMap.get(credentials.getUsername()).equals(encryptPassword(credentials.getPassword()))) {
            return Optional.of(new SwaggerUser(credentials.getUsername(), userRolesMap.get(credentials.getUsername())));
        }
        return Optional.empty();
    }

    private String encryptPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(password.getBytes());

            byte byteData[] = md.digest();

            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < byteData.length; i++) {
                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            //In case of exception return an empty string
            return "";
        }
    }
}
