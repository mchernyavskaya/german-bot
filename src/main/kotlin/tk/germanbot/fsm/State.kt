package tk.germanbot.fsm


interface StateData {
    val userId: String
}

abstract class State(open val stateData: StateData) {

    abstract val transitions: List<Transition>

    protected abstract val unknownEventTransition: Transition

    fun nextState(event: Event): State =
            transitions
                    .stream()
                    .filter { tr -> tr.accept(event) }
                    .findAny()
                    .map { tr -> tr.follow(event) }
                    .orElseGet{ unknownEventTransition.follow(event) }

}
