package lt.saltyjuice.dragas.chatty.v3.biscord.entity

enum class PlayerClass(val value: String, val id: Int)
{

    Warlock("Warlock", 893),

    Warrior("Warrior", 7),
    Magni("Warrior", 2828),

    Hunter("Hunter", 31),
    Alleria("Hunter", 2826),

    Shaman("Shaman", 1066),
    Morgl("Shaman", 40183),


    Druid("Druid", 274),

    Mage("Mage", 637),
    Khadgar("Mage", 39117),
    Medivh("Mage", 2829),

    Rogue("Rogue", 930),
    Maiev("Rogue", 40195),

    Paladin("Paladin", 671),
    Liadrin("Paladin", 2827),
    Arthas("Paladin", 46116),

    Priest("Priest", 813),
    Tyrande("Priest", 41887),

    Neutral("Neutral", -1);

    companion object
    {
        @JvmStatic
        fun getById(id: Int): PlayerClass
        {
            return PlayerClass.values().find { it.id == id } ?: Neutral
        }
    }
}