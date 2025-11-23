plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktor)
    application
}

group = "be.ecam.server"
version = "1.0.0"
application {
    mainClass.set("be.ecam.server.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

val exposedVersion = "0.61.0"

dependencies {
    implementation(projects.shared)
    implementation(libs.logback)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.network.tls.certificates)

    // Test libs
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
    testImplementation("io.mockk:mockk:1.14.6") // or latest mockk
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
//    testImplementation("org.junit.jupiter:junit-jupiter:5.9.4")

    // SQLite Database
    implementation("org.jetbrains.exposed:exposed-core:0.61.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.61.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.61.0")
    implementation("org.xerial:sqlite-jdbc:3.50.3.0")

    // datetime
    implementation("org.jetbrains.exposed:exposed-java-time:${exposedVersion}")

}

//tasks.test {
//    // Run tests on JUnit Platform (supports JUnit Jupiter and kotlin.test adapters)
//    useJUnitPlatform()
//
//    testLogging {
//        events("started", "passed", "skipped", "failed")
//        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
//    }
//}







