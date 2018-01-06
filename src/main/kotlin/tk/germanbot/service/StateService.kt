package tk.germanbot.service

import tk.germanbot.activity.ActivityData

interface StateService {
    fun getActivityStack(userId: String): List<ActivityData>
    fun saveActivityStack(userId: String, stack: List<ActivityData>)

    fun updateActivityData(activity: ActivityData) {
        val activityStack = getActivityStack(activity.userId).toMutableList()
        activityStack.replaceAll { if (it.id == activity.id) activity else it }
        saveActivityStack(activity.userId, activityStack)
    }
}
