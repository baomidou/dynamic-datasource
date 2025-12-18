plugins {
    `java-library`
    id("io.spring.dependency-management") version "1.1.4"
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:${property("springBootDependenciesVersion")}")
    }
}

dependencies {
    api(project(":dynamic-datasource-spring-boot-common"))
    
    compileOnly("org.springframework.boot:spring-boot-starter-web")
    compileOnly("com.alibaba:druid-spring-boot-starter:${property("druidVersion")}")
    compileOnly("org.springframework.boot:spring-boot-starter-actuator")
    compileOnly("com.zaxxer:HikariCP")
    compileOnly("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    
    testImplementation("com.h2database:h2:${property("h2Version")}")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-web")
    // Make compileOnly dependencies available for tests
    testRuntimeOnly("com.alibaba:druid-spring-boot-starter:${property("druidVersion")}")
    testRuntimeOnly("com.zaxxer:HikariCP")
}
