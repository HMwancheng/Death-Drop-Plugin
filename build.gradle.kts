plugins {
    java
}

group = "com.deathdrop"
version = "1.0.0"

// 如果目标服务端版本不同，修改此处 paper-api 版本号即可
val paperApiVersion = "1.21.1-R0.1-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:$paperApiVersion")
}

java {
    // 高版本服务端 (1.21.x) 需要 JDK 21
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
    processResources {
        // 将版本号写入 plugin.yml
        filesMatching("plugin.yml") {
            expand("pluginVersion" to project.version)
        }
    }
}