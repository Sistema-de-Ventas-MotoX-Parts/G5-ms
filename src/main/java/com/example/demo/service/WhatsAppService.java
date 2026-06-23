package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
public class WhatsAppService {

    @Value("${whatsapp.api.url:https://graph.facebook.com/v18.0}")
    private String apiUrl;

    @Value("${whatsapp.phone.number.id:YOUR_PHONE_NUMBER_ID}")
    private String phoneNumberId;

    @Value("${whatsapp.access.token:YOUR_ACCESS_TOKEN}")
    private String accessToken;

    @Value("${whatsapp.enabled:false}")
    private boolean enabled;

    private final RestTemplate restTemplate;

    public WhatsAppService() {
        this.restTemplate = new RestTemplate();
    }

    public void enviarPinWhatsApp(String telefono, String pin) {
        if (telefono == null || telefono.trim().isEmpty()) {
            System.out.println("Aviso WhatsApp: No se envió el mensaje porque la orden no tiene teléfono de contacto.");
            return;
        }

        if (!enabled) {
            System.out.println("\n[SIMULACIÓN WHATSAPP] Enviando mensaje a " + telefono + ":");
            System.out.println("--------------------------------------------------");
            System.out.println("¡Hola! Tu motocicleta está lista para retirar en el taller.");
            System.out.println("Tu PIN de seguridad es: " + pin);
            System.out.println("Recuerda que este PIN expira en 15 minutos.");
            System.out.println("--------------------------------------------------\n");
            return;
        }

        try {
            String url = apiUrl + "/" + phoneNumberId + "/messages";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            // Estructura oficial para la API de Meta (WhatsApp Cloud API)
            // Se envía como tipo 'text' para el entorno de desarrollo
            Map<String, Object> body = new HashMap<>();
            body.put("messaging_product", "whatsapp");
            body.put("to", telefono);
            body.put("type", "text");

            Map<String, Object> text = new HashMap<>();
            text.put("body", "¡Hola! Tu motocicleta está lista para retirar en el taller. Tu PIN de seguridad es: " + pin + ". Recuerda que este PIN expira en 15 minutos.");
            body.put("text", text);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            System.out.println("WhatsApp enviado exitosamente. Respuesta de Meta: " + response.getBody());
        } catch (Exception e) {
            System.err.println("Error enviando mensaje de WhatsApp: " + e.getMessage());
        }
    }
}
