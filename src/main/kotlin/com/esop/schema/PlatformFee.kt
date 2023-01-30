package com.esop.schema

import com.esop.PlatformFeeLessThanZeroException
import java.math.BigInteger

class PlatformFee{

    companion object {

        var totalPlatformFee: BigInteger = BigInteger("0")

        fun addPlatformFee(fee: Long){
            if(fee < 0)
                throw PlatformFeeLessThanZeroException()
            totalPlatformFee += fee.toBigInteger()
        }

<<<<<<< HEAD
        fun getPlatformFee(): BigInteger {
            return totalPlatformFee
=======
            var feeToBeAdd: String = fee.toString()

            var result:String = ""
            platFee.reversed()
            feeToBeAdd.reversed()

            var len:Int = maxOf(platFee.length,feeToBeAdd.length)
            var carry:Int = 0

            for (i in (0..len-1)){
                var sum: Int = carry
                if(i< platFee.length && i<feeToBeAdd.length)
                    sum += (platFee[i]-48).toInt()+(feeToBeAdd[i]-48).toInt()
                else if(i< platFee.length)
                    sum += (platFee[i]-48).toInt()
                else
                    sum += (feeToBeAdd[i]-48).toInt()
                carry = sum/10
                result += (sum%10).toString()
            }
            if(carry>0)
                result += carry.toString()
            result.reversed()
            platFee = result
>>>>>>> 7efcd85 (Add unit testcase)
        }

    }


}