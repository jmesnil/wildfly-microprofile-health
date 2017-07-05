package org.wildfly.extension.microprofile.health;

import java.util.Arrays;
import java.util.Collection;

import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.PersistentResourceDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelType;

/**
 * @author <a href="mailto:tcerar@redhat.com">Tomaz Cerar</a>
 */
public class SubsystemDefinition extends PersistentResourceDefinition {
    static AttributeDefinition HTTP_ENDPOINT = SimpleAttributeDefinitionBuilder.create("http-endpoint", ModelType.STRING)
            .setAllowNull(true)
            .setRestartAllServices()
            .build();

    static AttributeDefinition[] ATTRIBUTES = { HTTP_ENDPOINT };

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
