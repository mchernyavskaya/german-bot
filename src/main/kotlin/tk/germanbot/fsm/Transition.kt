package tk.germanbot.fsm

abstract class Transition {
    open val helpText = ""
    abstract fun accept(event: Event): Boolean
    abstract fun follow(event: Event): State
}