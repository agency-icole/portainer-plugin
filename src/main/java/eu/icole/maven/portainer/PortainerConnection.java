package eu.icole.maven.portainer;

public class PortainerConnection {

    String url;
    String user;
    String password;

    PortainerConnection(String url, String user, String password){
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public static PortainerConnection connect(String url, String user, String password){
        PortainerConnection connection = new PortainerConnection(url, user, password);
        return connection;
    }
}
