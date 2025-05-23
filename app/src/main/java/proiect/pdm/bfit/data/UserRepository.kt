package proiect.pdm.bfit.data

class UserRepository(val userDao: UserDao) {
    suspend fun getUserById(id: Long) = userDao.getUserById(id)
    suspend fun addUser(user: UserEntity) = userDao.insert(user)
}