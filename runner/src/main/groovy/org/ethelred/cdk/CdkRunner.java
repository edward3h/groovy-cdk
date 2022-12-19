package org.ethelred.cdk;

import groovy.lang.GroovyShell;
import groovy.util.logging.Log;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import software.amazon.awscdk.App;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;

public class CdkRunner {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: CdkRunner <script path>");
            System.exit(1);
        }
        var scriptFile = Path.of(args[0]);
        if (!Files.isReadable(scriptFile)) {
            System.out.println("Could not read file " + scriptFile);
            System.exit(2);
        }
//        var moreArgs = new String[args.length - 1];
//        System.arraycopy(args, 1, moreArgs, 0, moreArgs.length);

        var compiler = new CompilerConfiguration();
        customize(compiler);

        try {
            var shell = new GroovyShell(compiler);
            ConstructExtension.setLoader(shell.getClassLoader());
            Files.walkFileTree(scriptFile.getParent(), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (!file.equals(scriptFile) && file.toString().endsWith(".groovy")) {
                        var script = shell.parse(file.toFile());
                        var basename = file.getFileName().toString().replaceAll("\\..*$", "");
                        if (script.getClass().getSimpleName().startsWith(basename)) {
                            script.run();
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            shell.evaluate(scriptFile.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void customize(CompilerConfiguration compiler) {
        compiler.setScriptBaseClass(ReferenceManglingScript.class.getName());
        var icz = new ImportCustomizer();
        icz.addStarImports(
                "software.amazon.awscdk",
                "software.amazon.awscdk.services.ec2",
                "software.amazon.awscdk.services.sqs",
                "software.amazon.awscdk.services.sns"
        );
        icz.addStaticStars(CdkTypes.class.getName());
        compiler.addCompilationCustomizers(icz);
    }
}
