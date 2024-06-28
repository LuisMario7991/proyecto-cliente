import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JFileChooser;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ColaboratorInterface {

    public static void showColaboratorInterface(Stage primaryStage) {
        primaryStage.setTitle("Colaborador");
        Button btnFirmarAcuerdo = new Button("Firmar Acuerdo");
        Button btnRecibirReceta = new Button("Recibir receta");
        Button btnDesencriptarReceta = new Button("Desencriptar Receta");
        Button btnSalir = new Button("Salir");

        btnFirmarAcuerdo.setOnAction(e -> firmarAcuerdo());
        btnRecibirReceta.setOnAction(e -> desencriptarReceta());
        btnDesencriptarReceta.setOnAction(e -> recibirarchivo());
        btnSalir.setOnAction(e -> salir(primaryStage));

        VBox vbox = new VBox(btnFirmarAcuerdo, btnDesencriptarReceta, btnSalir);
        Scene scene = new Scene(vbox, 300, 200);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private static void firmarAcuerdo() {
        try {
            GenerateKeys.generate();
            HashAndEncrypt.hashAndEncrypt();
            enviarFirma();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void enviarFirma() throws IOException {
        String[] filePaths = { "m.txt", "encrypted_hash.bin", "publicKey.pem" };
        for (String filePath : filePaths) {
            Utilidades.enviarArchivo(filePath);
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

    private static void recibirarchivo() {
        try {
            InputStream inputStream = Conexion.socket.getInputStream();
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

            System.out.println("Archivo recibido y guardado como: " + filePath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void salir(Stage stage) {
        Conexion.cerrarConexion();
        stage.close();
    }
}
