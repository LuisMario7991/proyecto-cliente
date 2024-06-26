import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class conexion {

    final static String SERVER_ADDRESS = "localhost";
    final static int SERVER_PORT = 12345;
    static Socket socket;
    private static ObjectOutputStream oos;
    private static ObjectInputStream ois;

    public static void main(String[] args) {
        LoginScreen.initialize(args);
    }

    public static void connectAndSetupKeys() throws Exception {
        while (true) {
            try {
                socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                oos = new ObjectOutputStream(socket.getOutputStream());
                ois = new ObjectInputStream(socket.getInputStream());

                // Recibir y generar parámetros Diffie-Hellman
                BigInteger p = (BigInteger) ois.readObject();
                BigInteger g = (BigInteger) ois.readObject();
                int l = ois.readInt();
                System.out.println("Parámetros Diffie-Hellman recibidos de Bob.");

                DHParameterSpec dhSpec = new DHParameterSpec(p, g, l);
                KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("DH");
                keyPairGen.initialize(dhSpec);
                KeyPair keyPair = keyPairGen.generateKeyPair();
                PublicKey publicKey = keyPair.getPublic();
                PrivateKey privateKey = keyPair.getPrivate();

                // Recibir clave pública de Bob
                PublicKey bobPublicKey = (PublicKey) ois.readObject();
                if (!validatePublicKey(bobPublicKey)) {
                    throw new IllegalArgumentException("Clave pública recibida es inválida");
                }
                System.out.println("Clave pública de Bob recibida y validada.");

                // Enviar clave pública a Bob
                oos.writeObject(publicKey);
                oos.flush();
                System.out.println("Clave pública de Alice enviada a Bob.");

                // Generar la clave compartida
                KeyAgreement keyAgree = KeyAgreement.getInstance("DH");
                keyAgree.init(privateKey);
                keyAgree.doPhase(bobPublicKey, true);
                byte[] sharedSecret = keyAgree.generateSecret();

                // Calcular hash SHA-256 de la clave compartida
                MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
                byte[] sharedSecretHash = sha256.digest(sharedSecret);
                System.out.println("Clave compartida hash (Alice): " + bytesToHex(sharedSecretHash));

                break;
            } catch (Exception e) {
                Thread.sleep(100);
            }
        }
    }

    public static void showMainInterface(Stage secondStage) {
        secondStage.setTitle("Pantalla con 3 Botones");
        Button btnFirmarAcuerdo = new Button("Firmar Acuerdo");
        Button btnDesencriptarReceta = new Button("Desencriptar Receta");
        Button btnSalir = new Button("Salir");

        btnFirmarAcuerdo.setOnAction(e -> {
            try {
                GenerateKeys.generate();
                HashAndEncrypt.hashAndEncrypt();
                enviarArchivo();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        btnDesencriptarReceta.setOnAction(e -> desencriptarReceta());
        btnSalir.setOnAction(e -> System.exit(0));

        VBox vbox = new VBox(btnFirmarAcuerdo, btnDesencriptarReceta, btnSalir);
        Scene scene = new Scene(vbox, 300, 200);
        secondStage.setScene(scene);
        secondStage.show();
    }

    private static void enviarArchivo() {
        // Implementación de envío de archivo
    }

    private static void desencriptarReceta() {
        // Implementación de desencriptación de receta
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private static boolean validatePublicKey(PublicKey key) {
        // Implementación de ejemplo: validar la especificación de la clave y cualquier
        // otra propiedad necesaria
        return key.getAlgorithm().equals("DH") && key.getEncoded().length > 0;
    }
}
