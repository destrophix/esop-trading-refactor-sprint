package com.esop.schema

class PlatformFee{

    companion object {
        var platFee:String = "0"

        fun addPlatformFee(fee: Long){

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
        }

    }


}