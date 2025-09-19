package com.quietjournal.config;

import com.quietjournal.util.SupabaseProperties;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class SupabaseConfig {

    private final Dotenv dotenv = Dotenv.load();
    private final String supabaseUrl = dotenv.get("SUPABASE_URL");
    private final String supabaseKey = dotenv.get("SUPABASE_KEY");



    @Bean
    public WebClient supabaseWebClient() {
        return WebClient.builder()
                .baseUrl(supabaseUrl + "/storage/v1/object")
                .defaultHeader("apikey", supabaseKey)
                .defaultHeader("Authorization", "Bearer " + supabaseKey)
                .build();


    }
    @Bean
    public SupabaseProperties supabaseProperties() {
        return new SupabaseProperties(
                dotenv.get("SUPABASE_URL"),
                dotenv.get("SUPABASE_KEY"),
                dotenv.get("SUPABASE_BUCKET")
        );
    }

}





