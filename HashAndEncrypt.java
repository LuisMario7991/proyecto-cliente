import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;

public class HashAndEncrypt {
    public static void hashAndEncrypt() throws Exception {
        // Leer el archivo 'm.txt'
        byte[] messageBytes = Files.readAllBytes(Paths.get("m.txt"));

        // Calcular el hash SHA-256 del contenido
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(messageBytes);

        // Leer la llave privada desde el archivo
        byte[] privateKeyBytes = Files.readAllBytes(Paths.get("privateKey.pem"));
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

        // Firmar el hash con la llave privada
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(hash);
        byte[] signedHash = signature.sign();

        // Guardar el hash firmado en un archivo
        Files.write(Paths.get("encrypted_hash.bin"), signedHash);
    }
}
