package org.wildfly.extension.microprofile.health;

import java.util.Arrays;
import java.util.Collection;

import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.PersistentResourceDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

/**
 * @author <a href="mailto:tcerar@redhat.com">Tomaz Cerar</a>
 */
public class SubsystemDefinition extends PersistentResourceDefinition {
    static AttributeDefinition HTTP_SERVER = SimpleAttributeDefinitionBuilder.create("http-server", ModelType.STRING)
            .setAttributeGroup("http")
            .setXmlName("server")
            .setDefaultValue(new ModelNode("default-server"))
            .setRequired(false)
            .setRestartAllServices()
            .build();
    static AttributeDefinition HTTP_VIRTUAL_HOST = SimpleAttributeDefinitionBuilder.create("http-virtual-host", ModelType.STRING)
            .setAttributeGroup("http")
            .setXmlName("virtual-host")
            .setDefaultValue(new ModelNode("default-host"))
            .setRequired(false)
            .setRestartAllServices()
            .build();
    static AttributeDefinition HTTP_PATH = SimpleAttributeDefinitionBuilder.create("http-path", ModelType.STRING)
            .setAttributeGroup("http")
            .setXmlName("path")
            .setRequired(false)
            .setRestartAllServices()
            .build();

    static AttributeDefinition[] ATTRIBUTES = { HTTP_SERVER, HTTP_VIRTUAL_HOST, HTTP_PATH};

    protected SubsystemDefinition() {
        super(SubsystemExtension.SUBSYSTEM_PATH,
                SubsystemExtension.getResourceDescriptionResolver(SubsystemExtension.SUBSYSTEM_NAME),
                //We always need to add an 'add' operation
                SubsystemAdd.INSTANCE,
                //Every resource that is added, normally needs a remove operation
                SubsystemRemove.INSTANCE);
    }

    @Override
    public Collection<AttributeDefinition> getAttributes() {
        return Arrays.asList(ATTRIBUTES);
    }

    @Override
    public void registerOperations(ManagementResourceRegistration resourceRegistration) {
        super.registerOperations(resourceRegistration);

        resourceRegistration.registerOperationHandler(CheckOperation.DEFINITION, new CheckOperation());
    }
}
