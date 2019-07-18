package com.valcom.opcua.client;

import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.ExecutionException;

@SpringBootApplication
public class ClientApplication implements CommandLineRunner {

    private final static Logger LOGGER = LoggerFactory.getLogger(ClientApplication.class);

    @Autowired
    private Client client;

    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
    }

    @Override
    public void run(String... args) {
        final String endpoint = String.format("opc.tcp://%s:%s", "localhost", 4845);
        NodeId searchedNodeId = new NodeId(2, "variable");

        try {
            client.connect(endpoint);
            LOGGER.info("Successfully connected to the server {}", endpoint);

            client.browse(searchedNodeId, "");
            LOGGER.info("NodeId has been found {}", searchedNodeId.toString());
        } catch (ExecutionException | InterruptedException e) {
            LOGGER.error("Could not connect to the server {}", endpoint, e);
        } catch (UnknownNodeIdException e) {
            LOGGER.warn("NodeId has not been found {}", searchedNodeId.toString(), e);
        }
    }
}
