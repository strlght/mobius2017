package me.strlght;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementScanner7;
import javax.tools.Diagnostic;

/**
 * @author Grigoriy Dzhanelidze
 */

public class BindViewBasicVisitor extends ElementScanner7<Void, Void> {
    private final CodeBlock.Builder bindViewBlock = CodeBlock.builder();
    private final Messager logger;
    private final Filer filer;
    private final TypeElement originElement;

    public BindViewBasicVisitor(ProcessingEnvironment env, TypeElement element) {
        super();
        logger = env.getMessager();
        filer = env.getFiler();
        originElement = element;
    }

    @Override
    public Void visitVariable(VariableElement e, Void aVoid) {
        if (e.getModifiers().contains(Modifier.PROTECTED)
                || e.getModifiers().contains(Modifier.PRIVATE)) {
            logger.printMessage(Diagnostic.Kind.ERROR, "Field " + e.getSimpleName() + "must be package-local or public");
        }
        if (e.getModifiers().contains(Modifier.FINAL)) {
            logger.printMessage(Diagnostic.Kind.ERROR, "Field " + e.getSimpleName() + "must be non-final");
        }
        final BindView bindView = e.getAnnotation(BindView.class);
        bindViewBlock.addStatement("param.$L = ($T) param.findViewById($L)",
                e.getSimpleName(), ClassName.get(e.asType()), bindView.value());
        return super.visitVariable(e, aVoid);
    }

    public void generate() {
        final String className = originElement.getSimpleName() + "_ViewBinding";
        final TypeSpec typeSpec = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.FINAL)
                .addMethod(MethodSpec.methodBuilder("bind")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .addParameter(TypeName.get(originElement.asType()), "param")
                        .addCode(bindViewBlock.build())
                        .build())
                .build();
        final JavaFile javaFile = JavaFile.builder(originElement.getEnclosingElement().toString(), typeSpec)
                .addFileComment("Generated, do not modify")
                .build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException exception) {
            logger.printMessage(Diagnostic.Kind.ERROR, "Can't write file: " + exception.getMessage());
        }
    }
}
