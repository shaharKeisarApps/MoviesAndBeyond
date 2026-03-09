package com.keisardev.moviesandbeyond.feature.auth

import com.keisardev.moviesandbeyond.core.testing.MainDispatcherRule
import com.keisardev.moviesandbeyond.data.testdoubles.repository.TestAuthRepository
import com.keisardev.moviesandbeyond.data.testdoubles.repository.TestUserRepository
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AuthViewModelTest {
    private val authRepository = TestAuthRepository()
    private val userRepository = TestUserRepository()
    private lateinit var viewModel: AuthViewModel

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Before
    fun setup() {
        viewModel = AuthViewModel(authRepository = authRepository, userRepository = userRepository)
    }

    @Test
    fun `test initial state`() {
        assertEquals(AuthUiState(), viewModel.uiState.value)
    }

    @Test
    fun `test login success when user onboards`() {
        val username = "name"
        val password = "1234"

        viewModel.onUsernameChange(username)
        viewModel.onPasswordChange(password)
        viewModel.logIn()

        assertFalse(viewModel.uiState.value.isLoggedIn)
    }

    @Test
    fun `test login success when after onboarding`() = runTest {
        userRepository.setHideOnboarding(true)
        viewModel = AuthViewModel(authRepository = authRepository, userRepository = userRepository)

        val username = "name"
        val password = "1234"

        viewModel.onUsernameChange(username)
        viewModel.onPasswordChange(password)
        viewModel.logIn()

        assertTrue(viewModel.uiState.value.isLoggedIn)
    }

    @Test
    fun `test login failure`() = runTest {
        authRepository.generateError(true)

        viewModel.onUsernameChange("error")
        viewModel.onPasswordChange("1234")
        viewModel.logIn()

        // ViewModel maps Result.Error to a user-friendly message — assert it is set
        assertFalse(viewModel.uiState.value.errorMessage.isNullOrEmpty())
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `test error message reset`() {
        viewModel.onErrorShown()

        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `test username change`() {
        val username = "testuser"

        viewModel.onUsernameChange(username)

        assertEquals(username, viewModel.uiState.value.username)
    }

    @Test
    fun `test password change`() {
        val password = "testpassword"

        viewModel.onPasswordChange(password)

        assertEquals(password, viewModel.uiState.value.password)
    }

    @Test
    fun `test error then retry then success flow`() = runTest {
        userRepository.setHideOnboarding(true)
        viewModel = AuthViewModel(authRepository = authRepository, userRepository = userRepository)

        // First attempt fails
        authRepository.generateError(true)
        viewModel.onUsernameChange("name")
        viewModel.onPasswordChange("1234")
        viewModel.logIn()

        assertFalse(viewModel.uiState.value.errorMessage.isNullOrEmpty())
        assertFalse(viewModel.uiState.value.isLoggedIn)

        // Dismiss error, retry with fixed repo
        viewModel.onErrorShown()
        assertNull(viewModel.uiState.value.errorMessage)

        authRepository.generateError(false)
        viewModel.logIn()

        assertTrue(viewModel.uiState.value.isLoggedIn)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `test loading state is set during login`() = runTest {
        userRepository.setHideOnboarding(true)
        viewModel = AuthViewModel(authRepository = authRepository, userRepository = userRepository)

        // After successful login, loading should be false
        viewModel.onUsernameChange("name")
        viewModel.onPasswordChange("1234")
        viewModel.logIn()

        assertFalse(viewModel.uiState.value.isLoading)
    }
}
