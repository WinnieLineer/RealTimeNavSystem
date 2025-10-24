
plugins {
    id("java")
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // H2 版本必須是 2.1.214 才能相容 JDK 1.8
    implementation("com.h2database:h2:2.1.214")
    // Guava (用於 Strings.repeat())
    implementation("com.google.guava:guava:33.2.1-jre")
}

application {
    mainClass = "com.example.realtimevalsystem.MainApplication"
}