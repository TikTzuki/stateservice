//package org.minerva.stateservice;
//
//import org.jbpm.bpmn2.xml.XmlBPMNProcessDumper;
//import org.jbpm.ruleflow.core.RuleFlowProcess;
//import org.jbpm.ruleflow.core.RuleFlowProcessFactory;
//import org.kie.api.KieServices;
//import org.kie.api.builder.KieFileSystem;
//import org.kie.api.builder.ReleaseId;
//import org.kie.api.fluent.Dialect;
//import org.kie.api.io.Resource;
//
//import java.nio.charset.StandardCharsets;
//
//public class Test {
//    public static void main(String[] args) {
//        RuleFlowProcessFactory factory = RuleFlowProcessFactory.createProcess("org.minerva.FluentProcess");
//        factory.name("FluentProcess")
//                .version("1.0")
//                .packageName("org.minerva")
//                .startNode(1).name("Start").done()
//                .actionNode(2).name("Action")
//                .action(Dialect.JAVA, "System.out.print(\"Hello\");").done()
//                .endNode(3).name("End").done()
//                .connection(1, 2)
//                .connection(2, 3);
//
//        RuleFlowProcess process = factory.validate().getProcess();
//        KieServices ks = KieServices.Factory.get();
//        KieFileSystem kfs = ks.newKieFileSystem();
//        Resource resource = ks.getResources().newByteArrayResource(
//                XmlBPMNProcessDumper.INSTANCE.dump(process).getBytes(StandardCharsets.UTF_8)
//        );
//        resource.setSourcePath("fluent.bpmn2");
//        kfs.write(resource);
//        ReleaseId releaseId = ks.newReleaseId("org.minerva", "fluent", "1.0");
//        kfs.generateAndWritePomXML(releaseId);
//        ks.newKieBuilder(kfs).buildAll();
//        ks.newKieContainer(releaseId).newKieSession().startProcess("org.minerva.FluentProcess");
//    }
//}
