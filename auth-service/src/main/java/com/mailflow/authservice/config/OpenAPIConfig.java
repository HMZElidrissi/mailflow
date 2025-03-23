package com.mailflow.authservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
    info =
        @Info(
            title = "mailflow - auth-service",
            version = "v1",
            description = "Auth service for mailflow",
            contact = @Contact(name = "Hamza El Idrissi", url = "https://hmzelidrissi.ma"),
            license = @License(name = "MIT License", url = "https://opensource.org/licenses/MIT"),
            termsOfService = "some terms of service"),
    servers = {
      @Server(url = "http://localhost:8081", description = "Local server"),
    },
    security = {@SecurityRequirement(name = "Authorization")})
@SecurityScheme(
    name = "Authorization",
    description = "JWT token",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER)
public class OpenAPIConfig {}
