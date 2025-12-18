plugins {
    java
    `maven-publish`
    signing
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
    apply(plugin = "maven-publish")
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
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
                
                pom {
                    name.set(project.name)
                    description.set("dynamic datasource")
                    url.set("https://github.com/baomidou/dynamic-datasource-spring-boot-starter")
                    inceptionYear.set("2018")
                    
                    licenses {
                        license {
                            name.set("Apache License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0")
                        }
                    }
                    
                    organization {
                        name.set("baomidou")
                        url.set("https://github.com/baomidou")
                    }
                    
                    developers {
                        developer {
                            name.set("TaoYu")
                            email.set("tracy5546@gmail.com")
                        }
                    }
                    
                    scm {
                        url.set("https://github.com/baomidou/dynamic-datasource-spring-boot-starter")
                        connection.set("scm:git:https://github.com/baomidou/dynamic-datasource-spring-boot-starter.git")
                        developerConnection.set("scm:git:https://github.com/baomidou/dynamic-datasource-spring-boot-starter.git")
                        tag.set("HEAD")
                    }
                }
            }
        }
        
        repositories {
            maven {
                name = "ossrh"
                val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                val snapshotsRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots")
                url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
                credentials {
                    username = project.findProperty("ossrhUsername") as String? ?: ""
                    password = project.findProperty("ossrhPassword") as String? ?: ""
                }
            }
        }
    }

    signing {
        sign(publishing.publications["mavenJava"])
    }
}
