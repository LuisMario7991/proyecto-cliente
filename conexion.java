import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.*;

import javax.crypto.*;
import javax.crypto.spec.*;
import javafx.stage.Stage;

public class Conexion {

    final static String SERVER_ADDRESS = "localhost";
    final static int SERVER_PORT = 12345;
    static Socket socket;
    protected static ObjectOutputStream objectOutputStream;
    protected static ObjectInputStream objectInputStream;
    protected static DataOutputStream dataOutputStream;
    protected static DataInputStream dataInputStream;

    private static Commands command = new Commands();

    public static void connect() throws Exception {

        while (true) {
            try {
                socket = new Socket(SERVER_ADDRESS, SERVER_PORT);

                System.out.println("Esperando recibir parámetros Diffie-Hellman");

                objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectInputStream = new ObjectInputStream(socket.getInputStream());
                dataInputStream = new DataInputStream(socket.getInputStream());
                dataOutputStream = new DataOutputStream(socket.getOutputStream());
                break;
            } catch (Exception e) {
                Thread.sleep(1000);
            }
        }
    }

    public static void setupKeys() throws Exception {
        System.out.println("Recibiendo parámetros Diffie-Hellman");

        // Recibir y generar parámetros Diffie-Hellman
        BigInteger p = (BigInteger) objectInputStream.readObject();
        BigInteger g = (BigInteger) objectInputStream.readObject();
        int l = objectInputStream.readInt();
        System.out.println("Parámetros Diffie-Hellman recibidos de Bob.");

        DHParameterSpec dhSpec = new DHParameterSpec(p, g, l);
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("DH");
        keyPairGen.initialize(dhSpec);
        KeyPair keyPair = keyPairGen.generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        // Recibir clave pública de Bob
        PublicKey bobPublicKey = (PublicKey) objectInputStream.readObject();
        if (!Utilidades.validatePublicKey(bobPublicKey)) {
            throw new IllegalArgumentException("Clave pública recibida es inválida");
        }
        System.out.println("Clave pública de Bob recibida y validada.");

        // Enviar clave pública a Bob
        objectOutputStream.writeObject(publicKey);
        objectOutputStream.flush();
        System.out.println("Clave pública de Alice enviada a Bob.");

        // Generar la clave compartida
        KeyAgreement keyAgree = KeyAgreement.getInstance("DH");
        keyAgree.init(privateKey);
        keyAgree.doPhase(bobPublicKey, true);
        byte[] sharedSecret = keyAgree.generateSecret();

        // Calcular hash SHA-256 de la clave compartida
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] sharedSecretHash = sha256.digest(sharedSecret);
        System.out.println("Clave compartida hash (Alice): " + Utilidades.bytesToHex(sharedSecretHash));
    }

    protected static String login(String email, String password) {
        try {
            dataOutputStream.writeUTF(email);
            dataOutputStream.flush();
            dataOutputStream.writeUTF(password);
            dataOutputStream.flush();

            String userType = dataInputStream.readUTF(); // Leer el tipo de usuario desde el servidor

            if (userType.equals("INVALID") || userType.equals("NOT_FOUND")) {
                System.out.println("Error de autenticación: " + userType);
            }

            return userType;
        } catch (Exception e) {
            System.err.println("Hubo un problema con la lectura de la respuesta: " + e);
            return null;
        }
    }

    protected static void displayUserInterface(String userType, Stage primaryStage) {
        if (userType.equals("administrador")) {
            AdminInterface.showAdminInterface(primaryStage);
        } else if (userType.equals("colaborador")) {
            ColaboratorInterface.showColaboratorInterface(primaryStage);
        }
    }

    protected static void cerrarConexion() {
        try {
            dataOutputStream.writeUTF(command.getFinishConnection());
            dataOutputStream.flush();

            if (objectInputStream != null)
                objectInputStream.close();

            if (objectOutputStream != null)
                objectOutputStream.close();

            if (dataInputStream != null)
                dataInputStream.close();

            if (dataOutputStream != null)
                dataOutputStream.close();

            if (socket != null && !socket.isClosed())
                socket.close();

            System.out.println("Conexión cerrada correctamente.");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("No se pudo cerrar correctamente la conexión: " + e.getMessage());
        }
    }
}
