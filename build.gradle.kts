plugins {
    java
    id("com.vanniktech.maven.publish") version "0.32.0" apply false
}

allprojects {
    group = property("group") as String
    version = property("version") as String

    repositories {
        maven {
            url = uri("https://maven.aliyun.com/repository/public")
        }
        // TODO 待 com.alibaba:druid-spring-boot-4-starter 完成发布后删除
        maven {
            url = uri("https://gitlab.xuxiaowei.com.cn/api/v4/projects/432/packages/maven")
        }
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "com.vanniktech.maven.publish")
    apply(plugin = "signing")

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        withSourcesJar()
        withJavadocJar()
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        // Refer to https://github.com/spring-projects/spring-framework/wiki/Upgrading-to-Spring-Framework-6.x#parameter-name-retention
        options.compilerArgs.add("-parameters")
    }

    tasks.withType<Javadoc> {
        options.encoding = "UTF-8"
        (options as StandardJavadocDocletOptions).apply {
            addStringOption("Xdoclint:none", "-quiet")
            addStringOption("encoding", "UTF-8")
            addStringOption("charSet", "UTF-8")
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    dependencies {
        compileOnly("org.projectlombok:lombok:${property("lombokVersion")}")
        annotationProcessor("org.projectlombok:lombok:${property("lombokVersion")}")
        testCompileOnly("org.projectlombok:lombok:${property("lombokVersion")}")
        testAnnotationProcessor("org.projectlombok:lombok:${property("lombokVersion")}")
    }
}
