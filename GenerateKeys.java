import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GenerateKeys {
    public static void generate() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair pair = keyGen.generateKeyPair();
        PrivateKey privateKey = pair.getPrivate();
        PublicKey publicKey = pair.getPublic();

        Files.write(Paths.get("keys/privateKey.pem"), privateKey.getEncoded());
        Files.write(Paths.get("keys/publicKey.pem"), publicKey.getEncoded());

        // Print the content of the private key file
        byte[] privateKeyBytes = Files.readAllBytes(Paths.get("keys/privateKey.pem"));
        System.out.println("Private Key:\n" + bytesToHex(privateKeyBytes));

        // Print the content of the public key file
        byte[] publicKeyBytes = Files.readAllBytes(Paths.get("keys/publicKey.pem"));
        System.out.println("Public Key:\n" + bytesToHex(publicKeyBytes));
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
