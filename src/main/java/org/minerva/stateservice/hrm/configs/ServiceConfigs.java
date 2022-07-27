package org.minerva.stateservice.hrm.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:dbs-service.properties")
public class ServiceConfigs {
    @Value("${host}")
    public String host;
    @Value("${auth}")
    public String auth;

    @Value("${server-auth}")
    public String serverAuth;
}
