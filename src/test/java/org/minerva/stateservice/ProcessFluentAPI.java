package org.minerva.stateservice;

import bitronix.tm.TransactionManagerServices;
import junit.framework.TestCase;
import org.drools.core.SessionConfiguration;
import org.drools.core.audit.WorkingMemoryInMemoryLogger;
import org.drools.core.impl.EnvironmentFactory;
import org.jbpm.bpmn2.xml.XmlBPMNProcessDumper;
import org.jbpm.marshalling.impl.ProcessInstanceResolverStrategy;
import org.jbpm.process.audit.AuditLogService;
import org.jbpm.process.audit.AuditLoggerFactory;
import org.jbpm.process.audit.JPAAuditLogService;
import org.jbpm.process.instance.event.DefaultSignalManagerFactory;
import org.jbpm.process.instance.impl.DefaultProcessInstanceManagerFactory;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.ruleflow.core.RuleFlowProcessFactory;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.Message.Level;
import org.kie.api.io.Resource;
import org.kie.api.marshalling.ObjectMarshallingStrategy;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.internal.KnowledgeBaseFactory;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.persistence.jpa.JPAKnowledgeService;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.kie.internal.runtime.conf.ForceEagerActivationOption;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.kie.api.runtime.EnvironmentName.*;

public class ProcessFluentAPI extends TestCase {
    protected WorkingMemoryInMemoryLogger logger;
    protected AuditLogService logService;
    protected boolean sessionPersistence = false;
    protected boolean pessimisticLocking = false;
    protected static EntityManagerFactory emf;
    EntityManager entityManager;

    public void setUp() {
        emf = Persistence.createEntityManagerFactory("org.jbpm.persistence.jpa");
        entityManager = emf.createEntityManager();
    }

    public void testProcessFactory() throws Exception {
        RuleFlowProcessFactory factory = RuleFlowProcessFactory.createProcess("org.jbpm.process");
        factory
                // header
                .name("My process").packageName("org.jbpm")
                // nodes
                .startNode(1).name("Start").done()
                .actionNode(2).name("Action").action("java", "System.out.println(\"Hello World\");").done()
//                .humanTaskNode(2).name("s1_a1_init").done()
                .endNode(3).name("End").done()
                // connections
                .connection(1,
                        2)
                .connection(2,
                        3);
        RuleFlowProcess process = factory.validate().getProcess();
        Resource res = ResourceFactory.newByteArrayResource(XmlBPMNProcessDumper.INSTANCE.dump(process).getBytes());
        res.setSourcePath("~/processFactory.bpmn2");
//        EntityTransaction transaction = entityManager.getTransaction();
//        transaction.begin();
//
//        entityManager.persist(new BPMN(
//                new SerialBlob(XmlBPMNProcessDumper.INSTANCE.dump(process).getBytes())
//        ));
//        transaction.commit();

        KieBase kbase = createKnowledgeBaseFromResources(res);
        StatefulKnowledgeSession ksession = createKnowledgeSession(kbase);
        ksession.startProcess("org.jbpm.process");
        ksession.dispose();
    }

    protected KieBase createKnowledgeBaseFromResources(Resource... process)
            throws Exception {

        KieServices ks = KieServices.Factory.get();
        KieRepository kr = ks.getRepository();
        if (process.length > 0) {
            KieFileSystem kfs = ks.newKieFileSystem();

            for (Resource p : process) {
                kfs.write(p);
            }

            KieBuilder kb = ks.newKieBuilder(kfs);

            kb.buildAll(); // kieModule is automatically deployed to KieRepository
            // if successfully built.

            if (kb.getResults().hasMessages(Level.ERROR)) {
                throw new RuntimeException("Build Errors:\n"
                        + kb.getResults().toString());
            }
        }

        KieContainer kContainer = ks.newKieContainer(kr.getDefaultReleaseId());
        return kContainer.getKieBase();
    }

    protected StatefulKnowledgeSession createKnowledgeSession(KieBase kbase)
            throws Exception {
        return createKnowledgeSession(kbase, null, null);
    }

    protected Environment createEnvironment(EntityManagerFactory emf) {
        Environment env = EnvironmentFactory.newEnvironment();
        env.set(ENTITY_MANAGER_FACTORY, emf);
        env.set(TRANSACTION_MANAGER,
                TransactionManagerServices.getTransactionManager()
        );
        if (sessionPersistence) {
            ObjectMarshallingStrategy[] strategies = (ObjectMarshallingStrategy[]) env.get(OBJECT_MARSHALLING_STRATEGIES);

            List<ObjectMarshallingStrategy> listStrategies = new ArrayList<ObjectMarshallingStrategy>(Arrays.asList(strategies));
            listStrategies.add(0, new ProcessInstanceResolverStrategy());
            strategies = new ObjectMarshallingStrategy[listStrategies.size()];
            env.set(OBJECT_MARSHALLING_STRATEGIES, listStrategies.toArray(strategies));
        }
        return env;
    }

    protected StatefulKnowledgeSession createKnowledgeSession(KieBase kbase,
                                                              KieSessionConfiguration conf, Environment env) throws Exception {
        StatefulKnowledgeSession result;
        if (conf == null) {
            conf = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
        }

        // Do NOT use the Pseudo clock yet..
        // conf.setOption( ClockTypeOption.get( ClockType.PSEUDO_CLOCK.getId() )
        // );

        if (sessionPersistence) {
            if (env == null) {
                env = createEnvironment(emf);
            }
            if (pessimisticLocking) {
                env.set(USE_PESSIMISTIC_LOCKING, true);
            }
            conf.setOption(ForceEagerActivationOption.YES);
            result = JPAKnowledgeService.newStatefulKnowledgeSession(kbase,
                    conf, env);
            AuditLoggerFactory.newInstance(AuditLoggerFactory.Type.JPA, result, null);
            logService = new JPAAuditLogService(env);
        } else {
            if (env == null) {
                env = EnvironmentFactory.newEnvironment();
            }

            Properties defaultProps = new Properties();
            defaultProps.setProperty("drools.processSignalManagerFactory",
                    DefaultSignalManagerFactory.class.getName());
            defaultProps.setProperty("drools.processInstanceManagerFactory",
                    DefaultProcessInstanceManagerFactory.class.getName());
            conf = SessionConfiguration.newInstance(defaultProps);
            conf.setOption(ForceEagerActivationOption.YES);
            result = (StatefulKnowledgeSession) kbase.newKieSession(conf, env);
            logger = new WorkingMemoryInMemoryLogger(result);
        }
        return result;
    }
}
