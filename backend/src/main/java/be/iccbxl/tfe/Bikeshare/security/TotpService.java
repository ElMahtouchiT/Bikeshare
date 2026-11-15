package be.iccbxl.tfe.Bikeshare.security;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;

/**
 * Double authentification TOTP (RFC 6238), compatible Microsoft / Google Authenticator.
 * Implémentation sans dépendance externe (JDK uniquement : HMAC-SHA1 + Base32 maison).
 */
@Service
public class TotpService {

    private static final int DIGITS = 6;
    private static final int PERIOD = 30;              // secondes
    private static final String ALGO = "HmacSHA1";
    private static final String ISSUER = "BikeShare";
    private static final String B32 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    private final SecureRandom random = new SecureRandom();

    /** Génère un secret Base32 (160 bits). */
    public String generateSecret() {
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        return base32Encode(bytes);
    }

    /** URI otpauth:// à encoder dans le QR code. */
    public String getOtpAuthUri(String secret, String accountEmail) {
        String label = enc(ISSUER + ":" + accountEmail);
        return "otpauth://totp/" + label + "?secret=" + secret
                + "&issuer=" + enc(ISSUER) + "&algorithm=SHA1&digits=" + DIGITS + "&period=" + PERIOD;
    }

    /** Vérifie un code à 6 chiffres (tolérance ±1 fenêtre pour l'horloge). */
    public boolean verify(String secret, String code) {
        if (secret == null || code == null) return false;
        code = code.trim().replaceAll("\\s", "");
        if (!code.matches("\\d{" + DIGITS + "}")) return false;
        long step = Instant.now().getEpochSecond() / PERIOD;
        for (int w = -1; w <= 1; w++) {
            if (generateCode(secret, step + w).equals(code)) return true;
        }
        return false;
    }

    private String generateCode(String secret, long step) {
        try {
            byte[] key = base32Decode(secret);
            byte[] data = ByteBuffer.allocate(8).putLong(step).array();
            Mac mac = Mac.getInstance(ALGO);
            mac.init(new SecretKeySpec(key, ALGO));
            byte[] h = mac.doFinal(data);
            int off = h[h.length - 1] & 0x0F;
            int bin = ((h[off] & 0x7F) << 24) | ((h[off + 1] & 0xFF) << 16)
                    | ((h[off + 2] & 0xFF) << 8) | (h[off + 3] & 0xFF);
            int otp = bin % (int) Math.pow(10, DIGITS);
            return String.format("%0" + DIGITS + "d", otp);
        } catch (Exception e) {
            return "";
        }
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static String base32Encode(byte[] data) {
        StringBuilder sb = new StringBuilder();
        int buffer = 0, bits = 0;
        for (byte b : data) {
            buffer = (buffer << 8) | (b & 0xFF);
            bits += 8;
            while (bits >= 5) {
                sb.append(B32.charAt((buffer >> (bits - 5)) & 0x1F));
                bits -= 5;
            }
        }
        if (bits > 0) sb.append(B32.charAt((buffer << (5 - bits)) & 0x1F));
        return sb.toString();
    }

    private static byte[] base32Decode(String s) {
        s = s.trim().replace("=", "").toUpperCase();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int buffer = 0, bits = 0;
        for (char c : s.toCharArray()) {
            int val = B32.indexOf(c);
            if (val < 0) continue;
            buffer = (buffer << 5) | val;
            bits += 5;
            if (bits >= 8) {
                out.write((buffer >> (bits - 8)) & 0xFF);
                bits -= 8;
            }
        }
        return out.toByteArray();
    }
}
