package org.minerva.stateservice.hrm;

import com.google.gson.JsonArray;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.*;
import org.minerva.stateservice.hrm.configs.ServiceConfigs;
import org.minerva.stateservice.hrm.models.FileUpload;
import org.minerva.stateservice.hrm.models.Org;
import org.minerva.stateservice.hrm.repos.OrgRepos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import retrofit2.Response;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.minerva.stateservice.Utils.print;

@Service
public class OrgService {
    @Autowired
    OrgRepos orgRepos;

    @Autowired
    ServiceConfigs serviceConfigs;

    @Autowired
    FileService fileService;

    public void syncFromFile(MultipartFile file) throws IOException {
        BpmnModelInstance modelInstance = Bpmn.readModelFromStream(file.getInputStream());
        StartEvent start = modelInstance.getModelElementsByType(StartEvent.class).stream().findFirst().get();

        FlowNode current = start.getOutgoing().iterator().next().getTarget();

        if (current instanceof Task) {
            Org org = new Org(null, null, current.getTextContent());
            orgRepos.save(org);
            String path = String.format("%s/", org.getId());
            for (SequenceFlow sequenceFlow : current.getOutgoing())
                travelFlowNode(sequenceFlow.getTarget(), path);
        }
    }


    public FileUpload exportOrgTree() throws IOException {
        Path path = Files.createTempFile("org_", ".bpmn");

        createProcess1(path.toFile());

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

    public void createProcess(File file) {
        BpmnModelInstance model = Bpmn.createEmptyModel();
        Definitions definitions = model.newInstance(Definitions.class);
        definitions.setTargetNamespace("https://camunda.org/examples");
        model.setDefinitions(definitions);

        Process process = model.newInstance(Process.class);
        definitions.addChildElement(process);

        StartEvent startEvent = model.newInstance(StartEvent.class);
        startEvent.setId("start");
        process.addChildElement(startEvent);

        UserTask task = model.newInstance(UserTask.class);
        task.setId("task");
        task.setName("User Task");
        process.addChildElement(task);

        SequenceFlow sequenceFlow = model.newInstance(SequenceFlow.class);
        process.addChildElement(sequenceFlow);
        connect(sequenceFlow, startEvent, task);

        Bpmn.writeModelToFile(file, model);
    }

    public void createProcess1(File file) {
        BpmnModelInstance modelInstance = Bpmn.createProcess()
                .name("simple-process")
                .executable()
                .startEvent()
                .name("start-event")
                .userTask()
                .exclusiveGateway()
                .condition("yes", "${approved}")
                .userTask()
                .endEvent()
                .moveToLastGateway()
                .condition("no", "${!approved}")
                .endEvent().done();
        Bpmn.writeModelToFile(file, modelInstance);
    }

    private void connect(SequenceFlow flow, FlowNode from, FlowNode to) {
        flow.setSource(from);
        from.getOutgoing().add(flow);
        flow.setTarget(to);
        to.getIncoming().add(flow);
    }

    public void createOrg(Long parentId, String name) {
        Org parent = orgRepos.getReferenceById(parentId);
        Org org = new Org(String.format("%s%s/", parent.getAncestry(), parent.getId()), name);
        orgRepos.save(org);
    }


    private void travelFlowNode(FlowNode flowNode, String path) {
        if (flowNode instanceof Task) {
            print(flowNode.getName());
            Org org = new Org(path, flowNode.getTextContent());
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
