import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

// Version constants
val ktorVersion = "2.3.12"
val logbackVersion = "1.4.14"
val coroutinesVersion = "1.7.3"
val kotestVersion = "5.8.1"
val mockkVersion = "1.13.10"
val openapiGeneratorVersion = "7.4.0"
val jvmTargetVersion = 21

plugins {
    kotlin("jvm") version "2.0.20"
    id("io.ktor.plugin") version "2.3.12"
    kotlin("plugin.serialization") version "2.0.20"
    id("org.openapi.generator") version "7.4.0"
}

group = "com.example"
version = "0.0.1"

application {
    mainClass.set("com.example.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

kotlin {
    jvmToolchain(jvmTargetVersion)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    // Ktor
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-resources:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-server-openapi:$ktorVersion")
    implementation("io.ktor:ktor-server-swagger:$ktorVersion") // Removed duplicate

    // OpenAPI
    implementation("org.openapitools:openapi-generator-gradle-plugin:$openapiGeneratorVersion")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

    // Testing
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktorVersion")

    // Kotest
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-framework-datatest:$kotestVersion")
    testImplementation("io.kotest:kotest-property:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-json:$kotestVersion")

    // MockK
    testImplementation("io.mockk:mockk:$mockkVersion")

    // Coroutines test
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
}

// OpenAPI Generator task configuration
val generateApiTask = tasks.register<GenerateTask>("generateApi") {
    generatorName.set("kotlin-server")
    inputSpec.set("$rootDir/src/main/api/openapi.yaml")

    // Place generated files directly in appropriate directories
    outputDir.set("$rootDir")

    templateDir.set("$rootDir/src/main/api/templates")
    apiPackage.set("com.example.api")
    modelPackage.set("com.example.apiSchema")
    // Set packageName same as apiPackage to prevent extra file generation
    packageName.set("com.example.api")

    configOptions.putAll(
        mapOf(
            "library" to "ktor",
            "serializationLibrary" to "kotlinx_serialization",
            "useCoroutines" to "true",
            "dateLibrary" to "java8",
            "sourceFolder" to "src/main/kotlin"  // Specify output source folder
        )
    )

    // Global properties configuration
    // Empty models generates only model classes
    // Empty apis generates empty API interfaces (which will be deleted)
    globalProperties.set(
        mapOf(
            "models" to "",
            "apis" to "",
            "supportingFiles" to "Paths.kt" // Only ApiPaths class will be generated
        )
    )

    // Add task to delete HealthcheckApi.kt after generation
    doLast {
        delete(fileTree("$rootDir/src/main/kotlin/com/example/api") {
            include("**/*Api.kt")
        })
    }
}

// Kotest configuration
tasks.withType<Test> {
    useJUnitPlatform()
}