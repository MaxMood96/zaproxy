import java.time.LocalDate
import java.util.stream.Collectors

plugins {
    `java-library`
    org.zaproxy.zap.distributions
    org.zaproxy.zap.installers
    org.zaproxy.zap.publish
}

group = "org.zaproxy"
version = "2.8.0-SNAPSHOT"

val creationDate by extra { project.findProperty("creationDate") ?: LocalDate.now().toString() }
val distDir = file("src/main/dist/")

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    api("com.fifesoft:rsyntaxtextarea:2.5.8")
    api("com.github.zafarkhaja:java-semver:0.8.0")
    api("com.googlecode.java-diff-utils:diffutils:1.2.1")
    api("commons-beanutils:commons-beanutils:1.8.3")
    api("commons-codec:commons-codec:1.9")
    api("commons-collections:commons-collections:3.2.2")
    api("commons-configuration:commons-configuration:1.9")
    api("commons-httpclient:commons-httpclient:3.1")
    api("commons-io:commons-io:2.4")
    api("commons-lang:commons-lang:2.6")
    api("org.apache.commons:commons-lang3:3.7")
    api("org.apache.commons:commons-text:1.3")
    api("edu.umass.cs.benchlab:harlib:1.1.2")
    api("javax.help:javahelp:2.0.05")
    api("log4j:log4j:1.2.17")
    api("net.htmlparser.jericho:jericho-html:3.1")
    api("net.sf.json-lib:json-lib:2.4:jdk15")
    api("org.apache.commons:commons-csv:1.1")
    api("org.bouncycastle:bcmail-jdk15on:1.52")
    api("org.bouncycastle:bcprov-jdk15on:1.52")
    api("org.bouncycastle:bcpkix-jdk15on:1.52")
    api("org.hsqldb:hsqldb:2.3.4")
    api("org.jdom:jdom:1.1.3")
    api("org.jfree:jfreechart:1.0.19")
    api("org.jgrapht:jgrapht-core:0.9.0")
    api("org.swinglabs.swingx:swingx-all:1.6.4")
    api("org.xerial:sqlite-jdbc:3.8.11.1")

    implementation("commons-validator:commons-validator:1.6")
    // Don't need its dependencies, for now.
    implementation("org.jitsi:ice4j:1.0") {
        setTransitive(false)
    }

    runtimeOnly("commons-jxpath:commons-jxpath:1.3")
    runtimeOnly("commons-logging:commons-logging:1.2")
    runtimeOnly("com.io7m.xom:xom:1.2.10") {
        setTransitive(false)
    }

    testImplementation("junit:junit:4.11")
    testImplementation("org.hamcrest:hamcrest-all:1.3")
    testImplementation("org.mockito:mockito-all:1.10.8")

    testRuntimeOnly(files(distDir))
}

tasks.register<JavaExec>("run") {
    group = ApplicationPlugin.APPLICATION_GROUP
    description = "Runs ZAP from source, using the default dev home."

    main = "org.zaproxy.zap.ZAP"
    classpath = sourceSets["main"].runtimeClasspath
    workingDir = distDir
}

listOf("jar", "jarDaily").forEach {
    tasks.named<Jar>(it) {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
        dirMode = "0755".toIntOrNull(8)
        fileMode = "0644".toIntOrNull(8)

        val attrs = mapOf(
                "Main-Class" to "org.zaproxy.zap.ZAP",
                "Implementation-Version" to ToString({ archiveVersion.get() }),
                "Create-Date" to creationDate,
                "Class-Path" to ToString({ configurations.runtimeClasspath.get().files.stream().map { file -> "lib/${file.name}" }.sorted().collect(Collectors.joining(" ")) }))

        manifest {
            attributes(attrs)
        }
    }
}

tasks.named<Javadoc>("javadoc") {
    title = "OWASP Zed Attack Proxy"
    source = sourceSets["main"].allJava.matching {
        include("org/parosproxy/**")
        include("org/zaproxy/**")
    }
    (options as StandardJavadocDocletOptions).run {
        links("https://docs.oracle.com/javase/8/docs/api/")
        encoding = "UTF-8"
        source("${java.targetCompatibility}")
    }
}

launch4j {
    jar = tasks.named<Jar>("jar").get().archiveFileName.get()
}

class ToString(private val callable: Callable<String>) {
    override fun toString() = callable.call()
}
