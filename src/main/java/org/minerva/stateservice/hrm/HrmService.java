package org.minerva.stateservice.hrm;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.camunda.bpm.model.bpmn.instance.Task;
import org.minerva.stateservice.hrm.repos.OrgRepos;
import org.minerva.stateservice.hrm.models.Org;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.minerva.stateservice.Utils.print;

@Service
public class HrmService {
    @Autowired
    OrgRepos orgRepos;

    public void syncFromFile(MultipartFile file) throws IOException {
        BpmnModelInstance modelInstance = Bpmn.readModelFromStream(file.getInputStream());
        StartEvent start = modelInstance.getModelElementsByType(StartEvent.class).stream().findFirst().get();

        FlowNode current = start.getOutgoing().iterator().next().getTarget();

        if (current instanceof Task) {
            Org org = new Org(null, null, current.getTextContent());
            orgRepos.save(org);
            print(org.getId());
            travelFlowNode(current);
        }

    }

    private void travelFlowNode(FlowNode flowNode) {
        if (flowNode instanceof Task) {
            print(flowNode.getName());
        }
        for (SequenceFlow sequenceFlow : flowNode.getOutgoing()) {
            travelFlowNode(sequenceFlow.getTarget());
        }
    }

}
