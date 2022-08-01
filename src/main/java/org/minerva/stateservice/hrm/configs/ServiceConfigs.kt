package org.minerva.stateservice.hrm.configs

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@PropertySource("classpath:dbs-service.properties")
open class ServiceConfigs(
    @Value("\${host}")
    var host: String,

    @Value("\${auth}")
    var auth: String,

    @Value("\${server-auth}")
    var serverAuth: String,
)