package tk.germanbot.flow.event

import tk.germanbot.fsm.Event

class UserTextMessageEvent(
        val userId: String,
        val message: String) : Event()