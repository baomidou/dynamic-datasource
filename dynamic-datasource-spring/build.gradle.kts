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
    api(project(":dynamic-datasource-creator"))
    
    // Third-party dependencies
    compileOnly("com.baomidou:mybatis-plus:${property("mybatisPlusVersion")}")
    compileOnly("p6spy:p6spy:${property("p6spyVersion")}")
    compileOnly("io.seata:seata-rm-datasource:${property("seataVersion")}") {
        exclude(group = "com.alibaba", module = "druid")
    }
    
    // Spring dependencies
    api("org.springframework:spring-core")
    api("org.springframework:spring-beans")
    api("org.springframework:spring-context")
    api("org.springframework:spring-jdbc")
    api("org.springframework:spring-aop")
    api("org.springframework:spring-expression")
    api("org.mybatis:mybatis-spring:${property("mybatisSpringVersion")}")
}
