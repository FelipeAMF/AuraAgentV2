package com.auraagent.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FirebaseAuthResponse {
    @JsonProperty("idToken")
    private String idToken;

    @JsonProperty("email")
    private String email;
    
    @JsonProperty("localId")
    private String localId;

    // Getters and Setters
    public String getIdToken() { return idToken; }
    public void setIdToken(String idToken) { this.idToken = idToken; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getLocalId() { return localId; }
    public void setLocalId(String localId) { this.localId = localId; }
}