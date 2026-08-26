// helper load key từ chuỗi public và private cho mã hóa Rsa 
package com.example.auth_service.security;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RsaKeyUtil {

    public static PrivateKey loadPrivateKey(String base64Key) {
        try {
            byte[] decoded = Base64.getDecoder().decode(base64Key);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decoded));
        } catch (Exception e) {
            throw new IllegalStateException("Không load được RSA private key", e);
        }
    }

    public static PublicKey loadPublicKey(String base64Key) {
        try {
            byte[] decoded = Base64.getDecoder().decode(base64Key);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(new X509EncodedKeySpec(decoded));
        } catch (Exception e) {
            throw new IllegalStateException("Không load được RSA public key", e);
        }
    }

}