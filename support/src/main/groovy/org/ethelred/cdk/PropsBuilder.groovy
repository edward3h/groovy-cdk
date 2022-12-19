package org.ethelred.cdk

class PropsBuilder<T> {
    Object delegate

    PropsBuilder(Object builder) {
        delegate = builder
    }

    T build() {
        delegate.build()
    }

    def methodMissing(String name, args) {
        delegate.invokeMethod(name, args)
    }
}
