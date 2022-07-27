package org.minerva.stateservice.hrm;

import okhttp3.*;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.*;
import org.minerva.stateservice.hrm.configs.ServiceConfigs;
import org.minerva.stateservice.hrm.models.Org;
import org.minerva.stateservice.hrm.repos.OrgRepos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.minerva.stateservice.Utils.print;

@Service
public class OrgService {
    @Autowired
    OrgRepos orgRepos;
    @Autowired
    ServiceConfigs serviceConfigs;

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


    public Resource exportOrgTree() throws IOException {
        Path path = Files.createTempFile("file", ".bpmn");
        Files.write(path, "Hello".getBytes());
        MultipartBody.Part filePart = MultipartBody.Part.createFormData(
                "file",
                path.getFileName().toString(),
                RequestBody.create(MediaType.parse("/**"),path.toFile())
        );
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(serviceConfigs.host)
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient())
                .build();
        FileRepos fileRepos = retrofit.create(FileRepos.class);
        fileRepos.upload(filePart, serviceConfigs.serverAuth, serviceConfigs.auth).enqueue(
                new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            print(response.body().string());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                        print(throwable);
                    }
                }
        );
        return null;
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
