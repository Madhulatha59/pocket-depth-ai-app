package com.periodontal.ai.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "patients")
data class Patient(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val age: Int,
    val gender: String,
    val phoneNumber: String,
    val createdAt: Long = System.currentTimeMillis()
)
