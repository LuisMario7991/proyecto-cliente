import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.*;
import java.util.Arrays;

import javax.crypto.*;
import javax.crypto.spec.*;
import javafx.stage.Stage;

public class Conexion {

    final static String SERVER_ADDRESS = "localhost";
    final static int SERVER_PORT = 12345;
    protected static Socket socket;

    private static Commands command = new Commands();

    protected static DataInputStream dataInputStream;
    protected static DataOutputStream dataOutputStream;
    protected static ObjectInputStream objectInputStream;
    protected static ObjectOutputStream objectOutputStream;

    public static void connect() {
        while (true) {
            try {
                ;
                Conexion.socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                System.out.println("Conectado");

                Conexion.objectOutputStream = new ObjectOutputStream(Conexion.socket.getOutputStream());
                Conexion.objectInputStream = new ObjectInputStream(Conexion.socket.getInputStream());
                Conexion.dataInputStream = new DataInputStream(Conexion.socket.getInputStream());
                Conexion.dataOutputStream = new DataOutputStream(Conexion.socket.getOutputStream());

                System.out.println("Esperando recibir parámetros Diffie-Hellman");
                break;
            } catch (Exception e) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    System.err.println("Error en la espera de cliente nuevo: " + e.getMessage());
                }
            }
        }
    }

    public static void setupKeys() {
        try {
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
            PublicKey bobPublicKey = (PublicKey) Conexion.objectInputStream.readObject();
            if (!Utilidades.validatePublicKey(bobPublicKey)) {
                throw new IllegalArgumentException("Clave pública recibida es inválida");
            }
            System.out.println("Clave pública de Bob recibida y validada.");

            // Enviar clave pública a Bob
            Conexion.objectOutputStream.writeObject(publicKey);
            Conexion.objectOutputStream.flush();
            System.out.println("Clave pública de Alice enviada a Bob.");

            // Generar la clave compartida
            KeyAgreement keyAgree = KeyAgreement.getInstance("DH");
            keyAgree.init(privateKey);
            keyAgree.doPhase(bobPublicKey, true);
            byte[] sharedSecret = keyAgree.generateSecret();

            // Calcular hash SHA-256 de la clave compartida
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] sharedSecretHash = sha256.digest(sharedSecret);
            byte[] first16Bytes = Arrays.copyOf(sharedSecretHash, 16);
            System.out.println("Clave compartida hash (Alice): " + Utilidades.bytesToHex(sharedSecretHash));

            // Guarda el hash en un archivo TXT
            String fileName = "DHAESKEY.bin";
            Files.write(Paths.get(fileName), first16Bytes, StandardOpenOption.CREATE);

            // new Thread(new ConnectionHandler()).start();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    protected static String login(String email, String password) {
        try {
            System.out.println("Validando credenciales");

            String userType = UserManagement.authenticateUser(email, password);

            if (userType.equals("INVALID") || userType.equals("NOT_FOUND")) {
                System.out.println("Error de autenticación: " + userType);
            } else {
                System.out.println("Bienvenido/a");
            }

            return userType;
        } catch (Exception e) {
            System.err.println("Hubo un problema con la lectura de la respuesta: " + e);
            return null;
        }
    }

    protected static void displayUserInterface(String userType, Stage primaryStage) {
        if (userType.equals("colaborador")) {
            ColaboratorInterface.showColaboratorInterface(primaryStage);
        }
    }

    protected static void cerrarConexion() {
        try {
            Conexion.dataOutputStream.writeUTF(command.getFinishConnection());
            Conexion.dataOutputStream.flush();

            if (Conexion.objectInputStream != null)
                Conexion.objectInputStream.close();

            if (Conexion.objectOutputStream != null)
                Conexion.objectOutputStream.close();

            if (Conexion.dataInputStream != null)
                Conexion.dataInputStream.close();

            if (Conexion.dataOutputStream != null)
                Conexion.dataOutputStream.close();

            if (Conexion.socket != null && !Conexion.socket.isClosed())
                Conexion.socket.close();

            System.out.println("Conexión cerrada correctamente.");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("No se pudo cerrar correctamente la conexión: " + e.getMessage());
        }
    }
}
