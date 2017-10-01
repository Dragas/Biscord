package lt.saltyjuice.dragas.chatty.v3.biscord.entity

enum class PlayerClass(val value: String, val id: Int)
{

    WARLOCK("Warlock", 893),

    WARRIOR("Warrior", 7),
    MAGNI("Warrior", 2828),

    HUNTER("Hunter", 31),
    ALLERIA("Hunter", 2826),

    SHAMAN("Shaman", 1066),
    MORGL("Shaman", 40183),


    DRUID("Druid", 274),

    MAGE("Mage", 637),
    KHADGAR("Mage", 39117),
    MEDIVH("Mage", 2829),

    ROGUE("Rogue", 930),
    MAIEV("Rogue", 40195),

    PALADIN("Paladin", 671),
    LIADRIN("Paladin", 2827),
    ARTHAS("Paladin", 46116),

    PRIEST("Priest", 813),
    TYRANDE("Priest", 41887),

    NEUTRAL("Neutral", -1);

    companion object
    {
        @JvmStatic
        fun getById(id: Int): PlayerClass
        {
            return PlayerClass.values().find { it.id == id } ?: NEUTRAL
        }
    }
}