package com.valcom.opcua.client;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.UaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfig;
import org.eclipse.milo.opcua.stack.client.UaTcpStackClient;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.BrowseDirection;
import org.eclipse.milo.opcua.stack.core.types.enumerated.BrowseResultMask;
import org.eclipse.milo.opcua.stack.core.types.enumerated.NodeClass;
import org.eclipse.milo.opcua.stack.core.types.structured.BrowseDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.BrowseResult;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.ReferenceDescription;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

@Component
public class Client {

    private static String SPACER = " ";

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

    public void browse(final NodeId browseRoot) throws ExecutionException, InterruptedException, UnknownNodeIdException {
        if(this.uaClient == null) throw new IllegalStateException("Client is not connected!");

        final BrowseDescription browse = new BrowseDescription(browseRoot,
                BrowseDirection.Forward,
                Identifiers.References,
                true,
                uint(NodeClass.Object.getValue() | NodeClass.Variable.getValue()),
                uint(BrowseResultMask.All.getValue()));

        BrowseResult browseResult = uaClient.browse(browse).get();

        if(!browseResult.getStatusCode().equals(StatusCode.GOOD)) {
            throw new UnknownNodeIdException(browseRoot);
        }
        while (browseResult != null) {
            for (final ReferenceDescription ref : browseResult.getReferences()) {
                dumpRef(SPACER, ref);
                SPACER += " ";
                final Optional<NodeId> childId = ref.getNodeId().local();
                if (childId.isPresent()) {
                    browse(childId.get());
                }
            }

            if (browseResult.getContinuationPoint().isNotNull()) {
                browseResult = uaClient.browseNext(true, browseResult.getContinuationPoint()).get();
            } else browseResult = null;
        }


    }

    private void dumpRef(final String ident, final ReferenceDescription referenceDescription) {
        System.out.format("%-60s %-15s %s%n",
                ident + referenceDescription.getBrowseName().getName(),
                referenceDescription.getNodeClass().toString(),
                referenceDescription.getNodeId().local().map(NodeId::toParseableString).orElse(""));
    }
}
