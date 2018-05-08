import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("org.springframework.boot") version "2.0.1.RELEASE"
  val kotlinVersion = "1.2.41"
  kotlin("jvm") version kotlinVersion
  id("org.jetbrains.kotlin.plugin.spring") version kotlinVersion
  id("org.jetbrains.kotlin.plugin.jpa") version kotlinVersion
  id("io.spring.dependency-management") version "1.0.4.RELEASE"
}

version = "1.0.0-SNAPSHOT"

tasks.withType<KotlinCompile> {
  kotlinOptions {
    jvmTarget = "1.8"
    freeCompilerArgs = listOf("-Xjsr305=strict")
  }
}

val test by tasks.getting(Test::class) {
  useJUnitPlatform()
}

repositories {
  mavenCentral()
}

dependencies {
  val xchangeVersion = "4.3.2"
  compile("org.springframework.boot:spring-boot-starter-actuator")
  compile("org.springframework.boot:spring-boot-starter-webflux")
  compile(kotlin("stdlib"))
  compile("com.fasterxml.jackson.module:jackson-module-kotlin")
  compile("info.bitrich.xchange-stream:xchange-stream-core:$xchangeVersion")
  compile("info.bitrich.xchange-stream:xchange-binance:$xchangeVersion")

  testCompile("org.springframework.boot:spring-boot-starter-test") {
    exclude(module = "junit")
  }
  testCompile("io.projectreactor:reactor-test") {
    exclude(module = "junit")
  }
  testCompile("org.assertj:assertj-core:3.9.0")
  testImplementation("org.junit.jupiter:junit-jupiter-api")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

