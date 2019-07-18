package com.valcom.opcua.server.server;

import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig;
import org.eclipse.milo.opcua.sdk.server.identity.AnonymousIdentityValidator;
import org.eclipse.milo.opcua.sdk.server.identity.CompositeValidator;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.application.CertificateValidator;
import org.eclipse.milo.opcua.stack.core.application.DefaultCertificateManager;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.springframework.stereotype.Component;

import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

@Component
public class Server {

    public static OpcUaServer server;

    private Server() {
        if (server != null) {
            throw new IllegalStateException("Server already exists!");
        }
    }

    public static void startup() {
        if(server == null) {
            OpcUaServerConfig config = OpcUaServerConfig.builder()
                    .setApplicationName(LocalizedText.english("Valcom - OPC UA Server"))
                    .setEndpointAddresses(Collections.singletonList("localhost"))
                    .setBindPort(4845)
                    .setIdentityValidator(new CompositeValidator(AnonymousIdentityValidator.INSTANCE))
                    .setApplicationUri("urn:valcom")
                    .setUserTokenPolicies(Collections.singletonList(OpcUaServerConfig.USER_TOKEN_POLICY_ANONYMOUS))
                    .setSecurityPolicies(EnumSet.of(SecurityPolicy.None))
                    .setCertificateManager(new DefaultCertificateManager())
                    .setCertificateValidator(createCertificateValidator())
                    .build();

            server = new OpcUaServer(config);
            server.getNamespaceManager().registerAndAdd(Namespace.URI, index -> new Namespace(server, index));

            server.startup();
        } else throw new IllegalStateException("Server already exists!");
    }

    private static CertificateValidator createCertificateValidator() {
        return new CertificateValidator() {
            @Override
            public void validate(X509Certificate certificate) throws UaException {

            }

            @Override
            public void verifyTrustChain(List<X509Certificate> certificateChain) throws UaException {

            }
        };
    }
}
