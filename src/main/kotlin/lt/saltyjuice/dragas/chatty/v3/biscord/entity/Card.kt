package lt.saltyjuice.dragas.chatty.v3.biscord.entity

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

open class Card : Comparable<Card>
{
    @Expose
    @SerializedName("id")
    var cardId: String = ""

    @Expose
    @SerializedName("dbfId")
    var dbfId: Int = -1
    @Expose
    @SerializedName("name")
    var name: String = ""
    @Expose
    @SerializedName("set")
    var cardSet: String = ""
    @Expose
    @SerializedName("type")
    var type: Type? = Type.SPELL
    @Expose
    @SerializedName("faction")
    var faction: String = ""
    @Expose
    @SerializedName("rarity")
    var rarity: String = ""
    @Expose
    @SerializedName("cost")
    var cost: Int = 0
    @Expose
    @SerializedName("attack")
    var attack: Int = 0
    @Expose
    @SerializedName("health")
    var health: Int = 0
    @Expose
    @SerializedName("armor")
    var armor: Int = 0
    @Expose
    @SerializedName("text")
    var text: String = ""
    @Expose
    @SerializedName("flavor")
    var flavor: String = ""
    @Expose
    @SerializedName("entourage")
    var entourage: List<String> = ArrayList()
    var entourages: List<Card> = ArrayList()
    @Expose
    @SerializedName("artist")
    var artist: String = ""
    @Expose
    @SerializedName("collectible")
    var collectible: Boolean = false
    @Expose
    @SerializedName("elite")
    var elite: String = ""
    @Expose
    @SerializedName("race")
    var race: String = ""
    @Expose
    @SerializedName("playerClass")
    var playerClass: String = ""
    @Expose
    @SerializedName("img")
    var img: String = ""
        get()
        {
            return "https://art.hearthstonejson.com/v1/render/latest/enUS/256x/$cardId.png"
        }
    @Expose
    @SerializedName("imgGold")
    var imgGold: String = ""
    @Expose
    @SerializedName("locale")
    var locale: String = ""
    @Expose
    @SerializedName("howToGet")
    var howToGet: String = ""
    @Expose
    @SerializedName("howToGetGold")
    var howToGetGold: String = ""
    @Expose
    @SerializedName("durability")
    var durability: Int = 0

    fun getStatisticsURL(): String
    {
        return "https://hsreplay.net/cards/$dbfId"
    }

    /**
     * Compares this object with the specified object for order. Returns zero if this object is equal
     * to the specified [other] object, a negative number if it's less than [other], or a positive number
     * if it's greater than [other].
     */
    override fun compareTo(other: Card): Int
    {
        return dbfId - other.dbfId
    }
}