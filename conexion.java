import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class conexion {
    final static String SERVER_ADDRESS = "localhost";
    final static int SERVER_PORT = 12345;

    static Socket socket;

    public static void show(Stage secondStage) throws IOException {

        ObjectInputStream ois;
        ObjectOutputStream oos;

        try {
            // Conectar al servidor
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            System.out.println("Conectado a Bob en " + SERVER_ADDRESS + ":" + SERVER_PORT);

            // Inicializar flujos de entrada y salida
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());

            // Recibir parámetros Diffie-Hellman de Bob
            BigInteger p = (BigInteger) ois.readObject();
            BigInteger g = (BigInteger) ois.readObject();
            int l = ois.readInt();
            DHParameterSpec dhSpec = new DHParameterSpec(p, g, l);
            System.out.println("Parámetros Diffie-Hellman recibidos.");

            // Generar par de claves Diffie-Hellman
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("DH");
            keyPairGen.initialize(dhSpec);
            KeyPair keyPair = keyPairGen.generateKeyPair();

            // Obtener clave pública y privada
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();

            // Recibir clave pública de Bob
            PublicKey bobPublicKey = (PublicKey) ois.readObject();
            System.out.println("Clave pública de Bob recibida.");

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

            // Configurar la interfaz gráfica
            secondStage.setTitle("Pantalla con 3 Botones");

            Button btnFirmarAcuerdo = new Button("Firmar Acuerdo");
            Button btnDesencriptarReceta = new Button("Desencriptar Receta");
            Button btnSalir = new Button("Salir");

            btnFirmarAcuerdo.setOnAction(e -> {
                try {
                    // Generar las llaves RSA
                    GenerateKeys.generate();

                    // Aplicar el hash y encriptar con la llave privada
                    HashAndEncrypt.hashAndEncrypt();
                    enviarArchivo();
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            });
            btnDesencriptarReceta.setOnAction(e -> desencriptarReceta());
            btnSalir.setOnAction(e -> cerrarConexion());

            VBox vbox = new VBox(btnFirmarAcuerdo, btnDesencriptarReceta, btnSalir);
            Scene scene = new Scene(vbox, 300, 200);

            secondStage.setScene(scene);
            secondStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void enviarArchivo() throws IOException {

        String[] filePaths = { "m.txt", "encrypted_hash.bin", "publicKey.pem" };
        for (String filePath : filePaths) {
            enviarArch(filePath);
        }
    }

    private static void desencriptarReceta() {
        // Implementar lógica para desencriptar receta
    }

    private static void cerrarConexion() {
        /*
         * try {
         * // Cerrar flujos y socket
         * if (ois != null) ois.close();
         * if (oos != null) oos.close();
         * if (socket != null && !socket.isClosed()) socket.close();
         * System.out.println("Conexión cerrada correctamente.");
         * } catch (IOException e) {
         * e.printStackTrace();
         * }
         */
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

    public static void enviarArch(String filePath) throws IOException {
        OutputStream outputStream = socket.getOutputStream();

        FileInputStream fileInputStream = new FileInputStream(filePath);
        {

            File file = new File(filePath);
            long fileSize = file.length();
            String fileName = file.getName();

            // Enviar el nombre del archivo y su tamaño
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            dataOutputStream.writeUTF(fileName);
            dataOutputStream.writeLong(fileSize);

            System.out.println("Enviando archivo: " + fileName + " de tamaño: " + fileSize + " bytes");

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            System.out.println("Archivo " + fileName + " enviado al servidor.");

        }

    }

}
