package org.minerva.stateservice;

import bitronix.tm.TransactionManagerServices;
import junit.framework.TestCase;
import org.jbpm.runtime.manager.impl.jpa.EntityManagerFactoryManager;
import org.kie.api.KieBase;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.*;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.kie.internal.utils.KieHelper;
import org.minerva.stateservice.models.Person;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.transaction.*;

public class Transaction extends TestCase {

    public void testTransaction() throws SystemException, NotSupportedException, NamingException, HeuristicRollbackException, HeuristicMixedException, RollbackException {
// create the entity manager factory
        EntityManagerFactory emf = EntityManagerFactoryManager.get().getOrCreate("org.jbpm.persistence.jpa");
        TransactionManager tm = TransactionManagerServices.getTransactionManager();

// setup the runtime environment  
        RuntimeEnvironment environment = RuntimeEnvironmentBuilder.Factory.get()
                .newDefaultBuilder()
                .addAsset(ResourceFactory.newClassPathResource("scriptTaskExample.bpmn"), ResourceType.BPMN2)
                .addEnvironmentEntry(EnvironmentName.TRANSACTION_MANAGER, tm)
                .get();

// get the kie session  
        RuntimeManager manager = RuntimeManagerFactory.Factory.get().newPerRequestRuntimeManager(environment);
        RuntimeEngine runtime = manager.getRuntimeEngine(ProcessInstanceIdContext.get());
        KieSession ksession = runtime.getKieSession();

// start the transaction  
        UserTransaction ut = InitialContext.doLookup("java:comp/UserTransaction");
        ut.begin();

// perform multiple commands inside one transaction  
        ksession.insert(new Person("John Doe"));
        ksession.startProcess("MyProcess");

// commit the transaction  
        ut.commit();

    }
}
