import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

val kotlin_version: String by project
val ktor_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "2.0.0"
    id("io.ktor.plugin") version "2.3.12"
    kotlin("plugin.serialization") version "1.8.22"
    id("org.openapi.generator") version "7.4.0"
}

group = "com.example"
version = "0.0.1"

application {
    mainClass.set("com.example.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("ch.qos.logback:logback-classic:$logback_version")

    // ktor
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-resources:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    implementation("io.ktor:ktor-server-openapi:$ktor_version")
    implementation("io.ktor:ktor-server-openapi:$ktor_version")
    implementation("io.ktor:ktor-server-swagger:$ktor_version")

    // openapi
    implementation("org.openapitools:openapi-generator-gradle-plugin:7.4.0")
//    implementation("org.openapitools:jackson-databind-nullable:2.12.3")

    // test
    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}

// OpenAPI Generatorタスクの設定
val generateApiTask = tasks.register<GenerateTask>("generateApi") {
    generatorName.set("kotlin")
    inputSpec.set("$rootDir/src/main/api/openapi.yaml")
    outputDir.set("$rootDir")  // 生成コードの出力ディレクトリ
//    outputDir.set("$buildDir/generated/src/main/kotlin")
    templateDir.set("$rootDir/src/main/api/templates")
    apiPackage.set("com.example.api")
    modelPackage.set("com.example.apiSchema")
    configOptions.set(
        mapOf(
            "library" to "jvm-ktor",
            "dateLibrary" to "java8"
        )
    )
    globalProperties.set(
        mapOf(
            "models" to "", // モデルのみ生成
//        TODO Paths.ktを生成できるようにする
//        "supportingFiles" to "Paths.kt" // Paths.ktのみ生成
//        "apis" to "",   // APIのみ生成
        )
    )
}

sourceSets {
    main {
        kotlin {
            srcDirs("$buildDir/generated/src/main/kotlin")
        }
    }
}
