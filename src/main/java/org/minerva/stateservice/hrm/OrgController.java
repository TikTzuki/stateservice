package org.minerva.stateservice.hrm;

import org.minerva.stateservice.hrm.models.Org;
import org.minerva.stateservice.hrm.repos.OrgRepos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestControllerAdvice
@RequestMapping
public class OrgController {

    @Autowired
    OrgService orgService;

    @Autowired
    OrgRepos orgRepos;

    @PostMapping(value = "/orgs/sync", consumes = {
            "multipart/form-data"
    })
    public void syncOrgByBpmn(@RequestParam("file") MultipartFile file) throws IOException {
        orgService.syncFromFile(file);

    }

    @GetMapping("/orgs")
    public ResponseEntity<Page<Org>> getOrgs(Pageable pageable) {
        Page<Org> page = orgRepos.findAll(pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/orgs/{id}")
    public void getOrg(@PathVariable String id) {

    }

    @PostMapping("/orgs")
    public void postOrg() {

    }

    @DeleteMapping("/orgs")
    public void deleteOrg() {

    }
}
