package org.minerva.stateservice.hrm.org;

import com.google.gson.JsonArray;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.AbstractGatewayBuilder;
import org.camunda.bpm.model.bpmn.instance.*;
import org.minerva.stateservice.hrm.adapters.FileService;
import org.minerva.stateservice.hrm.configs.ServiceConfigs;
import org.minerva.stateservice.hrm.models.*;
import org.minerva.stateservice.hrm.repos.OrgRepos;
import org.minerva.stateservice.hrm.repos.RoleRepos;
import org.minerva.stateservice.hrm.repos.UserRepos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import retrofit2.Response;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class OrgService {
    @Autowired
    FileService fileService;
    @Autowired
    OrgRepos orgRepos;

    @Autowired
    UserRepos userRepos;

    @Autowired
    ServiceConfigs serviceConfigs;

    @Autowired
    RoleRepos roleRepos;

    @PostConstruct
    private void initOrgTree() {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("org_tree.bpmn");
        syncFromFile(inputStream);
        Role role = new Role("employee", "Nhân viên");
        roleRepos.save(role);

        User u = new User("hybrid");

        Set<Allocate> allocates = new HashSet<>() {{
            add(new Allocate(null, u, orgRepos.getReferenceById("001"), role, null));
        }};
        u.setAllocates(allocates);
        userRepos.save(u);

    }

    public void syncFromFile(InputStream inputStream) {
        BpmnModelInstance modelInstance = Bpmn.readModelFromStream(inputStream);
        StartEvent start = modelInstance.getModelElementsByType(StartEvent.class).stream().findFirst().get();

        FlowNode current = start.getOutgoing().iterator().next().getTarget();

        if (current instanceof Task) {
            Optional<Documentation> documentationOptional = current.getDocumentations().stream().findAny();

            Org org = Org.fromTask(current.getId(), null, documentationOptional.isPresent() ? documentationOptional.get().getTextContent() : "{}");
            orgRepos.save(org);
            String path = String.format("%s/", org.getId());
            for (SequenceFlow sequenceFlow : current.getOutgoing())
                travelFlowNode(sequenceFlow.getTarget(), path);
        }

    }

    public FileUpload exportFile() throws IOException {
        Path path = Files.createTempFile("org_", ".bpmn");

        Bpmn.writeModelToFile(path.toFile(), createBpmnModel());

        MultipartBody.Part filePart = MultipartBody.Part.createFormData(
                "file",
                path.getFileName().toString(),
                RequestBody.create(path.toFile(), MediaType.parse("/**"))
        );

        Response<FileUpload> resp = fileService.upload(filePart).execute();
        if (!resp.isSuccessful() || resp.body() == null)
            throw new RuntimeException(resp.errorBody().string());

        FileUpload file = resp.body();
        Response<JsonArray> urlObject = fileService.download(List.of(file.getUuid())).execute();
        if (!urlObject.isSuccessful() || urlObject.body() == null)
            throw new RuntimeException(urlObject.errorBody().string());

        file.setFileUrl(urlObject.body().iterator().next().getAsJsonObject().get("file_url").getAsString());

        return file;
    }

    private BpmnModelInstance createBpmnModel() {
        Optional<Org> rootOpt = orgRepos.findByAncestry(null).stream().findAny();
        if (rootOpt.isEmpty())
            return null;

        Org root = rootOpt.get();
        String rootAncestry = String.format("%s/", root.getId());
        AbstractGatewayBuilder<?, ?> builder = Bpmn.createProcess("org_tree")
                .startEvent().name("start")
                .manualTask(root.getTaskId()).name(root.getTaskId())
                .parallelGateway(root.getNextGatewayId()).name(root.getNextGatewayId());
        for (Org org : orgRepos.findByAncestry(rootAncestry)) {
            builder = travelOrgTree(org, builder);
        }
        return builder.done();
    }

    private AbstractGatewayBuilder<?, ?> travelOrgTree(Org org, AbstractGatewayBuilder builder) {
        String ancestry = String.format("%s%s/", org.getAncestry(), org.getId());
        builder.moveToNode(org.getPrevGatewayId())
                .manualTask(org.getTaskId()).name(org.getTaskId()).documentation(org.getData());
        List<Org> children = orgRepos.findByAncestry(ancestry);
        if (children.size() > 0)
            builder = builder.moveToNode(org.getTaskId())
                    .parallelGateway(org.getNextGatewayId()).name(org.getNextGatewayId());
        for (Org child : children) {
            builder = travelOrgTree(child, builder);
        }
        return builder;
    }

    public void createOrg(String parentId, String id, String name) {
        Org parent = orgRepos.getReferenceById(parentId);
        Org org = new Org(id, String.format("%s%s/", parent.getAncestry(), parent.getId()), name);
        orgRepos.save(org);
    }

    private void travelFlowNode(FlowNode flowNode, String path) {
        if (flowNode instanceof Task) {
            Optional<Documentation> documentationOptional = flowNode.getDocumentations().stream().findAny();
            Org org = Org.fromTask(flowNode.getId(), path, documentationOptional.isPresent() ? documentationOptional.get().getTextContent() : "{}");
            orgRepos.save(org);
            for (SequenceFlow sequenceFlow : flowNode.getOutgoing()) {
                travelFlowNode(sequenceFlow.getTarget(), String.format("%s%s/", path, org.getId()));
            }
        }
        if (flowNode instanceof ParallelGateway) {
            for (SequenceFlow sequenceFlow : flowNode.getOutgoing()) {
                travelFlowNode(sequenceFlow.getTarget(), path);
            }
        }
    }

}
