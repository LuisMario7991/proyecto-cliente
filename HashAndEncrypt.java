import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import javax.crypto.Cipher;

public class HashAndEncrypt {
    public static void hashAndEncrypt() throws Exception {
        // Leer el archivo 'm.txt'
        byte[] messageBytes = Files.readAllBytes(Paths.get("m.txt"));

        // Calcular el hash SHA-256 del contenido
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(messageBytes);

        // Leer la llave privada desde el archivo
        byte[] privateKeyBytes = Files.readAllBytes(Paths.get("keys/privateKey.pem"));
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

        // Cifrar el hash con la llave privada
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        byte[] encryptedHash = cipher.doFinal(hash);

        // Guardar el hash cifrado en un archivo
        Files.write(Paths.get("encrypted_hash.bin"), encryptedHash);
    }
}