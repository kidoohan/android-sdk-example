package com.example.sdk.internal.common

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import java.lang.IllegalStateException

/** Tests for [ReflectionUtils]  */
class ReflectionUtilsTest {
    @Test
    fun `test getField gets private fields`() {
        val example = ExampleDescendant()
        assertThat(ReflectionUtils.getField<Int>(example, "overridden")).isEqualTo(20)
    }

    @Test
    fun `test getField gets inherited fields`() {
        val example = ExampleDescendant().apply {
            setNotOverridden(100)
        }
        assertThat(ReflectionUtils.getField<Int>(example, "notOverridden")).isEqualTo(100)
    }

    @Test
    fun `test getField gives exception`() {
        // given
        val example = ExampleDescendant()

        // when
        val throwable = assertThrows(RuntimeException::class.java) {
            ReflectionUtils.getField<Int>(example, "nonExistent")
        }

        // then
        assertThat(throwable.message).contains("nonExistent")
    }

    @Test
    fun `tet setField sets private fields`() {
        val example = ExampleDescendant()
        ReflectionUtils.setField(example, "overridden", 100)
        assertThat(example.getOverriddenValue()).isEqualTo(100)
    }

    @Test
    fun `tet setField with type and sets private fields`() {
        val example = ExampleDescendant()
        ReflectionUtils.setField(ExampleDescendant::class.java, example, "overridden", 100)
        assertThat(example.getOverriddenValue()).isEqualTo(100)
    }

    @Test
    fun `test setField sets inherited fields`() {
        val example = ExampleDescendant().apply {
            setNotOverridden(200)
        }
        ReflectionUtils.setField(example, "notOverridden", 300)
        assertThat(example.getNotOverridden()).isEqualTo(300)
    }

    @Test
    fun `test setField with type and sets inherited fields`() {
        val example = ExampleDescendant().apply {
            setNotOverridden(200)
        }
        ReflectionUtils.setField(ExampleBase::class.java, example, "notOverridden", 300)
        assertThat(example.getNotOverridden()).isEqualTo(300)
    }

    @Test
    fun `test setField gives exception`() {
        // given
        val example = ExampleDescendant()

        // when
        val throwable = assertThrows(RuntimeException::class.java) {
            ReflectionUtils.setField(example, "nonExistent", 1)
        }

        // then
        assertThat(throwable.message).contains("nonExistent")
    }

    @Test
    fun `test setField with type gives exception`() {
        // given
        val example = ExampleDescendant()

        // when
        val throwable = assertThrows(RuntimeException::class.java) {
            ReflectionUtils.setField(ExampleDescendant::class.java, example, "nonExistent", 1)
        }

        // then
        assertThat(throwable.message).contains("nonExistent")
    }

    @Test
    fun `test getStaticField with field gets static fields`() {
        // given
        val field = ExampleDescendant::class.java.getDeclaredField("DESCENDANT")

        // when
        val result = ReflectionUtils.getStaticField<Int>(field)

        // then
        assertThat(result).isEqualTo(6)
    }

    @Test
    fun `test getStaticField with fieldName gets static field`() {
        // when
        val result =
            ReflectionUtils.getStaticField<Int>(ExampleDescendant::class.java, "DESCENDANT")

        // then
        assertThat(result).isEqualTo(6)
    }

    @Test
    fun `test getStaticField with fieldName gives exception`() {
        // when
        val throwable = assertThrows(RuntimeException::class.java) {
            ReflectionUtils.getStaticField<Int>(ExampleDescendant::class.java, "NON_EXISTENT")
        }

        // then
        assertThat(throwable.message).contains("NON_EXISTENT")
    }

    @Test
    fun `test getFinalStaticField with field gets static field`() {
        // given
        val field = ExampleBase::class.java.getDeclaredField("BASE")

        // when
        val result = ReflectionUtils.getStaticField<Int>(field)

        // then
        assertThat(result).isEqualTo(8)
    }

    @Test
    fun `test getFinalStaticField with fieldName gets static field`() {
        // when
        val result = ReflectionUtils.getStaticField<Int>(ExampleBase::class.java, "BASE")

        // then
        assertThat(result).isEqualTo(8)
    }

    @Test
    fun `test setStaticField with field sets static fields`() {
        // given
        val field = ExampleDescendant::class.java.getDeclaredField("DESCENDANT")
        val originalValue = ReflectionUtils.getStaticField<Int>(field)

        // when
        ReflectionUtils.setStaticField(field, 7)

        // then
        assertThat(originalValue).isEqualTo(6)
        assertThat(ExampleDescendant.DESCENDANT).isEqualTo(7)
        ReflectionUtils.setStaticField(field, originalValue)
    }

    @Test
    fun `test setStaticField with fieldName sets static fields`() {
        // given
        val originalValue =
            ReflectionUtils.getStaticField<Int>(ExampleDescendant::class.java, "DESCENDANT")

        // when
        ReflectionUtils.setStaticField(ExampleDescendant::class.java, "DESCENDANT", 7)

        // then
        assertThat(originalValue).isEqualTo(6)
        assertThat(ExampleDescendant.DESCENDANT).isEqualTo(7)
        ReflectionUtils.setStaticField(ExampleDescendant::class.java, "DESCENDANT", originalValue)
    }

    @Test
    fun `test setStaticField with fieldName gives exception`() {
        // when
        val throwable = assertThrows(RuntimeException::class.java) {
            ReflectionUtils.setStaticField(ExampleDescendant::class.java, "NON_EXISTENT", 7)
        }

        // then
        assertThat(throwable.message).contains("NON_EXISTENT")
    }

    @Test
    fun `test setFinalStaticField with fieldName sets static fields`() {
        // given
        val originalValue =
            ReflectionUtils.getStaticField<Int>(ExampleWithFinalStatic::class.java, "FIELD")

        // when
        ReflectionUtils.setStaticField(ExampleWithFinalStatic::class.java, "FIELD", 101)

        // then
        assertThat(
            ReflectionUtils.getStaticField<Int>(
                ExampleWithFinalStatic::class.java,
                "FIELD",
            ),
        ).isEqualTo(101)
        ReflectionUtils.setStaticField(ExampleWithFinalStatic::class.java, "FIELD", originalValue)
    }

    @Test
    fun `test callInstanceMethod calls private methods`() {
        // given
        val example = ExampleDescendant()

        // when
        val result = ReflectionUtils.callInstanceMethod<Int>(example, "returnNumber")

        // then
        assertThat(result).isEqualTo(1337)
    }

    @Test
    fun `test callInstanceMethod when multiple signatures exist for a methodName calls method with correct signature`() {
        assertThat(
            ReflectionUtils.callInstanceMethod<Int>(
                ExampleDescendant(),
                "returnNumber",
                ReflectionUtils.ClassParameter.from(Int::class.java, 5),
            ),
        ).isEqualTo(5)

        assertThat(
            ReflectionUtils.callInstanceMethod<Int>(
                ExampleDescendant::class.java,
                ExampleDescendant(),
                "returnNumber",
                ReflectionUtils.ClassParameter.from(Int::class.java, 5),
            ),
        ).isEqualTo(5)
    }

    @Test
    fun `test callInstanceMethod calls inherited methods`() {
        // given
        val example = ExampleDescendant()

        // when
        val result = ReflectionUtils.callInstanceMethod<Int>(
            example,
            "returnNegativeNumber",
        )

        // then
        assertThat(result).isEqualTo(-46)
    }

    @Test
    fun `test callInstanceMethod gives exception`() {
        // given
        val example = ExampleDescendant()

        // when
        val throwable = assertThrows(RuntimeException::class.java) {
            ReflectionUtils.callInstanceMethod<Int>(example, "nonExistent")
        }

        // then
        assertThat(throwable.message).contains("nonExistent")
    }

    @Test
    fun `test callInstanceMethod rethrows unchecked exception`() {
        // given
        val example = ExampleDescendant()

        // when
        assertThrows(IllegalStateException::class.java) {
            ReflectionUtils.callInstanceMethod<Unit>(example, "throwUncheckedException")
        }
    }

    @Test
    fun `test callInstanceMethod rethrows checked exception`() {
        // given
        val example = ExampleDescendant()

        // when
        val throwable = assertThrows(RuntimeException::class.java) {
            ReflectionUtils.callInstanceMethod<Unit>(example, "throwCheckedException")
        }

        // then
        assertThat(throwable.cause).isInstanceOf(IllegalAccessException::class.java)
    }

    @Test
    fun `test callInstanceMethod rethrows error`() {
        // given
        val example = ExampleDescendant()

        // when
        assertThrows(OutOfMemoryError::class.java) {
            ReflectionUtils.callInstanceMethod<Unit>(example, "throwError")
        }
    }

    @Test
    fun `test callInstanceMethod calls private kotlin object method`() {
        // when
        val result = ReflectionUtils.callInstanceMethod<Int>(
            ExampleDescendant.Companion,
            "getConstantNumber",
        )

        // then
        assertThat(result).isEqualTo(1)
    }

    @Test
    fun `test callInstanceMethod calls private kotlin object method rethrows unchecked exception`() {
        assertThrows(IllegalStateException::class.java) {
            ReflectionUtils.callInstanceMethod(
                ExampleDescendant.Companion,
                "throwUncheckedException",
            )
        }

        assertThrows(IllegalStateException::class.java) {
            ReflectionUtils.callInstanceMethod(
                ExampleDescendant.Companion::class.java,
                ExampleDescendant.Companion,
                "throwUncheckedException",
            )
        }
    }

    @Test
    fun `test callInstanceMethod calls private kotlin object method rethrows checked exception`() {
        run {
            // when
            val throwable = assertThrows(RuntimeException::class.java) {
                ReflectionUtils.callInstanceMethod(
                    ExampleDescendant.Companion,
                    "throwCheckedException",
                )
            }

            // then
            assertThat(throwable.cause).isInstanceOf(IllegalAccessException::class.java)
        }

        run {
            // when
            val throwable = assertThrows(RuntimeException::class.java) {
                ReflectionUtils.callInstanceMethod(
                    ExampleDescendant.Companion::class.java,
                    ExampleDescendant.Companion,
                    "throwCheckedException",
                )
            }

            // then
            assertThat(throwable.cause).isInstanceOf(IllegalAccessException::class.java)
        }
    }

    @Test
    fun `test callInstanceMethod calls private kotlin object method rethrows error`() {
        assertThrows(OutOfMemoryError::class.java) {
            ReflectionUtils.callInstanceMethod(ExampleDescendant.Companion, "throwError")
        }

        assertThrows(OutOfMemoryError::class.java) {
            ReflectionUtils.callInstanceMethod(
                ExampleDescendant.Companion::class.java,
                ExampleDescendant.Companion,
                "throwError",
            )
        }
    }

    @Test
    fun `test callInstanceMethod calls static method gives exception`() {
        // given
        val example = ExampleJavaClass()

        // when
        val throwable = assertThrows(RuntimeException::class.java) {
            ReflectionUtils.callInstanceMethod<Int>(
                ExampleJavaClass::class.java,
                example,
                "getConstantNumber",
            )
        }

        // then
        assertThat(throwable.cause).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(throwable.message)
            .contains("ExampleJavaClass.getConstantNumber() is static")
    }

    @Test
    fun `test callStaticMethod private method`() {
        // when
        val result = ReflectionUtils.callStaticMethod<Int>(
            ExampleJavaClass::class.java,
            "getConstantNumber",
        )

        // then
        assertThat(result).isEqualTo(1)
    }

    @Test
    fun `test callStaticMethod rethrows unchecked exception`() {
        assertThrows(IllegalStateException::class.java) {
            ReflectionUtils.callStaticMethod(
                ExampleJavaClass::class.java,
                "staticThrowUncheckedException",
            )
        }
    }

    @Test
    fun `test callStaticMethod rethrows checked exception`() {
        // when
        val throwable = assertThrows(RuntimeException::class.java) {
            ReflectionUtils.callStaticMethod(
                ExampleJavaClass::class.java,
                "staticThrowCheckedException",
            )
        }

        // then
        assertThat(throwable.cause).isInstanceOf(IllegalAccessException::class.java)
    }

    @Test
    fun `test callStaticMethod rethrows error`() {
        assertThrows(OutOfMemoryError::class.java) {
            ReflectionUtils.callStaticMethod(ExampleJavaClass::class.java, "staticThrowError")
        }
    }

    @Test
    fun `test callStaticMethod with not static method throw exception`() {
        val throwable = assertThrows(RuntimeException::class.java) {
            ReflectionUtils.callStaticMethod(
                ExampleJavaClass::class.java,
                "getNegativeNumber",
            )
        }

        assertThat(throwable.cause).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(throwable.cause!!.message).contains("getNegativeNumber() is not static")
    }

    @Test
    fun `test isClassAvailable`() {
        assertThat(ReflectionUtils.isClassAvailable(ExampleBase::class.java.name)).isTrue()
        assertThat(ReflectionUtils.isClassAvailable(ExampleDescendant::class.java.name)).isTrue()
        assertThat(
            ReflectionUtils.isClassAvailable(
                ExampleBase::class.java.name,
                ExampleDescendant::class.java,
            ),
        ).isFalse()
        assertThat(
            ReflectionUtils.isClassAvailable(
                ExampleDescendant::class.java.name,
                ExampleBase::class.java,
            ),
        ).isTrue()
    }

    private open class ExampleBase {
        private var notOverridden: Int = 0
        protected open var overridden: Int = 10

        fun getNotOverridden(): Int {
            return notOverridden
        }

        fun setNotOverridden(notOverridden: Int) {
            this.notOverridden = notOverridden
        }

        fun getOverriddenValue(): Int {
            return overridden
        }

        private fun returnNegativeNumber(): Int {
            return -46
        }

        companion object {
            private const val BASE = 8
        }
    }

    private class ExampleDescendant : ExampleBase() {
        override var overridden: Int = 20

        private fun returnNumber(): Int {
            return 1337
        }

        private fun returnNumber(n: Int): Int {
            return n
        }

        private fun throwUncheckedException() {
            throw IllegalStateException()
        }

        @Throws(IllegalAccessException::class)
        private fun throwCheckedException() {
            throw IllegalAccessException()
        }

        private fun throwError() {
            throw OutOfMemoryError()
        }

        companion object {
            var DESCENDANT = 6

            private fun getConstantNumber(): Int {
                return 1
            }

            private fun throwUncheckedException() {
                throw IllegalStateException()
            }

            @Throws(IllegalAccessException::class)
            private fun throwCheckedException() {
                throw IllegalAccessException()
            }

            private fun throwError() {
                throw OutOfMemoryError()
            }
        }
    }

    private class ExampleWithFinalStatic {
        companion object {
            private const val FIELD = 100
        }
    }
}
