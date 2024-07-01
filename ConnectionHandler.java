import java.io.IOException;

class ConnectionHandler implements Runnable {
    byte[] key;

    @Override
    public void run() {
        try {
            // El servidor ahora espera por comandos
            while (!Conexion.socket.isClosed()) {
                try {
                    System.out.println("Esperando intrucción del cliente");
                    String command = Conexion.dataInputStream.readUTF();
                    CommandProcessor.processCommand(command);
                } catch (IOException e) {
                    System.out.println("Error al leer el comando: " + e.getMessage());
                    Conexion.cerrarConexion();
                }
            }
            Thread.sleep(10000);
        } catch (Exception e) {
            System.out.println("Error en el exchangeKeys(): " + e.getMessage());
        }
    }
}