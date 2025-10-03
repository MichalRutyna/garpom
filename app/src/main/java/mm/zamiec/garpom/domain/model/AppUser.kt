package mm.zamiec.garpom.domain.model

data class AppUser (
    val id: String = "",
    val username: String = "",
    val isAnonymous: Boolean = true
)