package org.minerva.stateservice.hrm.models;

import javax.persistence.*;
import java.sql.Blob;

@Entity
@Table(name = "BPMN")
public class BPMN {

    @Id
    @GeneratedValue
    Long id;

    @Lob
    Blob content;

    public BPMN(Blob content) {
        this.content = content;
    }

    public BPMN() {

    }
}
