package org.minerva.stateservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;

import javax.json.JsonMergePatch;
import javax.json.JsonValue;
import javax.persistence.Access;

public interface Utils {
    static void print(Object o) {
        System.out.println(o);
    }

}
