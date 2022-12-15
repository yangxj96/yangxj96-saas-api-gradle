dependencies {

    compileOnly("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.springframework.boot:spring-boot-starter-validation")
    compileOnly("com.baomidou:mybatis-plus-boot-starter:${property("mybatisPlusVersion")}")

    // 工具 begin
    api("org.apache.commons:commons-lang3:3.12.0")
    api("org.apache.commons:commons-collections4:4.4")
    api("org.apache.commons:commons-pool2:2.11.1")
    api("cn.hutool:hutool-all:5.8.10")

    // 工具 end
}