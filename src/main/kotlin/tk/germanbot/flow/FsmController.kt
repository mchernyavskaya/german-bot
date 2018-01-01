package tk.germanbot.flow

import org.slf4j.LoggerFactory
import tk.germanbot.fsm.Event

class FsmController(
        private val fsmFactory: FsmFactory,
        private val stateService: StateService) {

    private val logger = LoggerFactory.getLogger(FsmController::class.java)

    fun acceptEvent(userId: String, event: Event) {
        logger.debug("Event received: {}", event.javaClass.simpleName)

        val state = stateService.getState(userId).orElse(fsmFactory.createReadyState(userId))
        val nextState = state.nextState(event)
        stateService.saveState(nextState)
    }

}
