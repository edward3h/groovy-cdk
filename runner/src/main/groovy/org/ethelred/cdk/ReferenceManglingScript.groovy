package org.ethelred.cdk

/**
 * TODO move dynamic behaviour to AST transforms
 */
abstract class ReferenceManglingScript extends Script {
    def methodMissing(String name, args) {
        if (args.length == 1 && args[0] instanceof Closure) {
            new Tuple2(name, args[0] as Closure)
        }
    }

    def propertyMissing(String name) {
//        throw new RuntimeException("property missing $name")
        name
    }

    def propertyMissing(String name, value) {
        throw new RuntimeException("$name = $value")
    }
}
