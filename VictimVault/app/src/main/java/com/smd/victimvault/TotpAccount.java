package com.smd.victimvault;

import androidx.annotation.NonNull;

import java.security.SecureRandom;

public class TotpAccount {

    private String serviceName;
    private String username;
    private String currentCode;
    private SecureRandom random = new SecureRandom();

    public TotpAccount(@NonNull String serviceName,@NonNull String username) {
        this.serviceName = serviceName;
        this.username = username;
        regenerateCode();
    }

    public void regenerateCode() {
        int code = 100000 + random.nextInt(900000);
        this.currentCode = String.valueOf(code);
    }
    @NonNull
    public String getServiceName() {
        return serviceName;
    }
    @NonNull
    public String getUsername() {
        return username;
    }
    @NonNull
    public String getCurrentCode() {
        return currentCode;
    }
}