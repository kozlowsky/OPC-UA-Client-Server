package com.valcom.opcua.client;

import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;

public class UnknownNodeIdException extends Exception {

    public UnknownNodeIdException(NodeId nodeId) {
        super("NodeId " + nodeId.toString() + " has not been found!");
    }
}
