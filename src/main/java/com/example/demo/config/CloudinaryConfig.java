package com.example.demo.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.cdimascio.dotenv.Dotenv;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        String cloudinaryUrl = dotenv.get("CLOUDINARY_URL");
        
        // Si no se encuentra la variable (ej. en los tests), usamos una de prueba
        if (cloudinaryUrl == null || cloudinaryUrl.isEmpty()) {
            cloudinaryUrl = "cloudinary://123456789:abcdefghijklmnopq@dummy_cloud";
        }
        
        return new Cloudinary(cloudinaryUrl);
    }
}
