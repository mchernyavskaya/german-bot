package tk.germanbot.fsm

abstract class Transition {
    abstract fun accept(event: Event): Boolean
    abstract fun follow(event: Event): State
}