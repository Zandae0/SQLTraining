package Data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import Model.UserEntity
import android.content.Context

@Database(entities = [UserEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sql_training_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}