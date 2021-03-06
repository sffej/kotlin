
import org.jetbrains.kotlin.gradle.dsl.Coroutines

plugins {
    kotlin("jvm")
}

jvmTarget = "1.6"

dependencies {
    compile(project(":kotlin-script-runtime"))
    compile(project(":kotlin-stdlib"))
    compile(project(":kotlin-scripting-common"))
    compile(project(":kotlin-scripting-jvm"))
    compile(project(":kotlin-script-util"))
    compileOnly(project(":compiler:cli"))
    compileOnly(intellijCoreDep())
}

sourceSets {
    "main" { projectDefault() }
    "test" {}
}

kotlin.experimental.coroutines = Coroutines.ENABLE

standardPublicJars()

publish()

