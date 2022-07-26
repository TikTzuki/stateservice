package org.minerva.stateservice.hrm;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.ScriptTask;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.minerva.stateservice.hrm.HrmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/deployment")
public class DeploymentController {

    @Autowired
    HrmService hrmService;

    @PostMapping("/process")
    public void deployNewBpmn(@RequestParam("file") MultipartFile file) throws IOException {
        hrmService.syncFromFile(file);

    }
}
