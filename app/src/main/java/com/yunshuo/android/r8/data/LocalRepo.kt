package com.yunshuo.android.r8.data

object LocalRepo {

    val textList = listOf(
        "心如河流，静时清澈，动时汹涌，而爱是那无法停息的源头。",
        "每一颗心都有一条要穿越的河流，彼岸便是灵魂与爱的交汇之地。",
        "追寻爱就像探寻星辰，越是接近，越感浩瀚无边。",
        "在你眼中，我看见了日月交替，仿佛世界因爱而生。",
        "灵魂深处有一座桥，通向我未曾抵达的你。",
        "爱是一场漫长的旅途，它的尽头是彼此凝视的时光。",
        "沉默的心懂得爱，正如平静的湖倒映星空的无涯。",
        "生命中的每一场相遇，都是心灵对宇宙温柔的回应。",
        "你的笑容如晨曦初升，唤醒了我沉睡的梦境。",
        "我听见你的声音，如同河流听见大海的召唤，无法抗拒，奔赴永恒。"
    )

    /**
     * 插入情绪状态记录
     *
     * @param mood MoodStatus
     * @param influencingEvent String
     */
    suspend fun insertUserMood(mood: MoodStatus, influencingEvent: String = ""): UserMood {
        val userMood = UserMood(mood = mood, influencingEvent = influencingEvent)
        userMood.id = DB.INSTANCE.userMoodDao().insertUserMood(userMood)
        return userMood
    }

    /**
     * 获取所有情绪状态记录，按时间降序排序
     *
     * @return List<UserMood>
     */
    suspend fun getAllUserMood(): List<UserMood> {
        return DB.INSTANCE.userMoodDao().getAllUserMood()
    }
}