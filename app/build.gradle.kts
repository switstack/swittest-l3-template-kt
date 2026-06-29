import com.android.build.api.variant.BuildConfigField
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.detekt)
}

val appVersionName = "1.0.0"
fun generateVersionCode(versionName: String): Int {
    val (major, minor, patch) = versionName.split('.').map { it.toInt() }
    return major * 1_000_000 + minor * 1_000 + patch
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) load(file.inputStream())
}

android {
    namespace = "io.switstack.switcloud.swittestl3"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "io.switstack.switcloud.swittestl3.template"
        minSdk = 28
        targetSdk = 36
        versionName = appVersionName
        versionCode = generateVersionCode(appVersionName)

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("int", "MIN_SDK_VERSION", "$minSdk")
        buildConfigField("String", "SWITSTACK_CLIENT_ATTESTATION_SECRET", localProperties.getProperty("SWITSTACK_CLIENT_ATTESTATION_SECRET") ?: "\"\"")
        buildConfigField("String", "SWITTEST_URL", localProperties.getProperty("LOCAL_SWITTEST_URL") ?: "\"\"")
        buildConfigField("String", "POI_ID", localProperties.getProperty("LOCAL_POI_ID") ?: "\"\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
            localProperties.getProperty("RELEASE_SWITTEST_URL")?.let {
                buildConfigField("String", "SWITTEST_URL", it)
            }
            localProperties.getProperty("RELEASE_POI_ID")?.let {
                buildConfigField("String", "POI_ID", it)
            }
        }
        debug { }
    }

    flavorDimensions += "l2"

    productFlavors {
        create("mokastd") {
            dimension = "l2"
            missingDimensionStrategy("hal", "std")
        }

        create("mokavepl") {
            dimension = "l2"
            missingDimensionStrategy("hal", "vepl")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

}

androidComponents {
    onVariants { variant ->
        val dependencyName = "switcloud-l2"
        val capitalName = variant.name.replaceFirstChar { it.uppercase() }

        val extractTask = tasks.register<ExtractVersionTask>("extract${capitalName}TransitiveVersion") {
            runtimeClasspath.setFrom(variant.runtimeConfiguration)
            targetDependencyName.set("$dependencyName-kt")
            outputFile.set(layout.buildDirectory.file("intermediates/dep_version/${variant.name}/ver.txt"))
        }

        variant.buildConfigFields?.put(
            "SWITCLOUD_L2_VERSION",
            extractTask.flatMap { task ->
                task.outputFile.map { file ->
                    val version = file.asFile.readText().trim()
                    BuildConfigField("String", "\"$dependencyName-$version-${variant.flavorName}\"", "Dependency Version")
                }
            }
        )
    }
}

base {
    archivesName.set("${rootProject.name}-$appVersionName")
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.datastore.preferences)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose.core)
    implementation(libs.androidx.activity)

    implementation(libs.okhttp)
    implementation(libs.okhttp.interceptor)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.timber)
    implementation(libs.koin.core)
    implementation(libs.koin.androidx.compose)

    // Switcloud deps
    implementation(libs.switcloud.clt)
    implementation(libs.switcloud.l2)

    // TLV parser / builder
    implementation(libs.tlv)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

detekt {
    autoCorrect = true
    toolVersion = libs.versions.detekt.get()
    source.setFrom("src/main/java/")
    config.setFrom("../conf/detekt/detekt.yml")
    buildUponDefaultConfig = true
    basePath = projectDir.absolutePath
    debug = false
}

dependencies {
    detektPlugins(libs.bundles.detekt)
}

abstract class ExtractVersionTask : DefaultTask() {
    @get:InputFiles
    abstract val runtimeClasspath: ConfigurableFileCollection

    @get:Input
    abstract val targetDependencyName: Property<String>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun execute() {
        val target = runtimeClasspath.files.mapNotNull { file ->
            file.name
        }.find { it.contains(targetDependencyName.get()) }

        val versionRegex = Regex("""(\d+\.\d+\.\d+)""")
        val version = target?.let { versionRegex.find(target)?.value } ?: "unknown"
        outputFile.get().asFile.writeText(version)
    }
}