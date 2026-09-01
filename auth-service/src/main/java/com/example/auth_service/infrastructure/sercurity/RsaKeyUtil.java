// helper load key từ chuỗi  private để ký taoj token chỉ cho auth service
package com.example.auth_service.infrastructure.sercurity;

import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
@Component
public class RsaKeyUtil {

    // tạo PrivateKey để ký token từ base64key
    public static PrivateKey loadPrivateKey(String base64Key) {
        try {
            byte[] decoded = Base64.getDecoder().decode(base64Key);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decoded));
        } catch (Exception e) {
            throw new IllegalStateException("Không load được RSA private key", e);
        }
    }

}