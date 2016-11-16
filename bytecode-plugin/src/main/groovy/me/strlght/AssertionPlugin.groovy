package me.strlght

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project;

class AssertionPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        if (project.hasProperty('android')) {
            project.android.registerTransform(new AssertionTransform(project))
        } else {
            throw new GradleException("Plugin must be applied after Android plugin")
        }
    }
}