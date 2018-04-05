actual fun cTests_platformDependent(): String = "pJvmTests"
fun cTests_platformOnly() = "pJvmTests"

fun test() {
    cMain_platformDependent()
    cMain_platformIndependent()
    cMain_platformOnly()

    cTests_platformIndependent()
    cTests_platformDependent()
    cTests_platformOnly()
}