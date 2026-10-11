package com.iqbalansyori.izzah.data

import com.iqbalansyori.izzah.domain.repository.BlockingController
import com.iqbalansyori.izzah.domain.repository.ReminderScheduler

// replaced by the real implementations in the notification and blocking tasks
object NoopReminderScheduler : ReminderScheduler {
    override fun rescheduleAll() = Unit
    override fun cancelAll() = Unit
}

object NoopBlockingController : BlockingController {
    override fun evaluate() = Unit
    override fun stop() = Unit
}
