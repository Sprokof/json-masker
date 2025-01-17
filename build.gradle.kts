import org.sonarqube.gradle.SonarTask

plugins {
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.test.logger)
    alias(libs.plugins.nexus.publish)
    alias(libs.plugins.jmh)
    `maven-publish`
    `java-library`
    signing
    jacoco
}

description = "High-performance JSON masker in Java with no runtime dependencies"

group = "dev.blaauwendraad"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
    withJavadocJar()
    withSourcesJar()
    registerFeature("nullabilityAnnotations") {
        usingSourceSet(sourceSets["main"])
    }
}

dependencies {
    "nullabilityAnnotationsImplementation"(libs.findbugs)

    testImplementation(libs.assertj.core)
    testImplementation(libs.jackson.databind)
    testImplementation(libs.junit.api)
    testImplementation(libs.junit.params)
    testRuntimeOnly(libs.junit.engine)

    jmh(libs.jmh.core)
    jmhAnnotationProcessor(libs.jmh.generator.annproccesor)
}

publishing {
    repositories {
        maven {
            val url = if (version.toString().endsWith("SNAPSHOT")) {
                "https://s01.oss.sonatype.org/content/repositories/snapshots/"
            } else {
                "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
            }
            setUrl(url)
        }
    }

    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            pom {
                name = project.name
                description = project.description
                url = "https://github.com/Breus/json-masker"
                licenses {
                    license {
                        name = "MIT License"
                        url = "https://opensource.org/license/mit/"
                    }
                }
                developers {
                    developer {
                        id = "breus"
                        name = "Breus Blaauwendraad"
                        email = "b.blaauwendraad@gmail.com"
                    }
                    developer {
                        id = "gavlyukovskiy"
                        name = "Arthur Gavlyukovskiy"
                        email = "agavlyukovskiy@gmail.com"
                    }
                }
                scm {
                    connection = "scm:git:git://github.com/Breus/json-masker.git"
                    url = "https://github.com/Breus/json-masker"
                }
                issueManagement {
                    url = "https://github.com/Breus/json-masker/issues"
                }
            }
        }
    }
}

val sonatypeUser = System.getenv("SONATYPE_USER")
val sonatypePassword = System.getenv("SONATYPE_PASSWORD")
val gpgKey = System.getenv("GPG_PRIV_KEY")
val gpgPassphrase = System.getenv("GPG_PASS_PHRASE")

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl = uri("https://s01.oss.sonatype.org/service/local/")
            snapshotRepositoryUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            username = sonatypeUser
            password = sonatypePassword
        }
    }
}

signing {
    isRequired = gpgKey != null
    useInMemoryPgpKeys(gpgKey, gpgPassphrase)
    sign(*publishing.publications.toTypedArray())
}

jmh {
    fun listOfProperty(vararg values: String) = rootProject.objects.listProperty<String>().value(values.toList())
    // run this with ./gradlew jmh -PjmhShort to only run these parameters and 4 iterations
    if (project.hasProperty("jmhShort")) {
        benchmarkParameters = mapOf(
            "jsonSize" to listOfProperty("2mb"),
            "maskedKeyProbability" to listOfProperty("0.01"),
            "jsonPath" to listOfProperty("false", "true"),
            "characters" to listOfProperty("unicode")
        )

        iterations = 2
    }
    // if you have async profiler installed, you can provide it to generate flamegraphs
    // ./gradlew jmh -PjmhAsyncProfilerLibPath=/workspace/async-profiler/lib/libasyncProfiler.so (for MacOS - libasyncProfiler.dylib)
    // the results will be stored in build/results/jmh/async-profiler and can be opened in IDEA or Java Flight Recorder
    if (project.hasProperty("jmhAsyncProfilerLibPath")) {
        profilers = listOfProperty("async:libPath=${project.property("jmhAsyncProfilerLibPath")};output=jfr;dir=build/results/jmh/async-profiler")
    }
}

sonar {
    properties {
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.organization", "breus")
        property("sonar.projectKey", "Breus_json-masker")
    }
}

tasks {
    test {
        useJUnitPlatform()
    }

    compileJava {
        options.encoding = "UTF-8"
    }

    compileTestJava {
        options.encoding = "UTF-8"
    }

    javadoc {
        val options = options as StandardJavadocDocletOptions
        options.addBooleanOption("html5", true)
        options.addStringOption("Xdoclint:all,-missing", "-quiet")
    }

    jacocoTestReport {
        reports {
            xml.required = true
        }
    }

    withType<SonarTask> {
        dependsOn(jacocoTestReport)
    }
}