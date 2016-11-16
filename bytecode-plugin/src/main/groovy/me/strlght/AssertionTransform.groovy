package me.strlght

import com.android.build.api.transform.*
import org.gradle.api.Project

class AssertionTransform extends Transform {
    Project project

    AssertionTransform(Project project) {
        this.project = project
    }

    @Override
    String getName() {
        return "assertion"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return EnumSet.of(QualifiedContent.DefaultContentType.CLASSES)
    }

    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return EnumSet.of(QualifiedContent.Scope.PROJECT)
    }

    @Override
    boolean isIncremental() {
        return true
    }

    @Override
    void transform(Context context, Collection<TransformInput> inputs,
                   Collection<TransformInput> referencedInputs, TransformOutputProvider outputProvider,
                   boolean isIncremental) throws IOException, TransformException, InterruptedException {
        final DirectoryInput directoryInput = inputs.first().directoryInputs.first()
        final File output = outputProvider.getContentLocation(
                directoryInput.name, EnumSet.of(QualifiedContent.DefaultContentType.CLASSES),
                EnumSet.of(QualifiedContent.Scope.PROJECT), Format.DIRECTORY)
        final Collection<File> classpath = extractFiles(referencedInputs, isIncremental)
        final def processor = new AssertionProcessor(
                directoryInput.file,
                new File(context.temporaryDir, "src"),
                output,
                classpath,
                project.android.bootClasspath.toList()
        )
        try {
            processor.process()
        } catch (final Exception exception) {
            throw new TransformException(exception)
        }
    }

    static Collection<File> extractFiles(Collection<TransformInput> referencedInputs, boolean isIncremental) {
        if (isIncremental) {
            return referencedInputs.collectMany {
                it.directoryInputs.collect { it.file } + it.jarInputs.collect { it.file }
            }
        } else {
            // extract modified files only
            return referencedInputs.collectMany {
                it.directoryInputs.collect {
                    it.changedFiles.findAll {
                        it.value == Status.ADDED || it.value == Status.CHANGED
                    }
                    .collect { it.key }
                }
            }
        }
    }
}