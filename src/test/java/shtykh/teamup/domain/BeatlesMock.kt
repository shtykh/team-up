package shtykh.teamup.domain

import shtykh.teamup.domain.team.Team
import java.util.*

var beatles = Team("The Beatles")

val john = Person("john", "John Lennon")
val paul = Person("paul", "Paul McCartney")

val ringo = Person("ringo", "Ringo Starr")
val george = Person("george", "George Harrison")

val pete = Person("pete", "Pete Best")

val stewart = Person("stewart", "Stuart Sutcliffe")

fun hireBeatles() : Team {
    beatles = Team("The Beatles")
    beatles.hireAll(john, paul, ringo, george)
            .legio.hireAll(pete, stewart)
    return beatles
}

fun saveBeatles() {
    Arrays.asList(john, paul, george, ringo, stewart, pete)
            .forEach {it.save()}
}
