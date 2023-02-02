package com.esop

class Validator {
    fun validate(email :String): Boolean {
        val p = Regex("^[a-z0-9-_]+[\"]")
        if(p.containsMatchIn(email)){
            return false
        }
        val EMAIL_REGEX = "^(?=[\"\'a-zA-Z0-9][\"\\s\'a-zA-Z0-9@._%+-]{5,253}\$)[\\s\"\'a-zA-Z0-9._%+-]{1,64}@(?:(?=[a-zA-Z0-9-]{1,63}\\.)[a-zA-Z0-9]+(?:-[a-zA-Z0-9]+)*\\.){1,8}[a-z0-9A-Z]{2,63}\$"
        val pattern = Regex(EMAIL_REGEX)
        return pattern.containsMatchIn(email)
    }
    fun checkIfEmailIsValid(email :String): Boolean {
        return validate(email)
    }
}