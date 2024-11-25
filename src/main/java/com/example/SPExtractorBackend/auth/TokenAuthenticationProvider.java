package com.example.SPExtractorBackend.auth;

import com.microsoft.graph.authentication.IAuthenticationProvider;
import okhttp3.Request;

import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class TokenAuthenticationProvider implements IAuthenticationProvider {

    private final String accessToken;

    public TokenAuthenticationProvider(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public CompletableFuture<String> getAuthorizationTokenAsync(URL requestUrl) {
        // Return the token asynchronously as required by the interface
        return CompletableFuture.completedFuture(accessToken);
    }

    public void authenticateRequest(Request.Builder requestBuilder) {
        // Add the Bearer token to the Authorization header
        requestBuilder.addHeader("Authorization", "Bearer " + accessToken);
    }
}
