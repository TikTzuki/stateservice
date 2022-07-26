package org.minerva.stateservice;


import junit.framework.TestCase;
import org.jbpm.runtime.manager.impl.jpa.EntityManagerFactoryManager;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.*;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.manager.context.EmptyContext;

import javax.persistence.EntityManagerFactory;

public class SimpleFlow extends TestCase {
    public void testSimpleFlow() {
        EntityManagerFactory emf = EntityManagerFactoryManager.get().getOrCreate("org.jbpm.persistence.jpa");
        RuntimeEnvironment env = RuntimeEnvironmentBuilder.Factory.get()
                .newDefaultInMemoryBuilder()
                .addAsset(ResourceFactory.newClassPathResource("scriptTaskExample.bpmn"), ResourceType.BPMN2)
                .addEnvironmentEntry(EnvironmentName.ENTITY_MANAGER_FACTORY, emf)
                .get();

        RuntimeManager manager = RuntimeManagerFactory.Factory.get().newSingletonRuntimeManager(env);

        RuntimeEngine engine = manager.getRuntimeEngine(EmptyContext.get());

        KieSession session = engine.getKieSession();

        session.startProcess("LOS.scriptTaskExample");

        manager.disposeRuntimeEngine(engine);

    }
}
