import com.google.protobuf.gradle.*

plugins {
    java
    id("com.google.protobuf") version "0.8.17"
    id("io.freefair.lombok") version "5.3.0"
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web:2.6.2")
    implementation("javax.validation:validation-api:2.0.1.Final")

    //official public api sdk
    implementation("ru.tinkoff.piapi:java-sdk-core:1.0-M5")

    //rest
    implementation("com.konghq:unirest-java:3.13.6")

    //grpc
    implementation("io.grpc:grpc-all:1.42.1")
    implementation("io.grpc:grpc-netty:1.42.1")
    implementation("io.grpc:grpc-protobuf:1.42.1")
    implementation("io.grpc:grpc-stub:1.42.1")
    implementation("com.google.protobuf:protobuf-java:3.19.2")

    //db
    implementation("org.postgresql:postgresql:9.4.1212")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:2.6.2")
    implementation("org.liquibase:liquibase-core:4.6.2")

    implementation("com.google.guava:guava:30.1.1-jre")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus:1.8.1")
    compileOnly("org.projectlombok:lombok:1.18.22")
}

group = "ru.tinkoff.public.invest.api.robot"
version = "0.0.1"
description = "piapi-robot"
java.sourceCompatibility = JavaVersion.VERSION_11


sourceSets {
    main {
        java {
            srcDirs("build/generated/source/proto/main/grpc")
            srcDirs("build/generated/source/proto/main/java")
        }
    }
}


protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.18.1"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.24.0"
        }
    }
    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                id("grpc")
            }
        }
    }
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}
