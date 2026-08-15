package com.rmm.std.conf;

import com.rmm.std.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final UserDetailsService userDetailsService;
  private final PasswordEncoder passwordEncoder;

  @Bean
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder);
    return provider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http, AuthenticationProvider authenticationProvider) throws Exception {

    http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/auth/register")
                    .permitAll()
                    .requestMatchers("/ping", "/auth/login")
                    .anonymous()
                    .requestMatchers("/health/email", "/health/bucket")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/users/me/grades")
                    .hasRole("STUDENT")
                    .requestMatchers(HttpMethod.POST, "/students/*/transcript/send")
                    .hasAnyRole("STUDENT", "ADMIN")
                    .requestMatchers(
                        HttpMethod.POST,
                        "/careers",
                        "/career-courses",
                        "/course-groups",
                        "/courses",
                        "/course-teachers",
                        "/groups",
                        "/promotions",
                        "/semesters",
                        "/user-groups",
                        "/user-promotions",
                        "/users")
                    .hasRole("ADMIN")
                    .requestMatchers(
                        HttpMethod.PUT,
                        "/careers/*",
                        "/career-courses/*",
                        "/course-groups/*",
                        "/courses/*",
                        "/course-teachers/*",
                        "/groups/*",
                        "/promotions/*",
                        "/semesters/*",
                        "/user-groups/*",
                        "/user-promotions/*",
                        "/users/*")
                    .hasRole("ADMIN")
                    .requestMatchers(
                        HttpMethod.DELETE,
                        "/careers/*",
                        "/career-courses/*",
                        "/course-groups/*",
                        "/courses/*",
                        "/course-teachers/*",
                        "/grades/*",
                        "/groups/*",
                        "/promotions/*",
                        "/semesters/*",
                        "/user-groups/*",
                        "/user-promotions/*",
                        "/users/*")
                    .hasRole("ADMIN")
                    .requestMatchers(
                        HttpMethod.GET,
                        "/promotions/*/graduates/export",
                        "/promotions/*/graduates",
                        "/ui/promotions",
                        "/user-promotions",
                        "/user-promotions/*",
                        "/users")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/exams", "/grades")
                    .hasAnyRole("ADMIN", "TEACHER")
                    .requestMatchers(HttpMethod.PUT, "/exams/*")
                    .hasAnyRole("ADMIN", "TEACHER")
                    .requestMatchers(HttpMethod.DELETE, "/exams/*")
                    .hasAnyRole("ADMIN", "TEACHER")
                    .requestMatchers(HttpMethod.PATCH, "/grades/*/correct")
                    .hasAnyRole("ADMIN", "TEACHER")
                    .requestMatchers(HttpMethod.GET, "/grades", "/user-groups")
                    .hasAnyRole("ADMIN", "TEACHER")
                    .requestMatchers(
                        HttpMethod.GET,
                        "/exams",
                        "/exams/*",
                        "/courses",
                        "/courses/*",
                        "/course-teachers",
                        "/course-teachers/*",
                        "/course-groups",
                        "/course-groups/*",
                        "/groups",
                        "/groups/*",
                        "/semesters",
                        "/semesters/*",
                        "/careers",
                        "/careers/*",
                        "/career-courses",
                        "/career-courses/*",
                        "/promotions",
                        "/promotions/*")
                    .hasAnyRole("ADMIN", "TEACHER", "STUDENT")
                    .requestMatchers(HttpMethod.GET, "/user-groups/*")
                    .hasAnyRole("ADMIN", "TEACHER", "STUDENT")
                    .anyRequest()
                    .authenticated())
        .authenticationProvider(authenticationProvider)
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
