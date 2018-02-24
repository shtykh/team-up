package shtykh.teamup.domain

abstract class Party<out T> {
    var id: String
        set(value) {
            field = value.replace(" ", "_")
        }
    open var name: String

    constructor(id: String, name: String = "Some party") {
        this.id = id
        this.name = name
        this.members = ArrayList()
    }

    open var members: MutableList<String>

    fun size(): Int {
        return members.size
    }

    open infix fun hire(newby: Person): T {
        return hire(newby.id)
    }

    open infix fun hire(newby: String): T {
        if (!members.contains(newby)) {
            members.add(newby)
        }
        return instance()
    }

    open fun hireAll(vararg newbies: Person): T {
        newbies.map(Person::id)
                .filter { !members.contains(it) }
                .forEach { this hire it }
        return instance()
    }

    open fun hireAll(vararg newbies: String): T {
        members.addAll(newbies)
        return instance()
    }

    open infix fun fire(retired: String): T {
        members.remove(retired)
        return instance()
    }

    open infix fun fire(retired: Person): T {
        return fire(retired.id)
    }

    open fun fireAll(vararg newbies: Person): T {
        newbies.map(Person::id)
                .forEach({ members.remove(it) })
        return instance()
    }

    abstract fun instance(): T

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Party<*>) return false

        if (name != other.name) return false
        if (members != other.members) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + members.hashCode()
        return result
    }
}

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

    override fun toString(): String {
        return "PartyImpl(name='$name', members='$members')"
    }


}

interface Manageble {
    var admin: String
    fun managers(): MutableList<String>
}
