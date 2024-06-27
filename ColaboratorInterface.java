import java.io.IOException;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ColaboratorInterface {

    public static void showColaboratorInterface(Stage primaryStage) {
        primaryStage.setTitle("Colaborador");
        Button btnFirmarAcuerdo = new Button("Firmar Acuerdo");
        Button btnDesencriptarReceta = new Button("Desencriptar Receta");
        Button btnSalir = new Button("Salir");

        btnFirmarAcuerdo.setOnAction(e -> firmarAcuerdo());
        btnDesencriptarReceta.setOnAction(e -> desencriptarReceta());
        btnSalir.setOnAction(e -> {
            Conexion.cerrarConexion();
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
        // Implementación de desencriptación de receta
    }
}
