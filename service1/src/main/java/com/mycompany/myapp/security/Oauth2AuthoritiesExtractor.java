package com.mycompany.myapp.security;

import org.springframework.boot.autoconfigure.security.oauth2.resource.AuthoritiesExtractor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public class Oauth2AuthoritiesExtractor implements AuthoritiesExtractor {

    @Override
    public List<GrantedAuthority> extractAuthorities(Map<String, Object> map) {
        List<String> roles = (List) map.getOrDefault("roles", new ArrayList<>());
        if (roles.isEmpty()) {
            return singletonList(new SimpleGrantedAuthority(AuthoritiesConstants.USER));
        }
        return roles.stream().map(role -> new SimpleGrantedAuthority(role)).collect(toList());
    }
}
