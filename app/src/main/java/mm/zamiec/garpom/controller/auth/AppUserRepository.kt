package mm.zamiec.garpom.controller.auth

import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEmpty
import mm.zamiec.garpom.controller.firebase.documentAsFlow
import mm.zamiec.garpom.domain.model.AppUser
import mm.zamiec.garpom.domain.model.dto.MeasurementDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppUserRepository @Inject constructor() {

    private val db = Firebase.firestore

    fun mapper(doc: DocumentSnapshot): AppUser {
        return AppUser(
            doc.id,
            doc.getString("username") ?: "",
            false
        )
    }


    fun getUserById(id: String): Flow<AppUser> {
        return db.documentAsFlow("users", id, ::mapper).map {
            it ?: AppUser()
        }
    }

    fun changeUsername(userId: String, newUsername: String): Task<Void?> {
        return db.collection("users").document(userId)
            .update("username", newUsername)
    }
}