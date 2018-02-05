package shtykh.teamup.domain

class PartyImpl(override var name: String = "PartyImpl") : Party<PartyImpl>(name) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false
        other as PartyImpl

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun instance(): PartyImpl {
        return this
    }
}
