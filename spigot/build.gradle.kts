repositories {
    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/public/") }
}

dependencies {
    implementation(project(":common"))
    compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
}
