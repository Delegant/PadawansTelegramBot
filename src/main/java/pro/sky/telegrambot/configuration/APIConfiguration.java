package pro.sky.telegrambot.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class APIConfiguration {


    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("AstanaAnimalShelter Bot API")
                                .version("0.1.0")
                                .contact(
                                        new Contact()
                                                .url("https://github.com/Delegant/PadawansTelegramBot")
                                                .name("Java-Padawans Team")
                                )
                );
    }

}