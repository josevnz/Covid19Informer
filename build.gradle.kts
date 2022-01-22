plugins {
    `java-library`
    id ("org.sonatype.gradle.plugins.scan") version "2.2.2"
    application
    // id("org.owasp.dependencycheck") version "6.5.3"
}

ossIndexAudit {
    username = System.getenv("ossindexaudit_user")
    password = System.getenv("ossindexaudit_password")
    isAllConfigurations = true
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
    withJavadocJar()
    withSourcesJar()
}

application {
    mainClass.set("com.kodegeek.covid19.informer.ConsoleInformer")
}

version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("commons-cli:commons-cli:1.5.0")
    // implementation("org.owasp:dependency-check-gradle:6.5.3")
    testImplementation("junit:junit:4.13.2")
}

tasks.test {
    useJUnit()
    maxHeapSize = "1G"
}

tasks.register<Jar>("uberJar") {
    archiveClassifier.set("uber")

    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}