package Data

import Model.UserEntity

class UserRepository(private val userDao: UserDao) {

    suspend fun registerUser(user: UserEntity) {
        userDao.insertUser(user)
    }

    suspend fun getUser(username: String): UserEntity? {
        return userDao.getUserByUsername(username)
    }

    suspend fun getAllUsers(): List<UserEntity> {
        return userDao.getAllUsers()
    }

    suspend fun updateUser(user: UserEntity) {
        userDao.updateUser(user)
    }

    suspend fun deleteUser(user: UserEntity) {
        userDao.deleteUser(user)
    }
}