package com.demo.app

// kotlin imports
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import retrofit2.Retrofit
import retrofit2.Retrofit

/**
 * Repository class
 * handles data fetching with coroutines
 */
class UserRepository(private val api: ApiService){

    // coroutine scope for background work
    private val scope = CoroutineScope(Dispatchers.IO)

    // fetch users in background
    fun fetchUsers(onResult: (List<User>) -> Unit){
        scope.launch{ // launch coroutine
            // switch to IO thread
            val users = withContext(Dispatchers.IO){
                api.getUsers() // call API
            }



            // switch back to main thread
            withContext(Dispatchers.Main){
                onResult(users) // deliver result
            }
        }
    }

    // suspend function for direct call
    suspend fun getUser(id: Int): User{
        return withContext(Dispatchers.IO){
            api.getUser(id) // fetch single user
        }
    }
}
