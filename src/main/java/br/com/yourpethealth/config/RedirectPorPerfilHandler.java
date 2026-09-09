package br.com.yourpethealth.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RedirectPorPerfilHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication auth) throws IOException {

        boolean veterinario = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_VETERINARIO"));

        getRedirectStrategy().sendRedirect(request, response,
                veterinario ? "/vet/agenda" : "/app/home");
    }
}