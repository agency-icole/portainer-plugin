package eu.icole.maven.portainer;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.icole.maven.portainer.model.Authorization;
import eu.icole.maven.portainer.model.Credentials;
import eu.icole.maven.portainer.model.ErrorResponse;
import eu.icole.maven.portainer.model.PortainerException;

import javax.sound.sampled.Port;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class PortainerConnection {

    String url;
    String user;
    String password;

    String jwt;

    Client client;
    WebTarget webTarget;

    PortainerConnection(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    void connect() throws PortainerException {
        client = ClientBuilder.newClient();
        if (!url.endsWith("/"))
            url += "/";
        webTarget = client.target(url + "api");

        WebTarget authWebTarget
                = webTarget.path("auth");

        Invocation.Builder invocationBuilder
                = authWebTarget.request(MediaType.APPLICATION_JSON);

        Response response
                = invocationBuilder
                .post(Entity.entity(new Credentials(user, password), MediaType.APPLICATION_JSON));

        checkForError(response);

        Authorization authorization = response.readEntity(Authorization.class);
        this.jwt = authorization.getJwt();
    }

    public String getJwt(){
        return jwt;
    }

    public static PortainerConnection connect(String url, String user, String password) throws PortainerException {
        PortainerConnection connection = new PortainerConnection(url, user, password);
        connection.connect();
        return connection;
    }

    public static void checkForError(Response response) throws PortainerException {
        if (response.getStatus() != 200){
            String message = "[" + response.getStatus() + "] " + response.getStatusInfo().getReasonPhrase();

            try{
                ErrorResponse err = response.readEntity(ErrorResponse.class);
                message+="\n"+err.toString();
            }
            catch (Exception e){}

            throw new PortainerException(message);
        }
    }
}
