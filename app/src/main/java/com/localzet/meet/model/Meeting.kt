package com.localzet.meet.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meetings")
data class Meeting(
    @PrimaryKey val code: String,
    val timeInMillis: Long
)