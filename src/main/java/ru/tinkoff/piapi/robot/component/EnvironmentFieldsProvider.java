package ru.tinkoff.piapi.robot.component;

import ch.qos.logback.core.spi.DeferredProcessingAware;
import com.fasterxml.jackson.core.JsonGenerator;
import net.logstash.logback.composite.AbstractJsonProvider;
import net.logstash.logback.composite.JsonWritingUtils;

import java.io.IOException;
import java.util.Objects;

public class EnvironmentFieldsProvider<Event extends DeferredProcessingAware> extends AbstractJsonProvider<Event> {

    public static final String ENVIRONMENT_ENV_NAME = "env.name";
    public static final String ENVIRONMENT_SYSTEM_NAME = "system.name";
    public static final String ENVIRONMENT_POD_NAME = "POD_NAME";
    public static final String FIELD_SAGE_ENV_NAME = "env";
    public static final String FIELD_SAGE_SYSTEM_NAME = "system";
    public static final String FIELD_SAGE_INST_NAME = "inst";

    private static final String DEFAULT_ENV = "dev";
    private static final String DEFAULT_NAME = "piapi-undefined";
    private static final String DEFAULT_INST = "piapi-instance-undefined";

    private final String environment;
    private final String system;
    private final String instance;

    public EnvironmentFieldsProvider() {
        environment = System.getProperty(ENVIRONMENT_ENV_NAME, DEFAULT_ENV);
        system = System.getProperty(ENVIRONMENT_SYSTEM_NAME, DEFAULT_NAME);
        instance = Objects.requireNonNullElse(System.getenv(ENVIRONMENT_POD_NAME), DEFAULT_INST);
    }

    @Override
    public void writeTo(JsonGenerator generator, Event event) throws IOException {
        JsonWritingUtils.writeStringField(generator, FIELD_SAGE_ENV_NAME, environment);
        JsonWritingUtils.writeStringField(generator, FIELD_SAGE_SYSTEM_NAME, system);
        JsonWritingUtils.writeStringField(generator, FIELD_SAGE_INST_NAME, instance);
    }
}
