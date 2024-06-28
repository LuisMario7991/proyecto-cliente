import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.crypto.spec.DHParameterSpec;
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
        Button generarParamsButton = new Button("Generar parámetros Diffie-Hellman");
        Button salirButton = new Button("Salir");

        subirButton.setOnAction(e -> subirArchivo());
        compartirButton.setOnAction(e -> compartirArchivo());
        validarButton.setOnAction(e -> validarArchivo());
        agregarButton.setOnAction(e -> agregaUsuario());
        eliminarButton.setOnAction(e -> eliminaUsuario());
        generarParamsButton.setOnAction(e -> generaParams());
        salirButton.setOnAction(e -> salir(primaryStage));

        VBox vbox = new VBox(10, subirButton, compartirButton, validarButton, agregarButton, eliminarButton,
                generarParamsButton, salirButton);
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
                Conexion.dataOutputStream.writeUTF(command.getUploadAzure());
                File selectedFile = fileChooser.getSelectedFile();

                FileInputStream fileInputStream = new FileInputStream(selectedFile);

                long fileSize = selectedFile.length();
                String fileName = selectedFile.getName();

                // Conexion.dataOutputStream.writeUTF(command.getUploadFile());
                // Conexion.dataOutputStream.flush();

                // Enviar el nombre del archivo y su tamaño
                Conexion.dataOutputStream.writeUTF(fileName);
                Conexion.dataOutputStream.writeLong(fileSize);
                Conexion.dataOutputStream.flush();

                System.out.println("Enviando archivo: " + fileName + " de tamaño: " + fileSize + " bytes");

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    Conexion.dataOutputStream.write(buffer, 0, bytesRead);
                    Conexion.dataOutputStream.flush();
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
        AzureBlobManager.launch(AzureBlobManager.class);
        String localFilePath = "downloaded_receta";
        try {
            Utilidades.enviarArchivo(localFilePath);
        } catch (IOException e) {
            System.err.println("Error al comparit archivo: " + e.getMessage());
        }
    }

    private static void validarArchivo() {
        try {
            System.out.println("Validando archivo en el serivdor");

            Conexion.dataOutputStream.writeUTF(command.getValidateFiles());

            // Comparar los hashes
            boolean isMatch = Conexion.dataInputStream.readBoolean();
            String validationText = (isMatch) ? "Validación exitosa:" : "Validación fallida:";
            String messageText = (isMatch) ? "El acuerdo fue verificado y cumple con los requisitos."
                    : "El acuerdo no coincide. Pida que firmen el acuerdo nuevamente.";

            // Crear una ventana para ingresar datos del usuario
            Stage stage = new Stage();
            GridPane grid = new GridPane();
            Button acceptButton = new Button("Aceptar");

            grid.add(new Label("               "), 1, 0);
            grid.add(new Label("        "), 0, 1);
            grid.add(new Label(validationText), 1, 1);
            grid.add(new Label("        "), 2, 1);
            grid.add(new Label("        "), 0, 2);
            grid.add(new Label(messageText), 1, 2);
            grid.add(new Label("        "), 2, 2);
            grid.add(new Label("               "), 1, 3);
            grid.add(acceptButton, 1, 4);
            grid.add(new Label("               "), 1, 5);

            acceptButton.setOnAction(e -> stage.close());

            Scene scene = new Scene(grid);
            stage.setScene(scene);
            stage.setTitle("Validación de Acuerdo");
            stage.show();

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

    private static void generaParams() {
        try {
            DHKeyExchange.ServerDH dhKeyExchange = new DHKeyExchange.ServerDH();
            DHParameterSpec dhParametersSpecs = dhKeyExchange.getDhSpec();
            DHKeyExchange.DHParams dhParams = new DHKeyExchange.DHParams(dhParametersSpecs.getP(),
                    dhParametersSpecs.getG(), dhParametersSpecs.getL());

            dhParams.saveToFile("Bob/dh_params", dhParams);
            Utilidades.enviarArchivo("Bob/dh_params");
        } catch (Exception e) {
            System.err.println("Hubo un error al generar parámetro Diffie-Hellman: " + e.getMessage());
        }
    }

    private static void salir(Stage stage) {
        Conexion.cerrarConexion();
        stage.close();
    }
}
