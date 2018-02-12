package shtykh.teamup.controller

abstract class State {
    abstract fun next(command: String): State
}

class Start: State() {
    override fun next(command: String): State {
        return Finish()
    }
}

class Finish: State() {
    override fun next(command: String): State {
        throw IllegalStateException("It's finished, it's over!")
    }

}
