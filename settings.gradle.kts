rootProject.name = "DonateCase"
include("common", "api", ":api:spigot-api", "velocity", "spigot")

file("addons").listFiles()?.forEach { dir ->
    if (dir.isDirectory) {
        include("addons:${dir.name}")
    }
}