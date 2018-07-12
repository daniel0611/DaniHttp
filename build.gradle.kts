import groovy.util.Node
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "de.dani09"
version = "0.1.1"

buildscript {
    var kotlinVersion: String by extra
    kotlinVersion = "1.2.51"

    repositories {
        mavenCentral()
    }

    dependencies {
        classpath(kotlin("gradle-plugin", kotlinVersion))
    }
}

plugins {
    java
    `maven-publish`
    signing
    id("org.jetbrains.dokka") version "0.9.17"
}

apply {
    plugin("kotlin")
}

val kotlinVersion: String by extra

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8", kotlinVersion))
    testCompile("junit", "junit", "4.12")

    compile("commons-io:commons-io:2.6")
    compile("org.json:json:20180130")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

val dokka by tasks.getting(org.jetbrains.dokka.gradle.DokkaTask::class) {
    outputFormat = "html"
    outputDirectory = "$buildDir/javadoc"
}

val dokkaJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    classifier = "javadoc"
    from(dokka)
}

val sourcesJar by tasks.creating(Jar::class) {
    classifier = "sources"
    from(java.sourceSets["main"].allSource)
}

val fullJar by tasks.creating(Jar::class) {
    classifier = "with-dependencies"
    from(configurations.runtime.map { if (it.isDirectory) it as Any else zipTree(it) })
    with(tasks["jar"] as CopySpec)
}

tasks {
    "assemble"{
        dependsOn(sourcesJar)
        dependsOn(fullJar)
        dependsOn(dokkaJar)
    }
}

project.publishing {
    repositories {
        maven {
            url = uri("https://api.bintray.com/maven/dani09/DaniHttp/DaniHttp/;publish=1")
            credentials {
                username = System.getenv("BINTRAY_USER")
                password = System.getenv("BINTRAY_API_KEY")
            }
        }
    }
    (publications) {
        "mavenJava"(MavenPublication::class) {
            from(components["java"])
            artifact(dokkaJar)
            artifact(sourcesJar)

            pom.withXml {
                val node = this.asNode()
                node.appendNode("name", "DaniHttp")
                node.appendNode("description", "A small Http Library with an Builder like pattern")
                node.appendNode("url", "https://github.com/dani909/DaniHttp")

                val scm = Node(node, "scm")
                scm.appendNode("connection", "scm:https://github.com/dani909/DaniHttp.git")
                scm.appendNode("developerConnection", "scm:https://github.com/dani909/DaniHttp.git")
                scm.appendNode("url", "https://github.com/dani909/DaniHttp")

                val licenses = Node(node, "licenses")
                val license = Node(licenses, "license")
                license.appendNode("name", "MIT License")
                license.appendNode("url", "https://opensource.org/licenses/mit-license.php")

                val developers = Node(node, "developers")
                val developer = Node(developers, "developer")
                developer.appendNode("id", "dani09")
                developer.appendNode("name", "Daniel Huber")
                developer.appendNode("email", "daniel@dani09.de")
            }

        }
    }
}
