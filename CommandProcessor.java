import java.io.IOException;

public class CommandProcessor {

    public static void processCommand(String command) throws IOException {
        switch (command) {
            case "enviaArchivo":
                FileManagement.recibirArchivo();
                break;
            case "comparteArchivo":
                FileManagement.recibirArchivo();
                break;
            case "terminaConexion":
                Conexion.cerrarConexion();
                break;
            default:
                Conexion.dataOutputStream.writeUTF("Comando desconocido");
                break;
        }
    }
}
