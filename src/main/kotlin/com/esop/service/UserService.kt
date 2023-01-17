package com.esop.service

import com.esop.schema.Order
import com.esop.schema.User
import io.micronaut.json.tree.JsonNode
import io.micronaut.json.tree.JsonObject
import io.micronaut.validation.validator.constraints.EmailValidator
import java.util.regex.Pattern
import com.esop.constant.errors
import com.esop.constant.success_response

val all_emails= mutableSetOf<String>()
val all_numbers= mutableSetOf<String>()
val all_usernames= mutableSetOf<String>()


var all_users = HashMap<String, User>()


fun check_username(username_set: MutableSet<String>, search_value: String) : Boolean
{
    return username_set.contains(search_value);
}

fun check_phonenumber(usernumber_set: MutableSet<String>,search_value:String) : Boolean
{
    return usernumber_set.contains(search_value);
}

fun check_email(useremail_set: MutableSet<String>, search_value: String) : Boolean
{
    return useremail_set.contains(search_value);
}



const val PHONENUMBER_REGEX = "^(\\+91[\\-\\s]?)?[0]?(91)?[789]\\d{9}\$"
//var PATTERN: Pattern = Pattern.compile(REG)
//fun CharSequence.isPhoneNumber() : Boolean = PATTERN.matcher(this).find()

val EMAIL_REGEX = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})";

fun isEmailValid(email: String): Boolean {
    return EMAIL_REGEX.toRegex().matches(email);
}

fun isPhoneNumber(pNumber: String): Boolean {
    return PHONENUMBER_REGEX.toRegex().matches(pNumber);
}

fun registerUser(userData: JsonObject): String {
    var v1 = userData.get("firstName").toString()
    var v2 = userData.get("lastName").toString()
    var v3 = userData.get("phoneNumber").toString()
    var v4 = userData.get("email").toString()
    var v5 = userData.get("username").toString()


    if(check_username(all_usernames, v5)){
        return errors["USERNAME_EXISTS"].toString()
    }
    else if(check_email(all_emails, v4)){
        return errors["EMAIL_EXISTS"].toString()
    }
    else if(check_phonenumber(all_numbers, v3)){
        return errors["PHONENUMBER_EXISTS"].toString()
    }
//    else if(!isEmailValid(v4)){
//        return errors["INVALID_EMAIL"].toString()
//    }
//    else if(!isPhoneNumber(v3)){
//        return errors["INVALID_PHONENUMBER"].toString()
//    }
    else{
        val user= User(v1,v2,v3,v4,v5);

        all_users[v5] = user
        all_emails.add(v4)
        all_numbers.add(v3)
        all_usernames.add(v5)
    }
    return success_response["USER_CREATED"].toString()
}

