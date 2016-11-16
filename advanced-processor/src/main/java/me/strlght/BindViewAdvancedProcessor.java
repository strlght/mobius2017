package me.strlght;

import com.google.auto.service.AutoService;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

@AutoService(Processor.class)
@SupportedAnnotationTypes({"me.strlght.BindView"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class BindViewAdvancedProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }

        final Map<TypeElement, BindViewAdvancedVisitor> visitors = new HashMap<>();
        final Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(BindView.class);
        for (Element element : elements) {
            final TypeElement object = (TypeElement) element.getEnclosingElement();
            BindViewAdvancedVisitor visitor = visitors.get(object);
            if (visitor == null) {
                visitor = new BindViewAdvancedVisitor(processingEnv, object);
                visitors.put(object, visitor);
            }
            element.accept(visitor, null);
        }

        for (BindViewAdvancedVisitor visitor : visitors.values()) {
            visitor.generate();
        }

        return true;
    }

}
