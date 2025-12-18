buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        if (JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_17)) {
            classpath("org.graalvm.buildtools:native-gradle-plugin:${property("graalvmPluginVersion")}")
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
        mavenBom("org.springframework.boot:spring-boot-dependencies:4.0.0")
    }
}

dependencies {
    api(project(":dynamic-datasource-spring-boot-common"))
    
    compileOnly("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.springframework.boot:spring-boot-jdbc")
    // TODO 待 com.alibaba:druid-spring-boot-4-starter 完成发布后修正版本号
    // compileOnly("com.alibaba:druid-spring-boot-4-starter:1.2.28-SNAPSHOT")
    compileOnly("org.springframework.boot:spring-boot-starter-actuator")
    compileOnly("com.zaxxer:HikariCP")
    compileOnly("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    
    testImplementation("com.h2database:h2:${property("h2Version")}")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-web")
}
