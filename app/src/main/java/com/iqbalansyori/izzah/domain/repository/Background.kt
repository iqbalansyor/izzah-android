package com.iqbalansyori.izzah.domain.repository

interface ReminderScheduler {
    fun rescheduleAll()
    fun cancelAll()
}

interface BlockingController {
    fun evaluate()
    fun stop()
}
