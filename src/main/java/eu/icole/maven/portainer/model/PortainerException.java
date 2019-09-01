package eu.icole.maven.portainer.model;

import java.io.IOException;

public class PortainerException extends IOException {

    public PortainerException(String message)
    {
        super(message);
    }
}
