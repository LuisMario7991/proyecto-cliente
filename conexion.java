import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.*;
import javax.crypto.spec.*;
import javax.swing.JFileChooser;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class conexion {

    final static String SERVER_ADDRESS = "localhost";
    final static int SERVER_PORT = 12345;
    static Socket socket;
    private static ObjectOutputStream objectOutputStream;
    private static ObjectInputStream objectInputStream;
    private static DataOutputStream dataOutputStream;
    private static DataInputStream dataInputStream;

    public static void main(String[] args) {
        try {
            connect();
            setupKeys();
            LoginScreen.initialize(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
        if (!validatePublicKey(bobPublicKey)) {
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
          byte[] first16Bytes = Arrays.copyOf(sharedSecret, 16);

        // Calcular hash SHA-256 de la clave compartida
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] sharedSecretHash = sha256.digest(sharedSecret);
        System.out.println("Clave compartida hash (Alice): " + bytesToHex(sharedSecretHash));
        
        String fileName = "hasht.txt";
        Files.write(Paths.get(fileName), first16Bytes, StandardOpenOption.CREATE);
    }

    // @SuppressWarnings("unused")
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
            showAdminInterface(primaryStage);
        } else if (userType.equals("colaborador")) {
            showColaboratorInterface(primaryStage);
        }
    }

    public static void showColaboratorInterface(Stage primaryStage) {
        primaryStage.setTitle("Pantalla con 3 Botones");
        Button btnFirmarAcuerdo = new Button("Firmar Acuerdo");
        Button btnDesencriptarReceta = new Button("Desencriptar Receta");
        Button btnSalir = new Button("Salir");

        btnFirmarAcuerdo.setOnAction(e -> firmarAcuerdo());
        btnDesencriptarReceta.setOnAction(e -> desencriptarReceta());
        btnSalir.setOnAction(e -> {
            cerrarConexion();
            primaryStage.close();
        });

        VBox vbox = new VBox(btnFirmarAcuerdo, btnDesencriptarReceta, btnSalir);
        Scene scene = new Scene(vbox, 300, 200);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private static void firmarAcuerdo() {
        try {
            GenerateKeys.generate();
            HashAndEncrypt.hashAndEncrypt();

            dataOutputStream.writeUTF("recibirArchivo");
            dataOutputStream.flush();

            enviarArchivo();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void cerrarConexion() {
        try {
            dataOutputStream.writeUTF("terminaConexion");
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

    public static void showAdminInterface(Stage primaryStage) {
        primaryStage.setTitle("File Sharing App");
        Button subirButton = new Button("Subir");
        Button compartirButton = new Button("Compartir");
        Button validarButton = new Button("Validar");
        Button agregarButton = new Button("Agregar usuario");
        Button eliminarButton = new Button("Eliminar usuario");
        Button salirButton = new Button("Salir");

        subirButton.setOnAction(e -> subirArchivo());
        compartirButton.setOnAction(e -> compartirArchivo());
        validarButton.setOnAction(e -> validarArchivo());
        agregarButton.setOnAction(e -> agregaUsuario());
        eliminarButton.setOnAction(e -> eliminaUsuario());
        salirButton.setOnAction(e -> {
            cerrarConexion();
            primaryStage.close();
        });

        VBox vbox = new VBox(10, subirButton, compartirButton, validarButton, agregarButton, eliminarButton,
                salirButton);
        Scene scene = new Scene(vbox, 300, 200);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private static void validarArchivo() {
        try {
            // Aplicar hash SHA-256 al archivo 'm.txt'
            byte[] fileHash;
            fileHash = hashFile("received_m.txt");
            PublicKey publicKey = getPublicKeyFromFile("receivedPublicKey.pem");

            byte[] decryptedHash = decryptWithPublicKey("received_encrypted_hash.bin",
                    publicKey);

            // Aplicar hash SHA-256 al resultado del descifrado
            byte[] decryptedHashFileHash = hashBytes(decryptedHash);

            // Comparar los hashes
            boolean isMatch = MessageDigest.isEqual(fileHash, decryptedHashFileHash);
            System.out.println("Hash comparison result: " + (isMatch ? "MATCH" : "DO NOT MATCH"));
            System.out.println("Validando archivo...");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void compartirArchivo() {
        // Lógica para compartir archivo
        System.out.println("Compartiendo archivo...");
        // Aquí se debería implementar la lógica para enviar el archivo al cliente
        // mediante sockets
        // Ejemplo básico:
        // enviarArchivoAlCliente();
    }

    private static void subirArchivo() {
        // Crear un selector de archivos usando JFileChooser
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            try {
                File selectedFile = fileChooser.getSelectedFile();

                OutputStream outputStream;
                outputStream = socket.getOutputStream();

                FileInputStream fileInputStream = new FileInputStream(selectedFile);

                long fileSize = selectedFile.length();
                String fileName = selectedFile.getName();

                // Enviar el nombre del archivo y su tamaño
                DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
                dataOutputStream.writeUTF(fileName);
                dataOutputStream.writeLong(fileSize);
                dataOutputStream.flush();

                System.out.println("Enviando archivo: " + fileName + " de tamaño: " + fileSize + " bytes");

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    outputStream.flush();
                }

                fileInputStream.close();

                System.out.println("Archivo " + fileName + " enviado al servidor.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private static void enviarArchivo() throws IOException {
        String[] filePaths = { "m.txt", "encrypted_hash.bin", "publicKey.pem" };
        for (String filePath : filePaths) {
            enviarArch(filePath);
        }
    }

    private static void desencriptarReceta() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            // Llamar a la clase AESGCMDecryptor para descifrar el archivo
            try {
                File keyFile = new File("hasht.txt"); // Ruta al archivo de clave
                File outputFile = new File("receta_descifrada.txt"); // Ruta al archivo de salida descifrado

                AESGCMDecryptor.decryptFile(selectedFile, keyFile, outputFile);
                System.out.println("Archivo descifrado correctamente.");
            } catch (Exception e) {
                System.err.println("Error al descifrar el archivo: " + e.getMessage());
                e.printStackTrace();
            }
         }
    }

    private static void recibirarchivo(){
            try {
                    InputStream inputStream = socket.getInputStream();
                    String filePath = "received_file"; // Nombre del archivo recibido
                    FileOutputStream fileOutputStream = new FileOutputStream(filePath);
                    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        bufferedOutputStream.write(buffer, 0, bytesRead);
                    }

                    bufferedOutputStream.close();
                    inputStream.close();
                    socket.close();

                    System.out.println("Archivo recibido y guardado como: " + filePath);

                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

    private static void agregaUsuario() {
        // Crear una ventana para ingresar datos del usuario
        Stage stage = new Stage();
        GridPane grid = new GridPane();
        TextField emailField = new TextField();
        PasswordField passwordField = new PasswordField();
        ComboBox<String> typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll("administrador", "colaborador");
        Button addButton = new Button("Agregar");

        grid.add(new Label("Email:"), 0, 0);
        grid.add(emailField, 1, 0);
        grid.add(new Label("Contraseña:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(new Label("Tipo de Usuario:"), 0, 2);
        grid.add(typeComboBox, 1, 2);
        grid.add(addButton, 1, 3);

        addButton.setOnAction(e -> {
            try {
                // Envía los datos del nuevo usuario al servidor
                dataOutputStream.writeUTF("agregaUsuario");
                dataOutputStream.flush();

                dataOutputStream.writeUTF(emailField.getText());
                dataOutputStream.flush();

                dataOutputStream.writeUTF(passwordField.getText());
                dataOutputStream.flush();

                dataOutputStream.writeUTF(typeComboBox.getValue());
                dataOutputStream.flush();

                // Espera una respuesta del servidor
                String serverResponse = dataInputStream.readUTF();
                System.out.println("Respuesta del servidor: " + serverResponse);

                stage.close(); // Cierra la ventana tras la respuesta
            } catch (IOException ex) {
                System.err.println("Error en el envío de parámetros al servidor: " + ex.getMessage());
            }
        });

        Scene scene = new Scene(grid);
        stage.setScene(scene);
        stage.show();
    }

    private static void eliminaUsuario() {
        // Crear una ventana para eliminar un usuario
        Stage stage = new Stage();
        GridPane grid = new GridPane();
        TextField emailField = new TextField();
        Button deleteButton = new Button("Eliminar");

        grid.add(new Label("Email del usuario a eliminar:"), 0, 0);
        grid.add(emailField, 1, 0);
        grid.add(deleteButton, 1, 1);

        deleteButton.setOnAction(e -> {
            try {
                // Envía los datos del nuevo usuario al servidor
                dataOutputStream.writeUTF("eliminaUsuario");
                dataOutputStream.flush();

                dataOutputStream.writeUTF(emailField.getText());
                dataOutputStream.flush();

                // Espera una respuesta del servidor
                String serverResponse = dataInputStream.readUTF();
                System.out.println("Respuesta del servidor: " + serverResponse);

                stage.close(); // Cierra la ventana tras la respuesta
            } catch (IOException ex) {
                System.err.println("Error en el envío de parámetros al servidor: " + ex.getMessage());
            }
        }); // Cierra la ventana tras la respuesta

        Scene scene = new Scene(grid);
        stage.setScene(scene);
        stage.show();
    }

    private static byte[] hashBytes(byte[] data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(data);
    }

    private static byte[] hashFile(String filePath) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
        return digest.digest(fileBytes);
    }

    private static PublicKey getPublicKeyFromFile(String filePath) throws Exception {
        byte[] publicKeyBytes = Files.readAllBytes(Paths.get(filePath));
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    private static byte[] decryptWithPublicKey(String filePath, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, publicKey);

        byte[] encryptedData = Files.readAllBytes(Paths.get(filePath));
        return cipher.doFinal(encryptedData);
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

    public static void enviarArch(String filePath) throws IOException {
        OutputStream outputStream = socket.getOutputStream();

        FileInputStream fileInputStream = new FileInputStream(filePath);

        File file = new File(filePath);
        long fileSize = file.length();
        String fileName = file.getName();

        // Enviar el nombre del archivo y su tamaño
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        dataOutputStream.writeUTF(fileName);
        dataOutputStream.writeLong(fileSize);
        dataOutputStream.flush();

        System.out.println("Enviando archivo: " + fileName + " de tamaño: " + fileSize + " bytes");

        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
            outputStream.flush();
        }

        fileInputStream.close();

        System.out.println("Archivo " + fileName + " enviado al servidor.");
    }
}
