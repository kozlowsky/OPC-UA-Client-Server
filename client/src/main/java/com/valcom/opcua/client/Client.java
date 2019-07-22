package com.valcom.opcua.client;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.UaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfig;
import org.eclipse.milo.opcua.stack.client.UaTcpStackClient;
import org.eclipse.milo.opcua.stack.core.types.builtin.*;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.*;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.ExecutionException;


@Component
public class Client {

    private UaClient uaClient;

    public void connect(final String endpoint) throws ExecutionException, InterruptedException {
        uaClient = createClient(endpoint).connect().get();
    }

    private OpcUaClient createClient(final String endpoint) throws ExecutionException, InterruptedException {
        return new OpcUaClient(buildConfiguration(UaTcpStackClient.getEndpoints(endpoint).get()));
    }

    private OpcUaClientConfig buildConfiguration(final EndpointDescription[] endpointDescriptions) {
        return OpcUaClientConfig.builder()
                .setEndpoint(endpointDescriptions[0])
                .build();
    }

    public DataValue[] read(NodeId nodeId) throws ExecutionException, InterruptedException {
        ReadResponse readResponse = uaClient.read(0, TimestampsToReturn.Both, Collections.singletonList(new ReadValueId(nodeId, UInteger.valueOf(13), "0", null))).get();
        return readResponse.getResults();
    }

    public void write(NodeId nodeId, Object object) throws ExecutionException, InterruptedException {
        StatusCode statusCode = uaClient.writeValue(nodeId, new DataValue(new Variant(object))).get();
        System.out.println(statusCode);
    }
}
