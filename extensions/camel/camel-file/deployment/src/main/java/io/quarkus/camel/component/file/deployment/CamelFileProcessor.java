package io.quarkus.camel.component.file.deployment;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;

import javax.inject.Inject;

import org.apache.camel.component.file.GenericFile;
import org.apache.camel.component.file.GenericFileProcessStrategy;
import org.apache.camel.component.file.strategy.GenericFileProcessStrategyFactory;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.substrate.ReflectiveClassBuildItem;

class CamelFileProcessor {

    @Inject
    BuildProducer<ReflectiveClassBuildItem> reflectiveClass;
    @Inject
    CombinedIndexBuildItem combinedIndexBuildItem;

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FeatureBuildItem.CAMEL_FILE);
    }

    @BuildStep
    void process() {
        IndexView view = combinedIndexBuildItem.getIndex();

        Collections.singleton(GenericFileProcessStrategy.class).stream()
                .map(Class::getName)
                .map(DotName::createSimple)
                .map(view::getAllKnownImplementors)
                .flatMap(Collection::stream)
                .filter(CamelFileProcessor::isPublic)
                .forEach(v -> addReflectiveClass(true, v.name().toString()));

        addReflectiveClass(false, GenericFile.class.getName());
        addReflectiveClass(true, GenericFileProcessStrategyFactory.class.getName());
    }

    static boolean isPublic(ClassInfo ci) {
        return (ci.flags() & Modifier.PUBLIC) != 0;
    }

    void addReflectiveClass(boolean methods, String... className) {
        reflectiveClass.produce(new ReflectiveClassBuildItem(methods, false, className));
    }

}
