package com.esop.validators

import com.esop.CustomConstraintFactory
import com.esop.repository.UserRecords
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class EmailValidatorTest {
    private val userRecords = UserRecords()
    private val emailValidator = CustomConstraintFactory(userRecords)

    companion object {
        @JvmStatic
        private fun validEmails(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("simple@example.com", "very.common@example.com"),
                Arguments.of("disposable.style.email.with+symbol@example.com"),
                Arguments.of("other.email-with-hyphen@example.com"),
                Arguments.of("fully-qualified-domain@example.com"),
                Arguments.of("user.name+tag+sorting@example.com"),
                Arguments.of("fully-qualified-domain@example.com"),
                Arguments.of("x@example.com"),
                Arguments.of("carlosd'intino@arnet.com.ar"),
                Arguments.of("example-indeed@strange-example.com"),
                Arguments.of("example@s.example"),
                Arguments.of("\" \"@example.org"),
                Arguments.of("\"john..doe\"@example.org"),
                Arguments.of("sankar@a2345678901234567890123456789012345678901234567890123456789012b.com"),
                Arguments.of("sankar@gmail.a2345678901234567890123456789012345678901234567890123456789012b.a2345678901234567890123456789012345678901234567890123456789012b.a2345678901234567890123456789012345678901234567890123456789012b.a23456789012345678901234567890123456789012345678b"),
                Arguments.of("mkyong__100@yahoo-test.com")
            )
        }

        @JvmStatic
        private fun invalidEmails(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("abc.def@mail.c"),
                Arguments.of("abc.def@mail#archive.com"),
                Arguments.of("Abc.example.com"),
                Arguments.of("A@b@c@example.com"),
                Arguments.of("a\"b(c)d,e:f;g<h>i[j\\k]l@example.com"),
                Arguments.of("just\"not\"right@example.com"),
                Arguments.of("this is\"not\\allowed@example.com"),
                Arguments.of("this\\ still\"not\\allowed@ex__ample.com"),
                Arguments.of("1234567890123456789012345678901234567890123456789012345678901234+x@example.com"),
                Arguments.of("john.doe@example..com")
            )
        }

    }

    @ParameterizedTest
    @MethodSource("validEmails")
    fun `it should return true for valid emails`(email: String) {
        //Assert
        assertTrue(emailValidator.validate(email))
    }

    @ParameterizedTest
    @MethodSource("invalidEmails")
    fun `it should return false for invalid email`(email: String) {
        //Assert
        assertFalse(emailValidator.validate(email))
    }

}