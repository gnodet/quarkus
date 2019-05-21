package io.quarkus.camel.bean.runtime.graal;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.RecomputeFieldValue;
import com.oracle.svm.core.annotate.TargetClass;

class CamelBeanSubstitutions {
}

@TargetClass(className = "org.apache.camel.component.bean.BeanInfo")
final class Target_org_apache_camel_component_bean_BeanInfo {

    @Alias
    @RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FromAlias)
    private static List<Method> EXCLUDED_METHODS;

    static {
        EXCLUDED_METHODS = new ArrayList<>();
        // exclude all java.lang.Object methods as we dont want to invoke them
        EXCLUDED_METHODS.addAll(Arrays.asList(Object.class.getMethods()));
        // exclude all java.lang.reflect.Proxy methods as we dont want to invoke them
        EXCLUDED_METHODS.addAll(Arrays.asList(Proxy.class.getMethods()));
        try {
            // but keep toString as this method is okay
            EXCLUDED_METHODS.remove(Object.class.getDeclaredMethod("toString"));
            EXCLUDED_METHODS.remove(Proxy.class.getDeclaredMethod("toString"));
        } catch (Throwable e) {
            // ignore
        }
    }

}
