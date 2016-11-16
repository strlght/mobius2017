package me.strlght;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Names;

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
public class BindViewAdvancedVisitor extends ElementScanner7<Void, Void> {
    private final CodeBlock.Builder bindViewBlock = CodeBlock.builder();
    private final Trees trees;
    private final Messager logger;
    private final Filer filer;
    private final TypeElement originElement;
    private final TreeMaker treeMaker;
    private final Names names;

    public BindViewAdvancedVisitor(ProcessingEnvironment env, TypeElement element) {
        super();
        trees = Trees.instance(env);
        logger = env.getMessager();
        filer = env.getFiler();
        originElement = element;
        final JavacProcessingEnvironment javacEnv = (JavacProcessingEnvironment) env;
        treeMaker = TreeMaker.instance(javacEnv.getContext());
        names = Names.instance(javacEnv.getContext());
    }

    @Override
    public Void visitVariable(VariableElement e, Void aVoid) {
        ((JCTree) trees.getTree(e)).accept(new TreeTranslator() {
            @Override
            public void visitVarDef(JCTree.JCVariableDecl jcVariableDecl) {
                super.visitVarDef(jcVariableDecl);
                jcVariableDecl.mods.flags &= ~Flags.PRIVATE;
            }
        });
        final BindView bindView = e.getAnnotation(BindView.class);
        bindViewBlock.addStatement("(($T) this).$L = ($T) findViewById($L)",
                ClassName.get(originElement), e.getSimpleName(), ClassName.get(e.asType()), bindView.value());
        return super.visitVariable(e, aVoid);
    }

    public void generate() {
        final String className = originElement.getSimpleName() + "$$ViewBinding";
        final TypeSpec typeSpec = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.ABSTRACT)
                .superclass(ClassName.get(originElement.getSuperclass()))
                .addOriginatingElement(originElement)
                .addMethod(MethodSpec.methodBuilder("setContentView")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(TypeName.INT, "layoutResId")
                        .addStatement("super.setContentView(layoutResId)")
                        .addCode(bindViewBlock.build())
                        .build())
                .build();
        final JavaFile javaFile = JavaFile.builder(originElement.getEnclosingElement().toString(), typeSpec)
                .addFileComment("Generated, do not modify")
                .build();
        try {
            javaFile.writeTo(filer);
            JCTree.JCExpression selector = treeMaker.Ident(names.fromString(javaFile.packageName));
            selector = treeMaker.Select(selector, names.fromString(typeSpec.name));
            ((JCTree.JCClassDecl) trees.getTree(originElement)).extending = selector;
        } catch (IOException exception) {
            logger.printMessage(Diagnostic.Kind.ERROR, "Can't write file: " + exception.getMessage());
        }
    }
}
