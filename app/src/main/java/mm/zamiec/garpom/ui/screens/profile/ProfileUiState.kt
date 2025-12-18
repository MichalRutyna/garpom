package mm.zamiec.garpom.ui.screens.profile


data class ProfileScreenState(
    val userId: String = "",
    val username: String = "",
    val isAnonymous: Boolean = true
)


sealed class ChangeUsernameScreenState {
    data object Success: ChangeUsernameScreenState()
    data object Loading: ChangeUsernameScreenState()
    data class Error(val message: String) : ChangeUsernameScreenState()
}