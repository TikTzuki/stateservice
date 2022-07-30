package org.minerva.stateservice.hrm;

import com.google.gson.JsonArray;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.ParallelGatewayBuilder;
import org.camunda.bpm.model.bpmn.instance.*;
import org.minerva.stateservice.hrm.configs.ServiceConfigs;
import org.minerva.stateservice.hrm.models.FileUpload;
import org.minerva.stateservice.hrm.models.Org;
import org.minerva.stateservice.hrm.repos.OrgRepos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import retrofit2.Response;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.minerva.stateservice.Utils.print;

@Service
public class OrgService {
    @Autowired
    OrgRepos orgRepos;

    @Autowired
    ServiceConfigs serviceConfigs;

    @Autowired
    FileService fileService;

    @PostConstruct
    private void initOrgTree() {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("org_tree.bpmn");
        syncFromFile(inputStream);
    }

    public void syncFromFile(InputStream inputStream) {
        BpmnModelInstance modelInstance = Bpmn.readModelFromStream(inputStream);
        StartEvent start = modelInstance.getModelElementsByType(StartEvent.class).stream().findFirst().get();

        FlowNode current = start.getOutgoing().iterator().next().getTarget();

        if (current instanceof Task) {
            Optional<Documentation> documentationOptional = current.getDocumentations().stream().findAny();
            Org org = new Org(null, null, documentationOptional.isPresent() ? documentationOptional.get().getTextContent() : "{}");
            orgRepos.save(org);
            String path = String.format("%s/", org.getId());
            for (SequenceFlow sequenceFlow : current.getOutgoing())
                travelFlowNode(sequenceFlow.getTarget(), path);
        }

    }

    public FileUpload exportFile() throws IOException {
        Path path = Files.createTempFile("org_", ".bpmn");


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

        Optional<Org> rootOpt = orgRepos.findByAncestry(null).stream().findAny();
        if(rootOpt.isEmpty())
            return null;

        Org root = rootOpt.get();
        String rootAncestry = String.format("%s/", root.getId());
        ParallelGatewayBuilder builder= Bpmn.createProcess("org_tree")
                .startEvent().name("start")
                .manualTask().documentation(root.getData())
                .parallelGateway().name(rootAncestry);
        for(Org org: orgRepos.findByAncestry(rootAncestry)){
            builder.manualTask().documentation(org.getData()).moveToLastGateway();
        }

        return file;
    }

    public void createOrg(Long parentId, String name) {
        Org parent = orgRepos.getReferenceById(parentId);
        Org org = new Org(String.format("%s%s/", parent.getAncestry(), parent.getId()), name);
        orgRepos.save(org);
    }

    private void travelFlowNode(FlowNode flowNode, String path) {
        if (flowNode instanceof Task) {
            Optional<Documentation> documentationOptional = flowNode.getDocumentations().stream().findAny();
            Org org = new Org(null, path, documentationOptional.isPresent() ? documentationOptional.get().getTextContent() : "{}");
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
