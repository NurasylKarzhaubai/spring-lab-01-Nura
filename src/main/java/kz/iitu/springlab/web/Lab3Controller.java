package kz.iitu.springlab.web;

import kz.iitu.springlab.config.AppProperties;
import kz.iitu.springlab.config.EnvironmentBanner;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/lab3")
public class Lab3Controller {
    private final AppProperties props;
    private final EnvironmentBanner banner;
    private final Environment environment;

    public Lab3Controller(AppProperties props, EnvironmentBanner banner, Environment environment) {
        this.props = props;
        this.banner = banner;
        this.environment = environment;
    }

    @GetMapping("/config")
    public Map<String, Object> config() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("owner", props.owner());
        data.put("group", props.group());
        data.put("mailFrom", props.mail().from());
        data.put("mailRetryCount", props.mail().retryCount());
        data.put("mailTimeout", props.mail().timeout().toString());
        data.put("mailEnabled", props.mail().enabled());
        data.put("securityTokenTtl", props.security().tokenTtl().toString());
        data.put("securityMinPasswordLength", props.security().minPasswordLength());
        data.put("serverPort", environment.getProperty("server.port"));
        data.put("activeProfiles", Arrays.asList(environment.getActiveProfiles()));
        data.put("banner", banner.describe());
        return data;
    }
}