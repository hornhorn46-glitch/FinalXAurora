// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    // Intentionally left empty. The actual plugins are configured in settings.gradle.kts and module build files.
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}