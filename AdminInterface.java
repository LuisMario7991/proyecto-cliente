import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.PublicKey;

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

public class AdminInterface {

    private static Commands command = new Commands();

    public static void showAdminInterface(Stage primaryStage) {
        primaryStage.setTitle("Administrador");
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
            Conexion.cerrarConexion();
            primaryStage.close();
        });

        VBox vbox = new VBox(10, subirButton, compartirButton, validarButton, agregarButton, eliminarButton,
                salirButton);
        Scene scene = new Scene(vbox, 300, 200);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private static void subirArchivo() {
        // Crear un selector de archivos usando JFileChooser
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            try {
                File selectedFile = fileChooser.getSelectedFile();

                OutputStream outputStream = Conexion.socket.getOutputStream(); // Pruebas

                FileInputStream fileInputStream = new FileInputStream(selectedFile);

                long fileSize = selectedFile.length();
                String fileName = selectedFile.getName();

                // Enviar el nombre del archivo y su tamaño
                Conexion.dataOutputStream.writeUTF(fileName);
                Conexion.dataOutputStream.writeLong(fileSize);
                Conexion.dataOutputStream.flush();

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

    private static void compartirArchivo() {
        // Lógica para compartir archivo
        System.out.println("Compartiendo archivo...");
        // Aquí se debería implementar la lógica para enviar el archivo al cliente
        // mediante sockets
        // Ejemplo básico:
        // enviarArchivoAlCliente();
    }

    private static void validarArchivo() {
        try {
            // Aplicar hash SHA-256 al archivo 'm.txt'
            byte[] fileHash;
            fileHash = Utilidades.hashFile("received_m.txt");
            PublicKey publicKey = Utilidades.getPublicKeyFromFile("receivedPublicKey.pem");

            byte[] decryptedHash = Utilidades.decryptWithPublicKey("received_encrypted_hash.bin",
                    publicKey);

            // Aplicar hash SHA-256 al resultado del descifrado
            byte[] decryptedHashFileHash = Utilidades.hashBytes(decryptedHash);

            // Comparar los hashes
            boolean isMatch = MessageDigest.isEqual(fileHash, decryptedHashFileHash);
            System.out.println("Hash comparison result: " + (isMatch ? "MATCH" : "DO NOT MATCH"));
            System.out.println("Validando archivo...");

        } catch (Exception e) {
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
                Conexion.dataOutputStream.writeUTF(command.getAddUser());
                Conexion.dataOutputStream.flush();

                Conexion.dataOutputStream.writeUTF(emailField.getText());
                Conexion.dataOutputStream.flush();

                Conexion.dataOutputStream.writeUTF(passwordField.getText());
                Conexion.dataOutputStream.flush();

                Conexion.dataOutputStream.writeUTF(typeComboBox.getValue());
                Conexion.dataOutputStream.flush();

                // Espera una respuesta del servidor
                String serverResponse = Conexion.dataInputStream.readUTF();
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
                Conexion.dataOutputStream.writeUTF(command.getDeleteUser());
                Conexion.dataOutputStream.flush();

                Conexion.dataOutputStream.writeUTF(emailField.getText());
                Conexion.dataOutputStream.flush();

                // Espera una respuesta del servidor
                String serverResponse = Conexion.dataInputStream.readUTF();
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
}
