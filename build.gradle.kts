plugins {
  id("java")
  id("org.springframework.boot") version "4.0.5"
  id("io.freefair.lombok") version "9.2.0"
}

group = "com.quickpick.app"
version = "1.0.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_25
java.targetCompatibility = JavaVersion.VERSION_25

repositories {
  mavenCentral()
}

dependencies {
  testImplementation(platform("org.junit:junit-bom:6.0.3"))
  testImplementation("org.junit.jupiter:junit-jupiter:6.0.3")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher:6.0.3")

  implementation("com.google.guava:guava:33.6.0-jre")

  implementation("org.projectlombok:lombok:1.18.44")
  annotationProcessor("org.projectlombok:lombok:1.18.44")
  testImplementation("org.projectlombok:lombok:1.18.44")
  testAnnotationProcessor("org.projectlombok:lombok:1.18.44")

  implementation("org.json:json:20251224")
  implementation("commons-io:commons-io:2.21.0")

  implementation("org.apache.commons:commons-configuration2:2.14.0")
  implementation("commons-beanutils:commons-beanutils:1.11.0")

  implementation("org.postgresql:postgresql:42.7.10")
  implementation("org.hibernate.orm:hibernate-core:7.3.1.Final")
  implementation("org.reflections:reflections:0.10.2")

  implementation("org.springframework.boot:spring-boot-starter-web:4.0.5")
  implementation("org.springframework:spring-core:7.0.7")
  implementation("org.springframework.data:spring-data-jpa:4.0.5")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa:4.0.5")
  implementation("com.h2database:h2:2.4.240")

  implementation("de.mkammerer:argon2-jvm:2.12")

  implementation("io.jsonwebtoken:jjwt:0.13.0")

  implementation("com.nimbusds:nimbus-jose-jwt:10.9")

  implementation("dev.samstevens.totp:totp:1.7.1")

  compileOnly("com.maxmind.geoip2:geoip2:5.0.2")

  implementation("com.sun.mail:javax.mail:1.6.2")

  implementation("com.googlecode.owasp-java-html-sanitizer:owasp-java-html-sanitizer:20260313.1")

  implementation("com.google.api-client:google-api-client:2.9.0")
}

tasks.test {
  useJUnitPlatform()
}

tasks.bootJar {
  mainClass = "com.quickpick.app.core.CoreApplication"
}