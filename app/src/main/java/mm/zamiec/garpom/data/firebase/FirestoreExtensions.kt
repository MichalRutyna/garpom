package mm.zamiec.garpom.data.firebase


import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf


fun <T, D> queryAsFlow(
    query: Query,
    dtoMapper: (doc: DocumentSnapshot) -> T?,
    domainMapper: (dto: T) -> D?,
): Flow<List<D>> = callbackFlow {
    val listener: ListenerRegistration =
        query
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val items = snapshot?.documents
                    ?.mapNotNull { dtoMapper(it) }
                    ?.mapNotNull { domainMapper(it) }
                    .orEmpty()

                trySend(items).isSuccess
            }
    awaitClose { listener.remove() }
}

fun <T, D> FirebaseFirestore.documentAsFlow(
    collectionPath: String,
    documentId: String,
    dtoMapper: (doc: DocumentSnapshot) -> T?,
    domainMapper: (dto: T) -> D?,
): Flow<D?> = callbackFlow {
    val listener = collection(collectionPath)
        .document(documentId)
        .addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val item = snapshot
                ?.let { dtoMapper(it) }
                ?.let { domainMapper(it) }
            trySend(item).isSuccess
        }

    awaitClose { listener.remove() }
}

fun <T, D> FirebaseFirestore.filteredCollectionAsFlow(
    collectionPath: String,
    field: String,
    value: Any,
    dtoMapper: (doc: DocumentSnapshot) -> T?,
    domainMapper: (dto: T) -> D?,
): Flow<List<D>> =
    queryAsFlow(
        collection(collectionPath).whereEqualTo(field, value),
        dtoMapper,
        domainMapper
    )


fun <T, D> FirebaseFirestore.filteredArrayContainsCollectionAsFlow(
    collectionPath: String,
    arrayField: String,
    value: Any,
    dtoMapper: (doc: DocumentSnapshot) -> T?,
    domainMapper: (dto: T) -> D?,
): Flow<List<D>> =
    queryAsFlow(
        collection(collectionPath).whereArrayContains(arrayField, value),
        dtoMapper,
        domainMapper
    )


fun <T, D> FirebaseFirestore.collectionByIdsAsFlow(
    collectionPath: String,
    ids: List<String>,
    dtoMapper: (doc: DocumentSnapshot) -> T?,
    domainMapper: (dto: T) -> D?,
): Flow<List<D>> {
    if (ids.isEmpty()) return flowOf(emptyList())
    return queryAsFlow(
        collection(collectionPath).whereIn(FieldPath.documentId(), ids.take(10)),
        dtoMapper,
        domainMapper
    )
}
