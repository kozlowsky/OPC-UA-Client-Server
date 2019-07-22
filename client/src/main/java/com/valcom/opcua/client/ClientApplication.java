package com.valcom.opcua.client;

import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
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
        NodeId searchedNodeId = new NodeId(2, "id");

        try {
            client.connect(endpoint);
            LOGGER.info("Successfully connected to the server {}", endpoint);
        } catch (ExecutionException | InterruptedException e) {
            LOGGER.error("Could not connect to the server {}", endpoint, e);
        }

        try {
            DataValue[] values = client.read(searchedNodeId);
            LOGGER.info("Successfully read values of a node {} \n {}", searchedNodeId, values[0]);
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Could not read value of a node {}", searchedNodeId);
        }

        try {
            client.write(searchedNodeId, new int[]
                    {
                            1,2,3,4,5,6,7,8,9,10
                    });
            LOGGER.info("StatusCode ");
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Could not write new value to the node {}", searchedNodeId);
        }

        try {
            client.read(searchedNodeId);
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Could not read updated value of a node {}", searchedNodeId);
        }
    }
}
