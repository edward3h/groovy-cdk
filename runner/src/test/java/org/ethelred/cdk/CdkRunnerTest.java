package org.ethelred.cdk;

import org.junit.jupiter.api.Test;

public class CdkRunnerTest {
    @Test
    public void runHelloWorld() {
        CdkRunner.main(new String[]{"src/test/resources/hello-world/cdk.groovy"});
    }
}
