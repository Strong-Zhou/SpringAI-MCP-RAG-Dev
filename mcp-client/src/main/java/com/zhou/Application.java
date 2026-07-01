package com.zhou;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
    org.springframework.ai.model.transformers.autoconfigure.TransformersEmbeddingModelAutoConfiguration.class
})
public class Application {
    
    public static void main(String[] args) {
        
        Dotenv dotenv = Dotenv.configure().ignoreIfMalformed().load();
        
        dotenv.entries().forEach(entry -> 
            System.setProperty(entry.getKey(), entry.getValue())
        );
        
        SpringApplication.run(Application.class, args);
    }
}