package com.mycompany.myapp.security;

import org.springframework.boot.autoconfigure.security.oauth2.resource.PrincipalExtractor;

import java.util.Map;

public class Oauth2PrincipalExtractor implements PrincipalExtractor{

    @Override
    public Object extractPrincipal(Map<String, Object> map) {
        return map.get("preferred_username");
    }
}
