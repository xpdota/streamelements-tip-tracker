package gg.xp

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Context
import jakarta.annotation.PostConstruct
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.DumperOptions

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Singleton
@CompileStatic
@Slf4j
@Context
class SettingsService {

    @Inject
    StreamElementsConfig defaults

    private final Path configPath = Paths.get("data", "settings.yml")
    private Map<String, String> dynamicConfig = [:]

    @PostConstruct
    void init() {
        try {
            if (!Files.exists(configPath.parent)) {
                Files.createDirectories(configPath.parent)
            }
            load()
        } catch (Exception e) {
            log.error("Failed to initialize data directory", e)
        }
    }

    void load() {
        if (Files.exists(configPath)) {
            try {
                Yaml yaml = new Yaml()
                Map<String, Object> loaded = (Map<String, Object>) yaml.load(Files.newInputStream(configPath))
                if (loaded) {
                    dynamicConfig["jwtToken"] = loaded["jwt-token"] as String
                    dynamicConfig["startTimestamp"] = loaded["start-timestamp"] as String
                    dynamicConfig["endTimestamp"] = loaded["end-timestamp"] as String
                }
            } catch (Exception e) {
                log.error("Failed to load dynamic settings from ${configPath}", e)
            }
        }
    }

    void saveJwt(String jwtToken) {
        dynamicConfig["jwtToken"] = jwtToken
        persist()
    }

    void saveTimeRange(String startTimestamp, String endTimestamp) {
        dynamicConfig["startTimestamp"] = startTimestamp
        dynamicConfig["endTimestamp"] = endTimestamp
        persist()
    }

    private void persist() {
        Map<String, String> toSave = [
            "jwt-token": dynamicConfig["jwtToken"],
            "start-timestamp": dynamicConfig["startTimestamp"],
            "end-timestamp": dynamicConfig["endTimestamp"]
        ]
        
        DumperOptions options = new DumperOptions()
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK)
        Yaml yaml = new Yaml(options)
        
        Files.newOutputStream(configPath).withCloseable { os ->
            os.write(yaml.dump(toSave).getBytes("UTF-8"))
        }
    }

    String getJwtToken() {
        return dynamicConfig["jwtToken"] ?: defaults.jwtToken
    }

    String getStartTimestamp() {
        return dynamicConfig["startTimestamp"] ?: defaults.startTimestamp
    }

    String getEndTimestamp() {
        return dynamicConfig["endTimestamp"] ?: null
    }

    boolean isTokenConfigured() {
        String token = getJwtToken()
        return token && token != "your_jwt_token"
    }
}
