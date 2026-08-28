package de.esolutions.connectedradioclient.helper

import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException

object ModifierAccessHacker {
    @get:Throws(
        InvocationTargetException::class,
        IllegalAccessException::class,
        NoSuchMethodException::class
    )
    val modifier: Field
        /**
         * Note:
         * Needed:
         * tasks.withType(Test::class) {
         * jvmArgs(
         * "--add-opens", "java.base/java.lang=ALL-UNNAMED",
         * "--add-opens", "java.base/java.lang.reflect=ALL-UNNAMED"
         * )
         * }
         * Only working for Java 17 ;-)
         * @return
         * @throws InvocationTargetException
         * @throws IllegalAccessException
         * @throws NoSuchMethodException
         */
        get() {
            val getDeclaredFields0 = Class::class.java.getDeclaredMethod(
                "getDeclaredFields0",
                Boolean::class.javaPrimitiveType
            )
            getDeclaredFields0.isAccessible = true
            val fields = getDeclaredFields0.invoke(Field::class.java, false) as Array<Field>
            var modifiers: Field? = null
            for (each in fields) {
                if ("modifiers" == each.name) {
                    modifiers = each
                    break
                }
            }
            return modifiers!!
        }
}