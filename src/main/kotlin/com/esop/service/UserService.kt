package com.esop.service

import com.esop.schema.User
import io.micronaut.json.tree.JsonObject
import com.esop.constant.errors
import com.esop.constant.success_response
import com.esop.schema.Inventory
import jakarta.inject.Singleton


@Singleton
class UserService {
    val all_emails = mutableSetOf<String>()
    val all_numbers = mutableSetOf<String>()
    val all_usernames = mutableSetOf<String>()
    var all_users = HashMap<String, User>()

    fun check_inventory(quantity: Long, userName: String, type: String): Boolean{
        val userinventory = all_users[userName]?.inventory!!
        if(type == "normal"){
            if(user_exists(userName) && userinventory.normalInventory.free>= quantity){
                return true
            }
        }else{
            if(user_exists(userName) && userinventory.performanceInventory.free>= quantity){
                return true
            }
        }
        return false
    }
    fun check_wallet(amount: Long, userName: String): Boolean{
        if(user_exists(userName) && all_users[userName]?.wallet?.free!! >= amount){
            return true
        }
        return false
    }
    fun orderCheckBeforePlace(userName: String, quantity: Long, type: String, price: Long, inventoryType : String): Map<String, MutableList<String>>{
        var userErrors = mutableListOf<String>()
        if(!all_users.containsKey(userName)){
            userErrors.add("User doesn't exist")
            return mapOf("error" to userErrors)
        }
        if(type == "buy"){
            if(!check_wallet(price*quantity, userName)){
                userErrors.add("Insufficient funds")
            }
        }
        else if(type == "sell"){
            if(inventoryType == "performance"){
                if(!check_inventory(quantity, userName,"performance")){
                    userErrors.add("Insufficient performance inventory")
                }
            }else{
                if(!check_inventory(quantity, userName,"normal")){
                    userErrors.add("Insufficient normal inventory")
                }
            }
        }
        if(userErrors.isEmpty()){
            if(type == "buy"){
                all_users[userName]?.buyAndUpdateWallet(price*quantity)
            }
            if(type == "sell"){
                all_users[userName]?.sellAndUpdateInventory(quantity,inventoryType)
            }
        }
        return mapOf("error" to userErrors)
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
    var firstName = userData.get("firstName").stringValue
    var lastName = userData.get("lastName").stringValue
    var phoneNumber = userData.get("phoneNumber").stringValue
    var email = userData.get("email").stringValue
    var username = userData.get("username").stringValue

        var Errors = mutableListOf<String>()


    if(check_username(all_usernames, username)){
//        return mapOf("error" to errors["USERNAME_EXISTS"].toString())
        Errors.add(errors["USERNAME_EXISTS"].toString())

        return mapOf("error" to Errors)
    }
    else if(check_email(all_emails, email)){
//        return mapOf("error" to errors["EMAIL_EXISTS"].toString())
        Errors.add(errors["EMAIL_EXISTS"].toString())

        return mapOf("error" to Errors)
    }
    else if(check_phonenumber(all_numbers, phoneNumber)){
//        return mapOf("error" to errors["PHONENUMBER_EXISTS"].toString())
        Errors.add(errors["PHONENUMBER_EXISTS"].toString())

        return mapOf("error" to Errors)
    }
    else if(!isEmailValid(email)){
        Errors.add(errors["INVALID_EMAIL"].toString())

        return mapOf("error" to Errors)

    }
    else if(!isPhoneNumber(phoneNumber)){
        Errors.add(errors["INVALID_PHONENUMBER"].toString())

        return mapOf("error" to Errors)
    }
        else {
        val user = User(firstName, lastName, phoneNumber, email, username);

        all_users[username] = user
        all_emails.add(email)
        all_numbers.add(phoneNumber)
        all_usernames.add(username)


        val newUser = mapOf("firstName" to user.firstName.toString(), "lastName" to user.lastName.toString(), "phoneNumber" to user.phoneNumber.toString(), "email" to user.email.toString(), "userName" to user.username.toString() )
        return newUser

        }
}
    fun accountInformation(userName: String): Map<String, Any?> {
        val user = all_users[userName.toString()]

        var accountErrors = mutableListOf<String>()

        var userData= mapOf("firstName" to user?.firstName.toString() ,"lastName" to user?.lastName.toString(), "phoneNumber" to user?.phoneNumber.toString(), "email" to user?.email.toString(), "wallet" to user?.wallet, "inventory" to user?.inventory)
        if(user!=null){
            return userData
        }

        accountErrors.add(errors["USER_DOES_NOT_EXISTS"].toString())

        return mapOf("error" to accountErrors)
    }


    fun adding_inventory(body: JsonObject, userName: String): Map<String, Any>
    {

        var quant=body.get("quantity").longValue
        var type:String = "normal"
        type = body.get("inventoryType").stringValue.lowercase()
        var accountErrors =mutableListOf<String>()

        var usr1= all_users[userName]

        if (usr1 != null) {
            if(quant > 10000 || quant <=0){
                accountErrors.add(errors["QUANTITY_NOT_ACCEPTED"].toString())
            }else{
                if(type != "normal" && type != "performance"){
                    accountErrors.add(errors["INVALID_TYPE"].toString())
                }else{
                    usr1.addInventory(quant,type)
                    return if(type == "performance"){
                        mapOf("message" to "${quant} Performance ESOPS added to inventory")
                    }else{
                        mapOf("message" to "${quant} normal added to inventory")
                    }
                }
            }
        }else{
            accountErrors.add(errors["USER_DOES_NOT_EXISTS"].toString())
        }
        return mapOf("error" to accountErrors)
    }

    fun adding_Money(body: JsonObject, userName: String): Map<String, Any>
    {
        var amt=body.get("amount").longValue
        var accountErrors =mutableListOf<String>()
        var usr1= all_users[userName]

        if (usr1 != null) {
            usr1.addWallet(amt)
            return mapOf("message" to "${amt} amount added to account");
        }
        accountErrors.add(errors["USER_DOES_NOT_EXISTS"].toString())

        return mapOf("error" to accountErrors)

        //return mapOf("errors" to errors["USER_DOES_NOT_EXISTS"]).toString()
    }
}