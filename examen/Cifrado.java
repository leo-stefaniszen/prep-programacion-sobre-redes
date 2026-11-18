package examen;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * CIFRADO AES/CBC/PKCS5Padding, UTF-8, salida en Base64.
 * Formato del mensaje cifrado:  IV_en_Base64:Mensaje_en_Base64
 *
 * ⚠️ CLAVE: tiene que ser LA MISMA en los dos lados. Para el servidor del
 *    profe, usá la clave de su clase Cifrado del TPC_chat (GitHub de la materia).
 *    AES necesita una clave de 16, 24 o 32 bytes.
 */
public class Cifrado {

    private static final String CLAVE = "ClaveSecreta1234";   // 16 bytes → AES-128
    private static final String ALGORITMO = "AES/CBC/PKCS5Padding";

    private static SecretKeySpec clave() {
        return new SecretKeySpec(CLAVE.getBytes(StandardCharsets.UTF_8), "AES");
    }

    /** Texto plano → "IV:cifrado" (los dos en Base64). El IV es nuevo y al azar cada vez. */
    public static String encriptar(String texto) throws Exception {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);                         // IV aleatorio, lo genera quien envía

        Cipher cipher = Cipher.getInstance(ALGORITMO);
        cipher.init(Cipher.ENCRYPT_MODE, clave(), new IvParameterSpec(iv));
        byte[] cifrado = cipher.doFinal(texto.getBytes(StandardCharsets.UTF_8));

        Base64.Encoder b64 = Base64.getEncoder();
        return b64.encodeToString(iv) + ":" + b64.encodeToString(cifrado);
    }

    /** "IV:cifrado" → texto plano. Si no tiene el formato, tira excepción. */
    public static String desencriptar(String linea) throws Exception {
        String[] partes = linea.split(":", 2);
        if (partes.length != 2) {
            throw new IllegalArgumentException("No tiene el separador ':'");
        }
        Base64.Decoder b64 = Base64.getDecoder();
        byte[] iv = b64.decode(partes[0]);
        byte[] cifrado = b64.decode(partes[1]);

        Cipher cipher = Cipher.getInstance(ALGORITMO);
        cipher.init(Cipher.DECRYPT_MODE, clave(), new IvParameterSpec(iv));
        return new String(cipher.doFinal(cifrado), StandardCharsets.UTF_8);
    }

    /**
     * Intenta desencriptar EN SILENCIO: si falla (era texto plano, no tiene ':',
     * Base64 inválido, clave distinta...), devuelve la línea original.
     * Así nunca se cae el programa ni aparecen errores rojos.
     */
    public static String intentarDesencriptar(String linea) {
        try {
            return desencriptar(linea);
        } catch (Exception e) {
            return linea;
        }
    }
}
