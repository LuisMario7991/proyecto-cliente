
public class Commands {
    private String uploadAzure;
    private String shareFile;
    private String validateFiles;
    private String uploadFile;
    private String downloadFile;
    private String addUser;
    private String deleteUser;
    private String finishConnection;

    protected Commands() {
        this.uploadAzure = "subeArchivo";
        this.shareFile = "comparteArchivo";
        this.validateFiles = "validaArchivo";
        this.uploadFile = "recibeArchivo";
        this.downloadFile = "enviaArchivo";
        this.addUser = "agregaUsuario";
        this.deleteUser = "eliminaUsuario";
        this.finishConnection = "terminaConexion";
    }

    public String getUploadAzure() {
        return uploadAzure;
    }

    public String getShareFile() {
        return shareFile;
    }

    public String getValidateFiles() {
        return validateFiles;
    }

    public String getUploadFile() {
        return uploadFile;
    }

    public String getDownloadFile() {
        return downloadFile;
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