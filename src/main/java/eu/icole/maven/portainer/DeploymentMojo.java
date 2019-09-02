package eu.icole.maven.portainer;

import eu.icole.portainer.PortainerConnection;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo( name = "deploy")
public class DeploymentMojo extends AbstractMojo {

    @Parameter( property = "portainer.url", defaultValue = "http://localhost:9000/" )
    private String url;

    @Parameter( property = "portainer.user", required = true)
    private String user;

    @Parameter( property = "portainer.password", required = true)
    private String password;

    public void execute() throws MojoExecutionException, MojoFailureException {
    try {
        getLog().info("Connecting to Portainer at " + url);
        PortainerConnection connection = PortainerConnection.connect(url, user, password);
        getLog().info("Connected to Portainer server.");
        getLog().debug("JWT token: "+connection.getJwt());
    }
    catch (Exception e){
        getLog().error(e);
        throw new MojoFailureException(e.getMessage());
    }
    }
}
