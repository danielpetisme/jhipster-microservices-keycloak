package com.mycompany.myapp.config;

import com.mycompany.myapp.security.AuthoritiesConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.resource.AuthoritiesExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.PrincipalExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Configuration
@EnableResourceServer
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class MicroserviceSecurityConfiguration extends WebSecurityConfigurerAdapter {


    private final String oauth2ClientPrincipalAttribute;
    private final String oauth2ClientAuthoritiesAttribute;

    private final ResourceServerProperties resourceServerProperties;

    public MicroserviceSecurityConfiguration(@Value("${oauth2.client.principal-attribute") String oauth2ClientPrincipalAttribute, @Value("${oauth2.client.authorities-attribute") String oauth2ClientAuthoritiesAttribute, ResourceServerProperties resourceServerProperties) {
        this.oauth2ClientPrincipalAttribute = oauth2ClientPrincipalAttribute;
        this.oauth2ClientAuthoritiesAttribute = oauth2ClientAuthoritiesAttribute;
        this.resourceServerProperties = resourceServerProperties;
    }

    @Bean
    @Primary
    public UserInfoTokenServices userInfoTokenServices() {
        UserInfoTokenServices userInfoTokenServices = new UserInfoTokenServices(resourceServerProperties.getUserInfoUri(), resourceServerProperties.getClientId());
        userInfoTokenServices.setPrincipalExtractor(principalExtractor());
        userInfoTokenServices.setAuthoritiesExtractor(authoritiesExtractor());
        return userInfoTokenServices;
    }

    public PrincipalExtractor principalExtractor() {
        return (Map<String, Object> map) -> map.getOrDefault(oauth2ClientPrincipalAttribute, "unknown");
    }

    public AuthoritiesExtractor authoritiesExtractor() {
        return (Map<String, Object> map) -> {
            List<String> authorities = Optional.ofNullable((List) map.get(oauth2ClientAuthoritiesAttribute))
                    .filter(it -> !it.isEmpty())
                    .orElse(Collections.singletonList(AuthoritiesConstants.USER));
            return authorities.stream().map(role -> new SimpleGrantedAuthority(role)).collect(toList());
        };
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .csrf()
                .disable()
                .headers()
                .frameOptions()
                .disable()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .requestMatcher(new RequestHeaderRequestMatcher("Authorization"))
                .authorizeRequests()
                .antMatchers("/api/profile-info").permitAll()
                .antMatchers("/api/**").authenticated()
                .antMatchers("/management/health").permitAll()
                .antMatchers("/management/**").hasAuthority(AuthoritiesConstants.ADMIN)
                .antMatchers("/swagger-resources/configuration/ui").permitAll();
    }

    @Bean
    public TokenStore tokenStore(JwtAccessTokenConverter jwtAccessTokenConverter) {
        return new JwtTokenStore(jwtAccessTokenConverter);
    }

    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setVerifierKey(getKeyFromAuthorizationServer());
        return converter;
    }

    private String getKeyFromAuthorizationServer() {
        return Optional.ofNullable(
                new RestTemplate()
                        .exchange(
                                resourceServerProperties.getJwt().getKeyUri(),
                                HttpMethod.GET,
                                new HttpEntity<Void>(new HttpHeaders()),
                                Map.class
                        )
                        .getBody()
                        .get("public_key"))
                .map(publicKey -> "-----BEGIN PUBLIC KEY-----\n" + publicKey + "\n-----END PUBLIC KEY-----")
                .orElse(resourceServerProperties.getJwt().getKeyValue());
    }
}
