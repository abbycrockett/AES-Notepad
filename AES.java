import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.KeySpec;
import java.util.Base64;

public class AES {

	//Constants
    public final static int KEY_LENGTH = 256;
    public final static int COUNTER = 65536;

    // Encrypts plaintext using AES-256 encryption algorithm
    public static String encryptTxt(String plaintext, String key, String salt) {
        try {
            // Generate a random Initialization Vector (iv)
            SecureRandom secureRandom = new SecureRandom();
            byte[] iv = new byte[16];
            secureRandom.nextBytes(iv);
            IvParameterSpec ivspec = new IvParameterSpec(iv);

            // Get a secret key using PBKDF2 with SHA-256
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(key.toCharArray(), salt.getBytes(), COUNTER, KEY_LENGTH);
            SecretKey tempKey = factory.generateSecret(spec);
            SecretKeySpec secretKeySpec = new SecretKeySpec(tempKey.getEncoded(), "AES");

            // Initialize the AES cipher in CBC mode with PKCS5 padding
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivspec);

            // Encrypt the plaintext
            byte[] cipherText = cipher.doFinal(plaintext.getBytes("UTF-8"));

            // Combine IV and cipherText and encode as Base64
            byte[] encryptedData = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, encryptedData, 0, iv.length);
            System.arraycopy(cipherText, 0, encryptedData, iv.length, cipherText.length);

            return Base64.getEncoder().encodeToString(encryptedData);
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Decrypts ciphertext using AES-256 decryption algorithm
    public static String decryptTxt(String ciphertext, String key, String salt) {
        try {
            // Decode the Base64-encoded ciphertext
            byte[] encryptedData = Base64.getDecoder().decode(ciphertext);

            // Extract IV from the encrypted data
            byte[] iv = new byte[16];
            System.arraycopy(encryptedData, 0, iv, 0, iv.length);
            IvParameterSpec ivspec = new IvParameterSpec(iv);

            // Get the secret key using PBKDF2 with SHA-256
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(key.toCharArray(), salt.getBytes(), COUNTER, KEY_LENGTH);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKeySpec = new SecretKeySpec(tmp.getEncoded(), "AES");

            // Initialize the AES cipher in CBC mode with PKCS5 padding
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivspec);

            // Decrypt the ciphertext
            byte[] ciphertextBytes = new byte[encryptedData.length - 16];
            System.arraycopy(encryptedData, 16, ciphertextBytes, 0, ciphertextBytes.length);
            byte[] plaintext = cipher.doFinal(ciphertextBytes);

            return new String(plaintext, "UTF-8");
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

