package com.esop.service

import com.esop.schema.Order
import com.esop.schema.User
import io.micronaut.json.tree.JsonNode
import io.micronaut.json.tree.JsonObject
import io.micronaut.validation.validator.constraints.EmailValidator
import java.util.regex.Pattern
import com.esop.constant.errors
import com.esop.constant.success_response
import jakarta.inject.Singleton
import javax.validation.constraints.Null
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType
import kotlin.reflect.jvm.internal.impl.resolve.constants.NullValue

@Singleton
class UserService {
    val all_emails = mutableSetOf<String>()
    val all_numbers = mutableSetOf<String>()
    val all_usernames = mutableSetOf<String>()
    var all_users = HashMap<String, User>()

    fun check_inventory(quantity: Long, userName: String): Boolean{
        if(user_exists(userName) && all_users[userName]?.inventory?.free!! >= quantity){
            return true
        }
        return false
    }
    fun check_wallet(amount: Long, userName: String): Boolean{
        if(user_exists(userName) && all_users[userName]?.wallet?.free!! >= amount){
            return true
        }
        return false
    }
    fun orderCheckBeforePlace(userName: String, quantity: Long, type: String, price: Long): MutableList<String>{
        var errors = mutableListOf<String>()
        if(!all_users.containsKey(userName)){
            errors.add("User doesn't exist")
            return errors
        }
        if(type == "BUY"){
            if(!check_wallet(price, userName)){
                errors.add("Insufficient funds")
            }
        }
        else if(type == "SELL"){
            if(!check_inventory(quantity, userName)){
                errors.add("Insufficient inventory")
            }
        }
        return errors
    }
    fun user_exists(userName: String): Boolean{
        return all_users.containsKey(userName)
    }
    fun check_username(username_set: MutableSet<String>, search_value: String): Boolean {
        return username_set.contains(search_value);
    }

    fun check_phonenumber(usernumber_set: MutableSet<String>, search_value: String): Boolean {
        return usernumber_set.contains(search_value);
    }

    fun check_email(useremail_set: MutableSet<String>, search_value: String): Boolean {
        return useremail_set.contains(search_value);
    }


    val PHONENUMBER_REGEX = "^(\\+91[\\-\\s]?)?[0]?(91)?[789]\\d{9}\$"
//var PATTERN: Pattern = Pattern.compile(REG)
//fun CharSequence.isPhoneNumber() : Boolean = PATTERN.matcher(this).find()

    val EMAIL_REGEX = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})";

    fun isEmailValid(email: String): Boolean {
        return EMAIL_REGEX.toRegex().matches(email);
    }

    fun isPhoneNumber(pNumber: String): Boolean {
        return PHONENUMBER_REGEX.toRegex().matches(pNumber);
    }

    fun registerUser(userData: JsonObject): Map<String,Any> {
    var v1 = userData.get("firstName").stringValue
    var v2 = userData.get("lastName").stringValue
    var v3 = userData.get("phoneNumber").stringValue
    var v4 = userData.get("email").stringValue
    var v5 = userData.get("username").stringValue


    if(check_username(all_usernames, v5)){
        return mapOf("error" to errors["USERNAME_EXISTS"].toString())
    }
    else if(check_email(all_emails, v4)){
        return mapOf("error" to errors["EMAIL_EXISTS"].toString())
    }
    else if(check_phonenumber(all_numbers, v3)){
        return mapOf("error" to errors["PHONENUMBER_EXISTS"].toString())
    }
//    else if(!isEmailValid(v4)){
//        return errors["INVALID_EMAIL"].toString()
//    }
//    else if(!isPhoneNumber(v3)){
//        return errors["INVALID_PHONENUMBER"].toString()
//    }
        else {
        val user = User(v1, v2, v3, v4, v5);

        all_users[v5] = user
        all_emails.add(v4)
        all_numbers.add(v3)
        all_usernames.add(v5)


        val newUser = mapOf("firstName" to user.firstName.toString(), "lastName" to user.lastName.toString(), "phoneNumber" to user.phoneNumber.toString(), "email" to user.email.toString(), "userName" to user.username.toString() )

        return mapOf("user" to newUser, "message" to success_response["USER_CREATED"].toString())
        }
}
    fun accountInformation(userName: String): Any {
        val user = all_users[userName.toString()]
        var v1= mapOf("firstName" to user?.firstName.toString() ,"lastName" to user?.lastName.toString(), "phoneNumber" to user?.phoneNumber.toString(), "email" to user?.email.toString(), "wallet" to user?.wallet, "inventory" to user?.inventory)
        if(user!=null){
            //val newUser = "{\"firstName\": ${user?.firstName.toString()}, \"lastName\": ${user?.lastName}, \"phoneNumber\": ${user?.phoneNumber}, \"email\": ${user?.email}, \"username\": ${user?.username}"
            //println(newUser)
            return v1
        }

        return mapOf("errors" to errors["USER_DOES_NOT_EXISTS"].toString())

    }


    fun adding_inventory(body: JsonObject, userName: String): Any
    {
        var quant=body.get("quantity").longValue

        var usr1= all_users[userName]

        if (usr1 != null) {
            usr1.addInventory(quant)
            return mapOf("message" to "${quant} ESOPS added to inventory")
        }
        return mapOf("errors" to errors["USER_DOES_NOT_EXISTS"].toString())
    }

    fun adding_Money(body: JsonObject, userName: String): Any
    {
        var amt=body.get("amount").longValue
        var usr1= all_users[userName]

        if (usr1 != null) {
            usr1.addWallet(amt)
            return mapOf("message" to "${amt} amount added to account");
        }
        return mapOf("messsage" to "user does not exist")
    }





}