package br.com.yourpethealth.security;

import br.com.yourpethealth.config.RedirectPorPerfilHandler;
import br.com.yourpethealth.dto.response.ApiErroResponse;
import br.com.yourpethealth.service.UsuarioDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.LocalDateTime;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final SecurityFilter jwtFilter;
    private final UsuarioDetailsService usuarioDetailsService;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private final RedirectPorPerfilHandler redirectPorPerfilHandler;

    @Bean
    @Order(1)
    SecurityFilterChain apiChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/api/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                        .requestMatchers("/api/consultas/agenda").hasRole("VETERINARIO")
                        .requestMatchers(HttpMethod.PATCH, "/api/consultas/*/concluir")
                        .hasRole("VETERINARIO")
                        .requestMatchers("/api/pets/buscar").hasRole("VETERINARIO")
                        .requestMatchers(HttpMethod.GET, "/api/veterinarios/**").authenticated()
                        .requestMatchers("/api/veterinarios/**").hasRole("ADMIN")
                        .requestMatchers("/api/responsaveis/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(jsonAuthEntryPoint())
                        .accessDeniedHandler(jsonAccessDeniedHandler()))
                .build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain webChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/login", "/cadastro", "/error",
                                "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
                                "/css/**", "/js/**", "/img/**").permitAll()
                        .requestMatchers("/vet/**").hasRole("VETERINARIO")
                        .requestMatchers("/app/**").hasRole("RESPONSAVEL")
                        .anyRequest().authenticated())
                .formLogin(f -> f
                        .loginPage("/login")
                        .successHandler(redirectPorPerfilHandler)
                        .permitAll())
                .logout(l -> l
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout"))
                .exceptionHandling(e -> e.accessDeniedPage("/403"))
                .build();
    }


    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    DaoAuthenticationProvider authenticationProvider() {
        var provider = new DaoAuthenticationProvider(usuarioDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }


    private AuthenticationEntryPoint jsonAuthEntryPoint() {
        return (request, response, ex) -> escrever(response,
                HttpServletResponse.SC_UNAUTHORIZED, "NAO_AUTORIZADO",
                "Autenticação necessária", request.getRequestURI());
    }

    private AccessDeniedHandler jsonAccessDeniedHandler() {
        return (request, response, ex) -> escrever(response,
                HttpServletResponse.SC_FORBIDDEN, "ACESSO_NEGADO",
                "Você não tem permissão para acessar este recurso", request.getRequestURI());
    }

    private void escrever(HttpServletResponse response, int status,
                          String codigo, String mensagem, String path) throws java.io.IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(),
                new ApiErroResponse(LocalDateTime.now(), status, codigo, mensagem, path, null));
    }
}
