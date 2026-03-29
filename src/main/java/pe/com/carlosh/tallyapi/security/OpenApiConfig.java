package pe.com.carlosh.tallyapi.security;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI(){
        final String securitySchemeName = "bearerAuth";

        Server localServer = new Server();

        Server productionServer = new Server();

        localServer.setUrl("http://localhost:8080");
        localServer.setDescription("Servidor de pruebas locales para tally API");

        productionServer.setUrl("https://tally-api-production-53b0.up.railway.app");
        productionServer.setDescription("Servidor de pruebas en producción para Tally");

        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .servers(List.of(localServer,productionServer));
    }
}
