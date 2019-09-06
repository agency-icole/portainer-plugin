# portainer-plugin

Maven Portainer plugin to support deployment process using Portainer API. This project is not official project of Portainer.io, yet.

# Goals

## deploy

The basic goal of this plugin. It will install given docker-stack.yml file on the chosen Portainer node.

### Properties

#### Requied properties

|Name | Description|
|------|--------------|
|portainer.user| Portainer Username|
|portainer.password| Portainer user password|

### Optional properties

|Name | Default value |Description|
|------|--------------|------------|
|portainer.url| http://localhost:9000| Url poiting to Portainer instance|

# Runing the plugin

You can run the plugin using the command line:

```
> mvn eu.icole:portainer-plugin:0.0.1:deploy -Dportainer.user=admin -Dportainer.password=admin
```

Alternativley you can setup the plugin in your pom.xml

```
<build>
    <plugins>
      <plugin>
        <groupId>eu.icole</groupId>
        <artifactId>portainer-plugin</artifactId>
        <version>0.0.1</version>
        <configuration>
          <user>admin</user>
          <password>admin</password>
        </configuration>        
        <executions>
          <execution>
            <phase>deploy</phase>
            <goals>
              <goal>deploy</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
```

Enjoy
