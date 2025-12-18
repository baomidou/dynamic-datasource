buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        if (JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_17)) {
            classpath("org.graalvm.buildtools:native-gradle-plugin:0.11.3")
        }
    }
}

plugins {
    `java-library`
    id("io.spring.dependency-management") version "1.1.4"
}

// Only apply GraalVM plugin when running on Java 17+
if (JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_17)) {
    apply(plugin = "org.graalvm.buildtools.native")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.2.1")
    }
}

dependencies {
    api(project(":dynamic-datasource-spring-boot-common"))
    
    compileOnly("org.springframework.boot:spring-boot-starter-web")
    compileOnly("com.alibaba:druid-spring-boot-3-starter:${property("druidVersion")}")
    compileOnly("org.springframework.boot:spring-boot-starter-actuator")
    compileOnly("com.zaxxer:HikariCP")
    compileOnly("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    
    testImplementation("com.h2database:h2:${property("h2Version")}")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-web")
}
