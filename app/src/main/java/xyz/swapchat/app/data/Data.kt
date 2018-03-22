package xyz.teamcatalyst.breedr.data

import java.io.Serializable

/**
 * @author A-Ar Andrew Concepcion
 * @createdOn 28/06/2017
 */
class Item(var name: String = "",
           var ownerId: String = "",
           var itemImage: String = "",
           var category: String = "",
           var gender: String = "",
           var age: String = "",
           var city: String = "",
           var about: String = "",
           var itemId: String = "",
           var imageList: List<String> = listOf()) : Serializable {
    constructor() : this(itemId = "", name = "Unnamed")

    var disliked = false
    var lastLiked = 0L
}

class Profile(var displayName: String = "",
              var profileUrl: String = "",
              var contactNumber: String = "",
              var email: String = "") {
    constructor() : this(displayName = "John Doe")
}

