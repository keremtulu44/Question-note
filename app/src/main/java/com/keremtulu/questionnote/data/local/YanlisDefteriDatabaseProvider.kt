package com.keremtulu.questionnote.data.local

import android.content.Context
import androidx.room.Room

object YanlisDefteriDatabaseProvider {
    @Volatile
    private var instance: YanlisDefteriDatabase? = null

    fun getInstance(context: Context): YanlisDefteriDatabase =
        instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                YanlisDefteriDatabase::class.java,
                YanlisDefteriDatabase.DATABASE_NAME,
            )
                .addCallback(VarsayilanDersleriEkle())
                .addMigrations(YanlisDefteriMigrations.MIGRATION_1_2)
                .build()
                .also { instance = it }
        }
}
