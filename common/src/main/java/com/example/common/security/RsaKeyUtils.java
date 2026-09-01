// helper load key từ chuỗi  public  để giải mã token dùng chung cho các service
package com.example.common.security;


import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RsaKeyUtils {
    // tạo PublicKey để ký verify token từ base64key
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
