package com.githubpilot.githubP.Security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.githubpilot.githubP.Service.githubOauth2UserService;
import java.util.List;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final githubOauth2UserService githubOauth2UserService;

    public SecurityConfig(githubOauth2UserService githubOauth2UserService) {
        this.githubOauth2UserService = githubOauth2UserService;
    }

    @Value("${app.frontend-url}")
    private String frontendUrl;

    
    @Bean
    public SecurityFilterChain doFilter(HttpSecurity http, AuthenticationSuccessHandler oauth2SuccessHandler,
            AuthenticationFailureHandler oauth2FailureHandle)
   {
        http.
            cors(Customizer.withDefaults()).
            csrf(csrf-> csrf.disable()).
            sessionManagement(session-> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)).
            authorizeHttpRequests(auth->auth.requestMatchers("/api/auth/login","/oauth2/**","/login/oauth2/**","/error").permitAll()
        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
    .requestMatchers("/api/**").authenticated()
.anyRequest().permitAll())
.exceptionHandling(ex-> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
.oauth2Login(oauth-> oauth
    .userInfoEndpoint(userInfo-> userInfo.userService(githubOauth2UserService))
    .successHandler(oauth2SuccessHandler)
    .failureHandler(oauth2FailureHandle)
)
.logout(logout-> logout.logoutUrl("/api/auth/logout")
.deleteCookies("DEVPILOT_SESSION")
.logoutSuccessHandler((req, res, auth)-> res.setStatus(HttpStatus.NO_CONTENT.value()))
.invalidateHttpSession(true)
.clearAuthentication(true)
);


return http.build();

   }


@Bean
AuthenticationSuccessHandler oauth2SuccessHandler(@Value("${app.frontend-url}") String frontendURL)
{
    SimpleUrlAuthenticationSuccessHandler handler= new SimpleUrlAuthenticationSuccessHandler();
    handler.setDefaultTargetUrl(frontendURL+"/auth/callback");
    return handler;
}


@Bean
AuthenticationFailureHandler oauth2FailureHandler(@Value("${app.frontend-url}") String frontendUrl)
{
    SimpleUrlAuthenticationFailureHandler handler= new SimpleUrlAuthenticationFailureHandler();
    handler.setDefaultFailureUrl(frontendUrl+"/login?error=oauth_failed");
    return handler;
}

@Bean
public CorsConfigurationSource corsConfigurationSource() {

    CorsConfiguration configuration = new CorsConfiguration();

    configuration.setAllowedOrigins(
        List.of(frontendUrl)
    );

    configuration.setAllowedMethods(
        List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")
    );

    configuration.setAllowedHeaders(
        List.of("*")
    );

    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source =
        new UrlBasedCorsConfigurationSource();

    source.registerCorsConfiguration("/**", configuration);

    return source;
}
}
