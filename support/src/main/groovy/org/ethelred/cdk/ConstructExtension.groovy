package org.ethelred.cdk

import groovy.util.logging.Log
import software.constructs.Construct

import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException
import java.util.stream.Stream

@Log
class ConstructExtension {
    static GroovyClassLoader loader

//    static boolean isConstruct(Class<?> type) {
//        return Construct.class.isAssignableFrom(type)
//    }

    static <T> PropsBuilder<T> getPropsBuilder(Class<?> type) {
        try {
            var constructor = findConstructor(type, true)
            var propsType = constructor.getParameterTypes()[2]
            var builderMethod = propsType.getMethod("builder")
            return new PropsBuilder<>(builderMethod.invoke(null))
        }
        catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("No props builder method found for " + type, e)
        }
    }

    @SuppressWarnings("unchecked") // safe because we do not modify the array of constructors, and getConstructors is documented as actually returning Constructor<T>[]
    static <T> Constructor<T> findConstructor(Class<T> type, boolean withProps) {
        return (Constructor<T>) Stream.of(type.getConstructors())
                .filter(c -> c.getParameterCount() == (withProps ? 3 : 2))
                .filter(c -> Construct.isAssignableFrom(c.getParameterTypes()[0]))
                .filter(c -> String.class.equals(c.getParameterTypes()[1]))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No constructor found for $type with props $withProps"))
    }

    static <T extends Construct> Class<T> defineClass(Class<? super T> superType, String name, Map<String, Class> propTypes, @DelegatesTo(value = Scope.class, strategy = Closure.DELEGATE_FIRST) Closure<?> initializer) {
        try {
            def checkType = Class.forName(name)
            if (checkType && !Construct.isAssignableFrom(checkType)) {
                name = "${name}\$Cdk"
            }
        } catch (Exception e) {
//            throw new RuntimeException("Oh dear", e)
        }
        def body
        if (propTypes) {
            var propsBody = """
                @groovy.transform.builder.Builder
                class ${name}Props {
                    ${propTypes.collect {k, v -> "${v instanceof Class ? v.name : v} $k\n"}.join("")}
                }
                """
            log.info(propsBody)
            loader.parseClass(propsBody)
            body = """
                import software.constructs.Construct
                class $name extends ${superType.name} {
                    $name(Construct parent, String id, ${name}Props props) {
                        super(parent, id);
                        runInit(this, props);
                    }
                }
                """
        } else {
            body = """
                import software.constructs.Construct
                class $name extends ${superType.name} {
                    $name(Construct parent, String id) {
                        super(parent, id);
                        runInit(this, null);
                    }
                }
                """
        }
        var type = loader.parseClass(body)
        type.metaClass.runInit << { parent, props ->
            def scope = new Scope(parent)
            initializer.resolveStrategy = Closure.DELEGATE_FIRST
            initializer.delegate = scope
            initializer(props)
        }
        type
    }
}
