import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
    id("com.vanniktech.maven.publish")
}

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.javaVersion.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.javaVersion.get())
}

kotlin {
    jvmToolchain(libs.versions.javaVersion.get().toInt())
}

dependencies {
    implementation(gradleApi())
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.compiler)
    implementation(libs.gradle.plugin.api)
    implementation(libs.autoservice.annotations)
    ksp(libs.autoservice.ksp)
    testImplementation(libs.junit)
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.compilerOptions {
    // Kotlin 2.2 起编译器内部 API（IR/FIR 扩展点）标记为 DeprecatedForRemovalCompilerApi，
    // 需要 opt-in 才能使用；待上游提供稳定 API 后移除。
    freeCompilerArgs.set(
        listOf(
            "-Xnon-local-break-continue",
            "-Xcontext-parameters",
            "-opt-in=org.jetbrains.kotlin.DeprecatedForRemovalCompilerApi"
        )
    )
}