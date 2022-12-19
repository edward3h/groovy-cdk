package org.ethelred.cdk

import software.amazon.awscdk.App
import software.amazon.awscdk.Stack
import software.constructs.Construct

class CdkTypes {
    static <T extends Stack> Class<T> stack(String name, Map<String,Class> propTypes, Closure initializer) {
        Stack.defineClass(name, propTypes, initializer)
    }

    static <T extends Construct> Class<T> construct(String name, Map<String,Class> propTypes, Closure initializer) {
        Construct.defineClass(name, propTypes, initializer)
    }

    static void app(@DelegatesTo(value = Scope, strategy = Closure.DELEGATE_FIRST) Closure<Void> appContents) {
        def app = new App()
        def scope = new Scope(app)
        appContents.delegate = scope
        appContents.resolveStrategy = Closure.DELEGATE_FIRST
        appContents.call()
        app.synth()
    }

    static <T extends Construct> Class<T> construct(Map demangle, Class type) {
        construct(demangle, type.name)
    }

    static <T extends Construct> Class<T> construct(Map demangle, String name) {
        def tidy = demangle.collectEntries {k, v -> if(v instanceof Class) {[k,v]} else {[k,v[0]]}}
        def initializer = demangle.values().find { it !instanceof Class && it.size() > 1 && it[1] instanceof Closure }[1]
        construct(name, tidy, initializer)
    }

    static <T extends Stack> Class<T> stack(Tuple2<String, Closure> demangle) {
        stack(demangle.v1, null, demangle.v2)
    }
}
