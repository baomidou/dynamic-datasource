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
    api(project(":dynamic-datasource-spring"))
    
    compileOnly("com.alibaba:druid:${property("druidVersion")}")
    compileOnly("com.zaxxer:HikariCP-java7:${property("hikaricpVersion")}")
    compileOnly("com.github.chris2018998:beecp:${property("beeCpVersion")}") {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    compileOnly("org.apache.commons:commons-dbcp2:${property("commonsDbcp2Version")}")
    compileOnly("com.atomikos:transactions-jdbc:${property("atomikosVersion")}")
    compileOnly("com.mchange:c3p0:${property("c3p0Version")}")
    api("com.oracle.database.spring:oracle-spring-boot-starter-ucp:${property("oracleUcpVersion")}") {
        isTransitive = false
    }
    api("com.oracle.database.jdbc:ucp:23.4.0.24.05")
    compileOnly("com.baomidou:mybatis-plus:${property("mybatisPlusVersion")}")
    
    // Use Spring Framework AOP directly instead of Spring Boot starter
    api("org.springframework:spring-aop")
    api("org.aspectj:aspectjweaver")
    api("org.springframework.boot:spring-boot-autoconfigure")
    compileOnly("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}
