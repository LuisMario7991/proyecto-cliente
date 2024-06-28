import com.azure.storage.blob.BlobServiceClientBuilder;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.FileOutputStream;
import java.io.IOException;

public class AzureBlobManager extends Application {

    private static final String connectionString = "https://criptografia.blob.core.windows.net/recetas?sp=racwdli&st=2024-06-26T05:33:45Z&se=2024-06-28T13:33:45Z&sv=2022-11-02&sr=c&sig=uPPamXwmkNlP69aTGs0bP8BFrCo39o5X3Smed7gazVE%3D";
    String containerName = "recetas";
    private final BlobServiceClientBuilder builder;
    private final ListView<String> blobListView;
    private final ObservableList<String> blobNames;

    public AzureBlobManager() {
        this.builder = new BlobServiceClientBuilder().connectionString(connectionString);
        this.blobListView = new ListView<>();
        this.blobNames = FXCollections.observableArrayList();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Descargar Blob desde Azure");

        // Listar blobs disponibles al iniciar la aplicación
        listarBlobs();

        blobListView.setItems(blobNames);

        Button selectButton = new Button("Seleccionar");
        selectButton.setOnAction(event -> {
            String selectedBlob = blobListView.getSelectionModel().getSelectedItem();
            if (selectedBlob != null) {
                descargarBlob(selectedBlob);
            }
        });

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(blobListView);
        borderPane.setBottom(selectButton);

        Scene scene = new Scene(borderPane, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void listarBlobs() {
        try {
            builder.buildClient().getBlobContainerClient(containerName).listBlobs().forEach(blobItem -> {
                blobNames.add(blobItem.getName());
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void descargarBlob(String blobName) {
        try {
            System.out.println("Descargando blob: " + blobName);

            byte[] blobContent = builder.buildClient().getBlobContainerClient(containerName)
                    .getBlobClient(blobName).downloadContent().toBytes();

            String localFilePath = "downloaded_receta";
            FileOutputStream fileOutputStream = new FileOutputStream(localFilePath);
            fileOutputStream.write(blobContent);
            fileOutputStream.close();

            System.out.println("Blob descargado y guardado localmente como: " + localFilePath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}