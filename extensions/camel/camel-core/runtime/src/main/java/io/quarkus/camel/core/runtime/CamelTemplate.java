package io.quarkus.camel.core.runtime;

import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.camel.RoutesBuilder;
import org.apache.camel.spi.Registry;

import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.arc.runtime.BeanContainerListener;
import io.quarkus.camel.core.runtime.support.FastCamelRuntime;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.ShutdownContext;
import io.quarkus.runtime.annotations.Template;

@Template
public class CamelTemplate {

    public RuntimeValue<CamelRuntime> create(
            Registry registry,
            Properties properties,
            List<RuntimeValue<?>> builders,
            CamelConfig.BuildTime buildTimeConfig) {

        if (buildTimeConfig.deferInitPhase) {

        }
        FastCamelRuntime runtime = new FastCamelRuntime();

        runtime.setRegistry(registry);
        runtime.setProperties(properties);
        builders.stream()
                .map(RuntimeValue::getValue)
                .map(RoutesBuilder.class::cast)
                .forEach(runtime.getBuilders()::add);

        return new RuntimeValue<>(runtime);
    }

    public void init(
            BeanContainer beanContainer,
            RuntimeValue<CamelRuntime> runtime,
            List<String> builders,
            CamelConfig.BuildTime buildTimeConfig) throws Exception {

        FastCamelRuntime fcr = (FastCamelRuntime) runtime.getValue();
        fcr.setBeanContainer(beanContainer);

        builders.stream()
                .forEach(name -> {
                    try {
                        fcr.getBuilders().add((RoutesBuilder) Class.forName(name).newInstance());
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                });

        fcr.init(buildTimeConfig);
    }

    public void start(
            ShutdownContext shutdown,
            RuntimeValue<CamelRuntime> runtime,
            CamelConfig.Runtime runtimeConfig) throws Exception {

        runtime.getValue().start(runtimeConfig);

        //in development mode undertow is started eagerly
        shutdown.addShutdownTask(new Runnable() {
            @Override
            public void run() {
                try {
                    runtime.getValue().stop();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public BeanContainerListener initRuntimeInjection(RuntimeValue<CamelRuntime> runtime) {
        return container -> container.instance(CamelProducers.class).setCamelRuntime(runtime.getValue());
    }

}
