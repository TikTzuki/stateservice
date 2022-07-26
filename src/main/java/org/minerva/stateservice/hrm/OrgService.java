package org.minerva.stateservice.hrm;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.*;
import org.minerva.stateservice.hrm.models.Org;
import org.minerva.stateservice.hrm.repos.OrgRepos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.minerva.stateservice.Utils.print;

@Service
public class OrgService {
    @Autowired
    OrgRepos orgRepos;

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

    private void travelFlowNode(FlowNode flowNode, String path) {
        if (flowNode instanceof Task) {
            print(flowNode.getName());
            Org org = new Org(null, path, flowNode.getTextContent());
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
