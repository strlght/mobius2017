package me.strlght;

import com.google.auto.service.AutoService;
import com.google.auto.value.extension.AutoValueExtension;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;

@AutoService(AutoValueExtension.class)
public class CreateTableAutovalueExtension extends AutoValueExtension {
    private static MethodSpec generateConstructor(Map<String, ExecutableElement> properties) {
        final List<ParameterSpec> params = new ArrayList<>();
        for (Map.Entry<String, ExecutableElement> entry : properties.entrySet()) {
            final TypeName typeName = TypeName.get(entry.getValue().getReturnType());
            params.add(ParameterSpec.builder(typeName, entry.getKey()).build());
        }

        final StringBuilder body = new StringBuilder("super(");
        for (int i = properties.size(); i > 0; i--) {
            body.append("$N");
            if (i > 1) {
                body.append(", ");
            }
        }
        body.append(")");

        return MethodSpec.constructorBuilder()
                .addParameters(params)
                .addStatement(body.toString(), properties.keySet().toArray())
                .build();
    }

    private static Set<String> getAnnotations(Element element) {
        final Set<String> set = new LinkedHashSet<>();

        final List<? extends AnnotationMirror> annotations = element.getAnnotationMirrors();
        for (AnnotationMirror annotation : annotations) {
            set.add(annotation.getAnnotationType().asElement().getSimpleName().toString());
        }

        return Collections.unmodifiableSet(set);
    }


    private static MethodSpec generateCreateTable(String className,
                                                  Map<String, ExecutableElement> properties) {
        final StringBuilder body = new StringBuilder("return \"CREATE TABLE ")
                .append(className).append(" (\" \n");
        int count = 0;
        boolean hasId = false;
        for (Map.Entry<String, ExecutableElement> entry : properties.entrySet()) {
            final ExecutableElement element = entry.getValue();
            String column = element.getSimpleName().toString();
            if ("id".equalsIgnoreCase(column)) {
                hasId = true;
                column = "_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT";
            }
            body.append("+ \"\\n").append(column);
            if (++count != properties.size()) {
                body.append(",");
            }
            body.append("\"\n");
        }
        body.append(" + \")\"");
        if (!hasId) {
            throw new IllegalStateException("Missing property id");
        }
        return MethodSpec.methodBuilder("createTable")
                .addModifiers(Modifier.PUBLIC)
                .returns(String.class)
                .addStatement(body.toString())
                .build();
    }

    @Override
    public boolean applicable(Context context) {
        return getAnnotations(context.autoValueClass()).contains("SQLTable");
    }

    @Override
    public boolean mustBeFinal(Context context) {
        return true;
    }

    @Override
    public String generateClass(Context context, String className, String classToExtend,
                                boolean isFinal) {
        final String packageName = context.packageName();
        final String originalName = context.autoValueClass().getSimpleName().toString();
        final Map<String, ExecutableElement> properties = context.properties();

        TypeSpec subclass = TypeSpec.classBuilder(className)
                .addModifiers(isFinal ? Modifier.FINAL : Modifier.ABSTRACT)
                .superclass(ClassName.get(packageName, classToExtend))
                .addMethod(generateConstructor(properties))
                .addMethod(generateCreateTable(originalName, properties))
                .build();

        JavaFile javaFile = JavaFile.builder(packageName, subclass).build();
        return javaFile.toString();
    }
}
