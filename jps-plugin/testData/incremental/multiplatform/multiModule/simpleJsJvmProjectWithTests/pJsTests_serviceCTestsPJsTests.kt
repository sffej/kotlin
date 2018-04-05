actual fun cTests_platformDependent(): String = "pJsTests"
fun cTests_platformOnly() = "pJsTests"

fun test() {
    cMain_platformDependent()
    cMain_platformIndependent()
    cMain_platformOnly()

    cTests_platformIndependent()
    cTests_platformDependent()
    cTests_platformOnly()
}