package org.minerva.stateservice.hrm.models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "person")
public class Person {

    @Id
    String username;

    public Person() {
    }

    public Person(String username) {
        this.username = username;
    }

}
