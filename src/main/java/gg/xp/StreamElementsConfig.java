package gg.xp;

import io.micronaut.context.annotation.ConfigurationInject;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.annotation.Introspected;

@ConfigurationProperties("streamelements")
@Introspected
public record StreamElementsConfig(
    String jwtToken,
    String apiBase,
    String startTimestamp
) {
    @ConfigurationInject
    public StreamElementsConfig {}
}
