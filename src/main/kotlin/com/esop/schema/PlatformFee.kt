package com.esop.schema

import com.esop.PlatformFeeLessThanZeroException
import java.math.BigInteger

class PlatformFee {

    companion object {

        var totalPlatformFee: BigInteger = BigInteger("0")

        fun addPlatformFee(fee: Long) {
            if (fee < 0)
                throw PlatformFeeLessThanZeroException()
            totalPlatformFee += fee.toBigInteger()
        }

        fun getPlatformFee(): BigInteger {
            return totalPlatformFee
        }

    }


}