package com.example.sdk.internal.common

import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Modifier

/** Utility methods for calling methods and accessing fields reflectively. */
object ReflectionUtils {
    /**
     * Reflectively get the value of a field.
     *
     * @param instance the target instance
     * @param fieldName the field name
     * @param R the return type
     * @return the value of the field on the instance
     */
    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun <R> getField(instance: Any, fieldName: String): R {
        try {
            return traverseClassHierarchy(
                instance.javaClass,
                NoSuchFieldException::class.java,
            ) { traversalClazz ->
                val field: Field = traversalClazz.getDeclaredField(fieldName)
                field.isAccessible = true
                field.get(instance) as R
            }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    /**
     * Reflectively set the value of a field.
     *
     * @param instance the target instance.
     * @param fieldName the field name.
     * @param fieldNewValue the new value of field.
     */
    fun setField(instance: Any, fieldName: String, fieldNewValue: Any) {
        try {
            traverseClassHierarchy(
                instance.javaClass,
                NoSuchFieldException::class.java,
            ) { traversalClazz ->
                val field = traversalClazz.getDeclaredField(fieldName)
                field.isAccessible = true
                field.set(instance, fieldNewValue)
                null
            }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    /**
     * Reflectively set the value of a field.
     *
     * @param type the target type.
     * @param instance the target instance.
     * @param fieldName the field name.
     * @param fieldNewValue the new value of field.
     */
    fun setField(
        type: Class<*>,
        instance: Any,
        fieldName: String,
        fieldNewValue: Any,
    ) {
        try {
            val field = type.getDeclaredField(fieldName)
            field.isAccessible = true
            field.set(instance, fieldNewValue)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    /**
     * Reflectively get the value of a static field.
     *
     * @param R the return type.
     * @param field the field instance.
     * @return the value of the field.
     */
    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun <R> getStaticField(field: Field): R {
        try {
            makeFieldVeryAccessible(field)
            return field.get(null) as R
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    /**
     * Reflectively get the value of a static field.
     *
     * @param R the return type.
     * @param clazz the target class.
     * @param fieldName the field name.
     * @return the value of the field.
     */
    @JvmStatic
    fun <R> getStaticField(clazz: Class<*>, fieldName: String): R {
        try {
            return getStaticField(clazz.getDeclaredField(fieldName))
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    /**
     * Reflectively set the value of a static field.
     *
     * @param field the field instance.
     * @param fieldNewValue the new value.
     */
    @JvmStatic
    fun setStaticField(field: Field, fieldNewValue: Any) {
        try {
            makeFieldVeryAccessible(field)
            field.set(null, fieldNewValue)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    /**
     * Reflectively set the value of a static field.
     *
     * @param clazz the target class.
     * @param fieldName the field name.
     * @param fieldNewValue the new value of field.
     */
    @JvmStatic
    fun setStaticField(clazz: Class<*>, fieldName: String, fieldNewValue: Any) {
        try {
            setStaticField(clazz.getDeclaredField(fieldName), fieldNewValue)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    /**
     * Reflectively call an instance method on an object.
     *
     * @param instance the target instance.
     * @param methodName the method name to call.
     * @param classParameters the array of parameter types and values.
     * @param R the return type
     * @return the return value of the method
     */
    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun <R> callInstanceMethod(
        instance: Any,
        methodName: String,
        vararg classParameters: ClassParameter<*>,
    ): R {
        try {
            val classes: Array<Class<*>?> = ClassParameter.getClasses(*classParameters)
            val values: Array<Any?> = ClassParameter.getValues(*classParameters)

            return traverseClassHierarchy(
                instance.javaClass,
                NoSuchMethodException::class.java,
            ) { traversalClazz ->
                val declaredMethod: Method = traversalClazz.getDeclaredMethod(methodName, *classes)
                declaredMethod.isAccessible = true
                declaredMethod.invoke(instance, *values) as R
            }
        } catch (e: InvocationTargetException) {
            if (e.targetException is RuntimeException) {
                throw e.targetException as RuntimeException
            }
            if (e.targetException is Error) {
                throw e.targetException as Error
            }
            throw RuntimeException(e.targetException)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    /**
     * Reflectively call an instance method on an instance on a specific class.
     *
     * @param R the return type.
     * @param clazz the class.
     * @param instance the target instance.
     * @param methodName the method name to call.
     * @param classParameters the array of parameter types and values.
     * @return the return value of the method.
     */
    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun <R> callInstanceMethod(
        clazz: Class<*>,
        instance: Any,
        methodName: String,
        vararg classParameters: ClassParameter<*>,
    ): R {
        try {
            val classes: Array<Class<*>?> = ClassParameter.getClasses(*classParameters)
            val values: Array<Any?> = ClassParameter.getValues(*classParameters)

            val declaredMethod: Method = clazz.getDeclaredMethod(methodName, *classes)
            declaredMethod.isAccessible = true
            if (Modifier.isStatic(declaredMethod.modifiers)) {
                throw IllegalArgumentException("$declaredMethod is static")
            }
            return declaredMethod.invoke(instance, *values) as R
        } catch (e: InvocationTargetException) {
            if (e.targetException is RuntimeException) {
                throw e.targetException as RuntimeException
            }
            if (e.targetException is Error) {
                throw e.targetException as Error
            }
            throw RuntimeException(e.targetException)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    /**
     * Helper method for calling a static method using a class.
     *
     * @param R the return type.
     * @param fullyQualifiedClassName the fully qualified class name.
     * @param methodName the method name to call.
     * @param classParameters the array of parameter types and values.
     * @return the return value of the method.
     */
    @JvmStatic
    fun <R> callStaticMethod(
        fullyQualifiedClassName: String,
        methodName: String,
        vararg classParameters: ClassParameter<*>,
    ): R {
        val clazz: Class<*> = loadClass(fullyQualifiedClassName)
        return callStaticMethod(clazz, methodName, *classParameters)
    }

    /**
     * Reflectively call a static method on a class.
     *
     * @param R the return type.
     * @param clazz the target class.
     * @param methodName the method name to call.
     * @param classParameters the array of parameter types and values.
     * @return the return value of the method.
     */
    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun <R> callStaticMethod(
        clazz: Class<*>,
        methodName: String,
        vararg classParameters: ClassParameter<*>,
    ): R {
        try {
            val classes: Array<Class<*>?> = ClassParameter.getClasses(*classParameters)
            val values: Array<Any?> = ClassParameter.getValues(*classParameters)

            val declaredMethod: Method = clazz.getDeclaredMethod(methodName, *classes)
            declaredMethod.isAccessible = true
            if (!Modifier.isStatic(declaredMethod.modifiers)) {
                throw IllegalArgumentException("$declaredMethod is not static")
            }
            return declaredMethod.invoke(null, *values) as R
        } catch (e: InvocationTargetException) {
            if (e.targetException is RuntimeException) {
                throw e.targetException as RuntimeException
            }
            if (e.targetException is Error) {
                throw e.targetException as Error
            }
            throw RuntimeException(e.targetException)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    /**
     * Try to load a class via reflection.
     *
     * @param fullyQualifiedClassName the full class name.
     * @return a Class if it's available, or throw Exception
     */
    @JvmStatic
    fun loadClass(fullyQualifiedClassName: String): Class<*> {
        return Class.forName(fullyQualifiedClassName)
    }

    /**
     * Try to load a class via reflection.
     *
     * @param T the type to cast this class object to.
     * @param fullyQualifiedClassName the full class name.
     * @param superClazz the class of the type to cast this class object to.
     * @return this Class object, cast to represent a subclass of the specified class object.
     */
    @JvmStatic
    fun <T> loadClass(fullyQualifiedClassName: String, superClazz: Class<T>): Class<out T> {
        return Class.forName(fullyQualifiedClassName).asSubclass(superClazz)
    }

    /**
     * Determines if a named class can be loaded or not.
     *
     * @param fullyQualifiedClassName the fully qualified name of the class.
     * @return true if class is available, false otherwise.
     */
    @JvmStatic
    fun isClassAvailable(fullyQualifiedClassName: String): Boolean {
        return runCatching { loadClass(fullyQualifiedClassName) }.getOrElse { null } != null
    }

    /**
     * Determines if a named Class can be loaded or not.
     *
     * @param fullyQualifiedClassName the fully qualified name of the class.
     * @param superClazz the class of the type to cast this class object to.
     * @return true if class is available, false otherwise.
     */
    @JvmStatic
    fun <T> isClassAvailable(fullyQualifiedClassName: String, superClazz: Class<T>): Boolean {
        return runCatching {
            loadClass(
                fullyQualifiedClassName,
                superClazz,
            )
        }.getOrElse { null } != null
    }

    @Throws(Exception::class)
    private fun <R, E : Exception> traverseClassHierarchy(
        clazz: Class<*>,
        exceptionClazz: Class<out E>,
        insideTraversal: InsideTraversal<R>,
    ): R {
        var hierarchyTraversalClazz: Class<*> = clazz
        while (true) {
            try {
                return insideTraversal.run(hierarchyTraversalClazz)
            } catch (e: Exception) {
                if (!exceptionClazz.isInstance(e)) {
                    throw e
                }
                hierarchyTraversalClazz = hierarchyTraversalClazz.superclass ?: run {
                    throw RuntimeException(e)
                }
            }
        }
    }

    private fun interface InsideTraversal<R> {
        @Throws(Exception::class)
        fun run(traversalClazz: Class<*>): R
    }

    private fun makeFieldVeryAccessible(field: Field) {
        field.isAccessible = true
        // remove 'final' modifier if present
        if (field.modifiers and Modifier.FINAL == Modifier.FINAL) {
            try {
                val modifiersField = Field::class.java.getDeclaredField("modifiers")
                modifiersField.isAccessible = true
                try {
                    modifiersField.setInt(field, field.modifiers and Modifier.FINAL.inv())
                } catch (e: IllegalAccessException) {
                    throw AssertionError(e)
                }
            } catch (e: NoSuchFieldException) {
                // ignore missing fields
            }
        }
    }

    /**
     * Typed parameter used with reflective method calls.
     *
     * @constructor
     * Constructs a new [ClassParameter] instance.
     *
     * @param V the value of the method parameter.
     * @property clazz the class.
     * @property value the value.
     */
    class ClassParameter<V>(val clazz: Class<out V>, val value: V) {
        companion object {
            @JvmStatic
            fun <V> from(clazz: Class<out V>, value: V): ClassParameter<V> {
                return ClassParameter(clazz, value)
            }

            @JvmStatic
            fun getClasses(vararg classParameters: ClassParameter<*>): Array<Class<*>?> {
                val classes: Array<Class<*>?> = arrayOfNulls(classParameters.size)
                for (i in classParameters.indices) {
                    val paramClass: Class<*> = classParameters[i].clazz
                    classes[i] = paramClass
                }
                return classes
            }

            @JvmStatic
            fun getValues(vararg classParameters: ClassParameter<*>): Array<Any?> {
                val values: Array<Any?> = arrayOfNulls(classParameters.size)
                for (i in classParameters.indices) {
                    val paramValue = classParameters[i].value
                    values[i] = paramValue
                }
                return values
            }
        }
    }
}
