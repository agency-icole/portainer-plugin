package eu.icole.maven.portainer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.swarm.Swarm;
import eu.icole.portainer.PortainerConnection;
import eu.icole.portainer.model.PortainerEndpoint;
import eu.icole.portainer.model.PortainerException;
import eu.icole.portainer.model.rest.StackDeployment;
import eu.icole.portainer.model.rest.StackDeploymentBody;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import javax.swing.tree.TreeNode;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;

@Mojo(name = "deploy")
public class DeploymentMojo extends AbstractMojo {

    @Parameter(property = "url", defaultValue = "http://localhost:9000/")
    private String url;

    @Parameter(property = ".ser", required = true)
    private String user;

    @Parameter(property = "password", required = true)
    private String password;

    @Parameter(property = "endpoint", defaultValue = "local")
    private String endpoint;

    @Parameter(property = "repository")
    private String repositoryURL;

    @Parameter(property = "repositoryReferenceName")
    private String repositoryReferenceName;

    @Parameter(property = "repositoryComposeFile")
    private String composeFilePathInRepository;

    @Parameter(property = "repositoryUsername")
    private String repositoryUsername;
    @Parameter(property = "repositoryPassword")
    private String repositoryPassword;

    @Parameter(property = "type", defaultValue = "swarm")
    private String type;

    @Parameter(property = "stackName", required = true)
    private String stackName;

    @Parameter(property = "file")
    private String filename;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            getLog().info("Connecting to Portainer at " + url + password);
            PortainerConnection connection = PortainerConnection.connect(url, user, password);
            getLog().info("Connected to Portainer server.");
            getLog().debug("JWT token: " + connection.getJwt());
            deploy(connection);
        } catch (Exception e) {
            getLog().error(e);
            throw new MojoFailureException(e.getMessage());
        }
    }

    void deploy(PortainerConnection connection) throws IOException, URISyntaxException, DockerException, InterruptedException {

        System.out.println(repositoryUsername + ":" + repositoryPassword);
        getLog().info("Deploying to " + endpoint);
        PortainerEndpoint deployEndpoint = connection.getEndpoint(endpoint);
        if (deployEndpoint == null) {
            getLog().error("Unable to find endpopint: " + endpoint);
            throw new PortainerException("Unable to find endpoint " + endpoint);
        }
        getLog().info("Obtained endpoint " + endpoint);

        StackDeploymentBody stackBody = new StackDeploymentBody();

        int deploymentType = StackDeployment.STACK_TYPE_SWARM;

        if ("compose".equals(type.toLowerCase()))
            deploymentType = StackDeployment.STHACK_TYPE_COMPOSE;
        else if (!"swarm".equals(type.toLowerCase()))
            throw new PortainerException("Unknown deployment type: " + type);

        getLog().info("Stack name: " + stackName);
        getLog().info("Deployment type: " + type.toLowerCase());
        if (deploymentType == StackDeployment.STACK_TYPE_SWARM) {
            getLog().info("Obtaining Swarm Id");
            Swarm swarm = deployEndpoint.getDockerClient().inspectSwarm();
            if (swarm == null) {
                String message = "Unable to obtain Swarm ID. Is swarm enabled?";
                throw new PortainerException(message);
            }
            getLog().info("Swarm id: " + swarm.id());
            stackBody.setSwarmID(swarm.id());
        }


        String method = StackDeployment.STACK_METHOD_STRING;
        if (repositoryURL != null && filename == null)
            method = StackDeployment.STACK_METHOD_REPOSITORY;

        getLog().info("Deployment method: " + method);

        if (method.equals(StackDeployment.STACK_METHOD_REPOSITORY)) {
            getLog().info("Deploying from repository: " + repositoryURL);
            stackBody.setRepositoryURL(repositoryURL);

            if (repositoryUsername != null) {
                getLog().info("Authentication enabled");
                stackBody.setRepositoryAuthentication(true);
                stackBody.setRepositoryUsername(repositoryUsername);
                stackBody.setRepositoryPassword(repositoryPassword);
            }

            if (repositoryReferenceName != null) {
                getLog().info("Remote branch is " + repositoryReferenceName);
                stackBody.setRepositoryReferenceName(repositoryReferenceName);
            }
            getLog().info("Deploying stack from file " + composeFilePathInRepository);

        } else {
            if (filename == null)
                throw new FileNotFoundException("Please specify filename!");
            File file = new File(filename);
            if (!file.exists())
                throw new FileNotFoundException("Docer compose file not found: " + filename);

            YAMLFactory yf = new YAMLFactory();

            ObjectMapper mapper = new ObjectMapper(yf);
            ObjectNode root = (ObjectNode) mapper.readTree(file);

            StringWriter sw = new StringWriter();
            yf.createGenerator(sw).writeObject(root);
            String out = sw.toString();
            System.out.println(out);
            stackBody.setStackFileContent(out);
        }

        stackBody.setName(stackName);

        StackDeployment deployment = new StackDeployment(deploymentType, method, deployEndpoint.getId(), stackBody);
        getLog().info("Deploying stack " + stackName + "...");
        connection.createStack(deployment, true);
        getLog().info("Stack deployed to " + endpoint);
    }

}
