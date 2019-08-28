package com.valcom.opcua.server.server;

import org.eclipse.milo.opcua.sdk.core.Reference;
import org.eclipse.milo.opcua.sdk.core.ValueRanks;
import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.AccessContext;
import org.eclipse.milo.opcua.sdk.server.api.DataItem;
import org.eclipse.milo.opcua.sdk.server.api.MonitoredItem;
import org.eclipse.milo.opcua.sdk.server.api.ServerNodeMap;
import org.eclipse.milo.opcua.sdk.server.model.nodes.objects.FolderNode;
import org.eclipse.milo.opcua.sdk.server.nodes.AttributeContext;
import org.eclipse.milo.opcua.sdk.server.nodes.ServerNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode;
import org.eclipse.milo.opcua.sdk.server.util.SubscriptionModel;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.*;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UShort;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId;
import org.eclipse.milo.opcua.stack.core.types.structured.WriteValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class Namespace implements org.eclipse.milo.opcua.sdk.server.api.Namespace {

    private final static Logger LOGGER = LoggerFactory.getLogger(Namespace.class);

    public static final String URI = "urn:valcom:namespace";

    private final SubscriptionModel subscriptionModel;

    private final ServerNodeMap serverNodeMap;

    private final UShort index;

    public Namespace(final OpcUaServer opcUaServer, final UShort index) {
        this.index = index;
        this.serverNodeMap = opcUaServer.getNodeMap();
        this.subscriptionModel = new SubscriptionModel(opcUaServer, this);
        addTestNodes();
    }

    @Override
    public UShort getNamespaceIndex() {
        return index;
    }

    @Override
    public String getNamespaceUri() {
        return URI;
    }

    @Override
    public void read(ReadContext context, Double maxAge, TimestampsToReturn timestamps, List<ReadValueId> readValueIds) {
        final List<DataValue> results = new ArrayList<>(readValueIds.size());

        for (final ReadValueId id : readValueIds) {
            final ServerNode node = this.serverNodeMap.get(id.getNodeId());

            final DataValue value = node != null ? node.readAttribute(new AttributeContext(context), id.getAttributeId()) : new DataValue(StatusCodes.Bad_NodeIdUnknown);

            results.add(value);
        }

        context.complete(results);
    }

    @Override
    public void write(WriteContext context, List<WriteValue> writeValues) {
        final List<StatusCode> results = writeValues.stream()
                .map(value -> {
                    Optional<ServerNode> serverNode = serverNodeMap.getNode(value.getNodeId());
                    System.out.println(value.getAttributeId());
                    if (serverNode.isPresent()) {
                        AttributeContext attributeContext = new AttributeContext(context);
                        LOGGER.info("Current Value : {}", serverNode.get().getAttribute(attributeContext, AttributeId.from(value.getAttributeId()).get()));
                        LOGGER.info("Value from client : {}", value.getValue());

                        try {
                            serverNode.get().setAttribute(attributeContext, AttributeId.from(value.getAttributeId()).get(), value.getValue());
                        } catch (UaException e) {
                            LOGGER.error("Could not write value");
                            return new StatusCode(StatusCodes.Bad_NotWritable);
                        }
                        return StatusCode.GOOD;
                    } else {
                        return new StatusCode(StatusCodes.Bad_NodeIdUnknown);
                    }
                })
                .collect(Collectors.toList());

        context.complete(results);
    }

    @Override
    public void onDataItemsCreated(List<DataItem> dataItems) {
        this.subscriptionModel.onDataItemsCreated(dataItems);
    }

    @Override
    public void onDataItemsModified(List<DataItem> dataItems) {
        this.subscriptionModel.onDataItemsModified(dataItems);
    }

    @Override
    public void onDataItemsDeleted(List<DataItem> dataItems) {
        this.subscriptionModel.onDataItemsDeleted(dataItems);
    }

    @Override
    public void onMonitoringModeChanged(List<MonitoredItem> monitoredItems) {
        this.subscriptionModel.onMonitoringModeChanged(monitoredItems);
    }

    @Override
    public CompletableFuture<List<Reference>> browse(AccessContext context, NodeId nodeId) {
        final Optional<ServerNode> serverNode = serverNodeMap.getNode(nodeId);

        if (serverNode.isPresent()) {
            return CompletableFuture.completedFuture(serverNode.get().getReferences());
        } else {
            final CompletableFuture<List<Reference>> f = new CompletableFuture<>();
            f.completeExceptionally(new UaException(StatusCodes.Bad_NodeIdUnknown));
            return f;
        }
    }

    @Deprecated
    private void addTestNodes() {
        String text = "identifier";
        UaVariableNode uaVariableNode = UaVariableNode.builder(this.serverNodeMap)
                .setNodeId(new NodeId(this.getNamespaceIndex(), text))
                .setDataType(Identifiers.Float)
                .setValueRank(ValueRanks.OneDimension)
                .setArrayDimensions(new UInteger[]{UInteger.valueOf(2)})
                .setBrowseName(new QualifiedName(this.getNamespaceIndex(), "123"))
                .setDisplayName(LocalizedText.english("my-custom-variable"))
                .build();

        serverNodeMap.getNode(Identifiers.ObjectsFolder).ifPresent(folder -> ((FolderNode) folder).addComponent(uaVariableNode));
    }
}
