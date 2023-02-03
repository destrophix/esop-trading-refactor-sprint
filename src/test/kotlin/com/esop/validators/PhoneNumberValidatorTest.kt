package com.esop.validators

import com.google.i18n.phonenumbers.PhoneNumberUtil
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream


class PhoneNumberValidatorTest {
    private val phoneUtil = PhoneNumberUtil.getInstance()

    companion object {
        @JvmStatic
        private fun validPhoneNumbers(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("+917550276216"),
                Arguments.of("+919840346538"),
                Arguments.of("+14844731220"),
                Arguments.of("+27 11 978 5313"),
                Arguments.of("+924278346517")
            )
        }


    }

    @ParameterizedTest
    @MethodSource("validPhoneNumbers")
    fun `it should return true for valid phoneNumber`(phoneNumber: String) {
        assertTrue(phoneUtil.isValidNumber(phoneUtil.parse(phoneNumber, null)))
    }


}
