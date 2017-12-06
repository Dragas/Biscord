package lt.saltyjuice.dragas.chatty.v3.biscord.entity

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.vladmihalcea.hibernate.type.array.StringArrayType
import org.hibernate.annotations.ColumnDefault
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.hibernate.annotations.TypeDefs
import javax.persistence.*
import lt.saltyjuice.dragas.chatty.v3.biscord.entity.Type as CardType

@Entity
@Table(name = "cards")
@TypeDefs(TypeDef(name = "string-array", typeClass = StringArrayType::class))
open class Card : Comparable<Card>
{
    @Expose
    @SerializedName("id")
    open var cardId: String = ""

    @Expose
    @SerializedName("dbfId")
    @Id
    open var dbfId: Int = -1

    @Expose
    @SerializedName("name")
    open var name: String = ""

    @Expose
    @SerializedName("set")
    open var cardSet: String = ""

    @Expose
    @SerializedName("type")
    open var type: CardType? = CardType.SPELL

    @Expose
    @SerializedName("faction")
    open var faction: String = ""

    @Expose
    @SerializedName("rarity")
    open var rarity: String = ""

    @Expose
    @SerializedName("cost")
    open var cost: Int = 0

    @Expose
    @SerializedName("attack")
    open var attack: Int = 0

    @Expose
    @SerializedName("health")
    open var health: Int = 0

    @Expose
    @SerializedName("armor")
    open var armor: Int = 0

    @Expose
    @SerializedName("text")
    open var text: String = ""

    @Expose
    @SerializedName("flavor")
    open var flavor: String = ""

    @Expose
    @SerializedName("entourage")
    @Column(columnDefinition = "text[]")
    @Type(type = "string-array")
    open var entourage: Array<String> = arrayOf()


    @Transient
    open var entourages: List<Card> = ArrayList()

    @Expose
    @SerializedName("artist")
    open var artist: String = ""

    @Expose
    @SerializedName("collectible")
    open var collectible: Boolean = false

    @Expose
    @SerializedName("elite")
    @Column(name = "elite_boolean", nullable = false)
    @ColumnDefault("false")
    open var elite: Boolean = false

    @Deprecated("kept for database constraints. use elite field instead")
    @Column(name = "elite", nullable = true)
    open var oldElite : String? = null

    @Expose
    @SerializedName("race")
    open var race: String = ""

    @Expose
    @SerializedName("playerClass")
    open var playerClass: String = ""

    @Expose
    @SerializedName("img")
    @Transient
    open var img: String = ""
        get()
        {
            return "https://art.hearthstonejson.com/v1/render/latest/enUS/512x/$cardId.png"
        }
    val artwork: String
        get()
        {
            return "https://art.hearthstonejson.com/v1/512x/$cardId.jpg"
        }
    @Expose
    @SerializedName("imgGold")
    @Transient
    open var imgGold: String = ""


    @Expose
    @SerializedName("durability")
    open var durability: Int = 0

    @Expose
    @SerializedName("mechanics")
    @Column(columnDefinition = "text[]", nullable = false)
    @Type(type = "string-array")
    @ColumnDefault("[]")
    open var mechanics : Array<String> = arrayOf()

    fun getStatisticsURL(): String
    {
        return "https://hsreplay.net/cards/$dbfId"
    }

    fun getCraftingCost(): Pair<Int, Int>
    {
        return when (rarity)
        {
            "COMMON" -> Pair(40, 400)
            "RARE" -> Pair(100, 800)
            "EPIC" -> Pair(400, 1600)
            "LEGENDARY" -> Pair(1600, 3200)
            else -> Pair(0, 0)
        }
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