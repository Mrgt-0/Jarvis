package com.jarvis.Config;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI jarvisOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Jarvis API")
                        .description("""
                                API для интеллектуального анализа Java-кода.
                                                   \s
                                                    ### Основные возможности:
                                                    - **Анализ кода**: Поиск ошибок, уязвимостей, code smells
                                                    - **Интеллектуальные подсказки**: Объяснения и рекомендации
                                                    - **Управление проектами**: История анализов
                                                   \s
                                                    ### Авторизация:
                                                    Пока не требуется (будет добавлена в будущем)
                                                    """)
                        .version("1.0.0"))
                .externalDocs(new ExternalDocumentation()
                        .description("Документация проекта")
                        .url("https://github.com/Mrgt-0/Jarvis"))
                .tags(List.of(
                        new Tag().name("Files").description("Загрузка и управление файлами")
                ))
                .components(new Components()
                        .addSchemas("MultipartFile", new Schema()
                                .type("string")
                                .format("binary")
                                .description("Загружаемый файл")));
    }
}