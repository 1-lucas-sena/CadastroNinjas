
package lucassena.CadastroDeNinjas.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cadastroDeNinjasOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("CadastroDeNinjas API")
                        .description("""
                                API REST para gerenciamento de ninjas,
                                missões e suas regras de associação.
                                Desenvolvida com Java 21 e Spring Boot.
                                """)
                        .version("1.0.0")
                );
    }
}
