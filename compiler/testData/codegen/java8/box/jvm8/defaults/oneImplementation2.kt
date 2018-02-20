// !API_VERSION: 1.3
// JVM_TARGET: 1.8
// WITH_RUNTIME

interface KCallable {
    @kotlin.annotations.JvmDefault
    val returnType: String
}

interface KCallableImpl : KCallable {
    @kotlin.annotations.JvmDefault
    override val returnType: String
        get() = "OK"
}

interface KProperty : KCallable
interface KPropertyImpl : KProperty, KCallableImpl
interface KMutableProperty : KProperty
interface KProperty1 : KProperty
interface KMutableProperty1 : KProperty1, KMutableProperty
interface KMutablePropertyImpl : KPropertyImpl

open class DescriptorBasedProperty : KCallableImpl
open class KProperty1Impl : DescriptorBasedProperty(), KProperty1, KPropertyImpl
open class KMutableProperty1Impl : KProperty1Impl(), KMutableProperty1, KMutablePropertyImpl

fun box(): String {
    return KMutableProperty1Impl().returnType
}