package mm.zamiec.garpom.controller.firebase


import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf


fun <T> FirebaseFirestore.queryAsFlow(
    query: Query,
    mapper: (doc: DocumentSnapshot) -> T?
): Flow<List<T>> = callbackFlow {
    val listener: ListenerRegistration = query.addSnapshotListener { snapshot, error ->
        if (error != null) {
            close(error)
            return@addSnapshotListener
        }

        val items = snapshot?.documents
            ?.mapNotNull { mapper(it) }
            .orEmpty()

        trySend(items).isSuccess
    }
    awaitClose { listener.remove() }
}

fun <T> FirebaseFirestore.documentAsFlow(
    collectionPath: String,
    documentId: String,
    mapper: (doc: DocumentSnapshot) -> T?
): Flow<T?> = callbackFlow {
    val listener = collection(collectionPath)
        .document(documentId)
        .addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val item = snapshot?.let { mapper(it) }
            trySend(item).isSuccess
        }

    awaitClose { listener.remove() }
}

fun <T> FirebaseFirestore.filteredCollectionAsFlow(
    collectionPath: String,
    field: String,
    value: Any,
    mapper: (doc: DocumentSnapshot) -> T?
): Flow<List<T>> =
    queryAsFlow(collection(collectionPath).whereEqualTo(field, value), mapper)

fun <T> FirebaseFirestore.collectionByIdsAsFlow(
    collectionPath: String,
    ids: List<String>,
    mapper: (doc: DocumentSnapshot) -> T?
): Flow<List<T>> {
    if (ids.isEmpty()) return flowOf(emptyList())
    return queryAsFlow(
        collection(collectionPath).whereIn(FieldPath.documentId(), ids.take(10)),
        mapper
    )
}
