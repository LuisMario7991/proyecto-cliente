
public class Commands {
    private String uploadFile;
    private String addUser;
    private String deleteUser;
    private String finishConnection;

    protected Commands() {
        this.uploadFile = "recibeArchivo";
        this.addUser = "agregaUsuario";
        this.deleteUser = "eliminaUsuario";
        this.finishConnection = "terminaConexion";
    }

    public String getUploadFile() {
        return uploadFile;
    }

    public String getAddUser() {
        return addUser;
    }

    public String getDeleteUser() {
        return deleteUser;
    }

    public String getFinishConnection() {
        return finishConnection;
    }
}