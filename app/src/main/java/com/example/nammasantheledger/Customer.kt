package com.example.nammasantheledger

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey val name: String, // Unique username as the ID
    val pendingUdari: Double = 0.0,
    val totalPaid: Double = 0.0,
    val balance: Double = 0.0
)