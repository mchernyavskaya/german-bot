package tk.germanbot.activity

import java.util.concurrent.CompletableFuture

interface EventDispatcher {
    fun handleEvent(userId: String, event: Event)
}

class AsyncEventDispatcher(private val innerDispatcher: EventDispatcher) : EventDispatcher {

    override fun handleEvent(userId: String, event: Event) {
        CompletableFuture.runAsync { innerDispatcher.handleEvent(userId, event) }
    }

}