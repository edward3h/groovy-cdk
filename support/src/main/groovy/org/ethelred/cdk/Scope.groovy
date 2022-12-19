package org.ethelred.cdk

import groovy.util.logging.Log
import software.constructs.Construct
import static org.ethelred.cdk.ConstructExtension.*

@Log
class Scope {
    private final Construct parent

    Scope(Construct parent) {
        this.parent = parent
    }

    def <T extends Construct> T create(Class<T> type, String id, Closure<?> propsSetter) {
        def indirectType = type
        if (Script.isAssignableFrom(type)) {
            indirectType = Class.forName("${type}\$Cdk")
        }
        log.info("Creating $indirectType ${indirectType.superclass}")
        if (propsSetter) {
            def propsBuilder = getPropsBuilder(indirectType)
            propsSetter.delegate = propsBuilder
            propsSetter.resolveStrategy = Closure.DELEGATE_ONLY
            propsSetter.call()
            def props = propsBuilder.build()
            def constructor = findConstructor(indirectType, true)
            constructor.newInstance(parent, id, props)
        } else {
            def constructor = findConstructor(indirectType, false)
            constructor.newInstance(parent, id)
        }
    }

    def <T extends Construct> T create(Class<T> type, String id) {
        create(type, id, null)
    }

    def <T extends Construct> T create(Class<T> type, Closure<?> propsSetter) {
        create(type, type.simpleName, propsSetter)
    }

    def <T extends Construct> T create(Class<T> type) {
        create(type, type.simpleName)
    }
}
