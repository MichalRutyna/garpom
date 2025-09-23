package mm.zamiec.garpom.ui.screens.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import mm.zamiec.garpom.auth.AuthRepository
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {

    private val TAG = "HomeViewModel"


    val currentUser get() = repository.currentUser

    fun test() {
        repository.test()
    }
}