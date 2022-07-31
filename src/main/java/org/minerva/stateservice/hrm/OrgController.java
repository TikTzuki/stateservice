package org.minerva.stateservice.hrm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.minerva.stateservice.hrm.models.FileUpload;
import org.minerva.stateservice.hrm.models.Org;
import org.minerva.stateservice.hrm.repos.OrgRepos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.json.JsonMergePatch;
import javax.json.JsonValue;
import java.io.IOException;
import java.util.Map;

@RestControllerAdvice
@RequestMapping
public class OrgController {

    @Autowired
    OrgService orgService;

    @Autowired
    OrgRepos orgRepos;

    @Autowired
    ObjectMapper mapper;

    @PostMapping(value = "/orgs/sync", consumes = {
            "multipart/form-data"
    })
    public void syncOrgByBpmn(@RequestParam("file") MultipartFile file) throws IOException {
        orgService.syncFromFile(file.getInputStream());
    }

    @PostMapping(value = "/orgs/export")
    public ResponseEntity<Object> exportOrgTree() throws IOException {
        FileUpload fileUpload = orgService.exportFile();
        return new ResponseEntity<>(fileUpload, HttpStatus.CREATED);
    }

    @GetMapping("/orgs")
    public ResponseEntity<Page<Org>> getOrgs(Pageable pageable) {
        Page<Org> page = orgRepos.findAll(pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/orgs/{id}")
    public ResponseEntity<Org> getOrg(@PathVariable Long id) {
        Org org = orgRepos.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return ResponseEntity.ok(org);
    }

    @PostMapping("/orgs")
    public void postOrg(Long parentId, @RequestBody Map<String, Object> org) {
        orgService.createOrg(parentId, (String) org.get("name"));
    }


    @PatchMapping(value = "/org/{id}", consumes = "application/json-patch+json")
    public ResponseEntity<Object> patchOrg(@PathVariable Long id, @RequestBody JsonMergePatch mergePatchOrg) {
        Org org = orgRepos.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Org orgPatched = mapper.convertValue(mergePatchOrg.apply(mapper.convertValue(org, JsonValue.class)), Org.class);
        orgRepos.save(orgPatched);
        return ResponseEntity.ok(orgPatched);
    }


    @DeleteMapping("/orgs/{id}")
    public void deleteOrg(Long id) {
        orgRepos.deleteById(id);
    }
}
