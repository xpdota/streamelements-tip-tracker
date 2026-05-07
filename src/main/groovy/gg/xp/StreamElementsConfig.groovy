package gg.xp

import groovy.transform.CompileStatic
import io.micronaut.context.annotation.ConfigurationInject
import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.core.annotation.Introspected

@ConfigurationProperties("streamelements")
@Introspected
@CompileStatic
class StreamElementsConfig {
    final String jwtToken
    final String apiBase
    final String startTimestamp

    @ConfigurationInject
    StreamElementsConfig(String jwtToken, String apiBase, String startTimestamp) {
        this.jwtToken = jwtToken
        this.apiBase = apiBase
        this.startTimestamp = startTimestamp
    }
}
