package com.example.mappi.presentation.ui.friends.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mappi.domain.model.FriendRequest
import com.example.mappi.domain.model.UserData
import com.example.mappi.domain.use_case.friends.AcceptRequestUseCase
import com.example.mappi.domain.use_case.friends.DeleteFriendUseCase
import com.example.mappi.domain.use_case.friends.GetFriendRequestsUseCase
import com.example.mappi.domain.use_case.friends.GetFriendsUseCase
import com.example.mappi.domain.use_case.friends.RejectRequestUseCase
import com.example.mappi.domain.use_case.friends.SearchFriendsUseCase
import com.example.mappi.domain.use_case.friends.SendRequestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val getFriendsUseCase: GetFriendsUseCase,
    private val searchFriendsUseCase: SearchFriendsUseCase,
    private val sendRequestUseCase: SendRequestUseCase,
    private val acceptRequestUseCase: AcceptRequestUseCase,
    private val rejectRequestUseCase: RejectRequestUseCase,
    private val getFriendRequestsUseCase: GetFriendRequestsUseCase,
    private val deleteFriendUseCase: DeleteFriendUseCase
) : ViewModel() {
    private val _friends = MutableStateFlow<List<UserData>>(emptyList())
    val friends: StateFlow<List<UserData>> get() = _friends

    private val _searchResults = MutableStateFlow<List<UserData>>(emptyList())
    val searchResults: StateFlow<List<UserData>> get() = _searchResults

    private val _friendRequests = MutableStateFlow<List<FriendRequest>>(emptyList())
    val friendRequests: StateFlow<List<FriendRequest>> get() = _friendRequests

    init {
        refreshData()
        searchFriends("")
    }

    fun searchFriends(query: String) {
        launchWithErrorHandler {
            _searchResults.value = searchFriendsUseCase(query)
        }
    }

    fun sendRequest(friendId: String) {
        launchWithErrorHandler {
            sendRequestUseCase(friendId)
            refreshData()
            searchFriends("") // Refresh search results to update statuses
        }
    }

    fun acceptRequest(requestId: String) {
        launchWithErrorHandler {
            acceptRequestUseCase(requestId)
            removeFriendRequest(requestId)
            refreshData()
            searchFriends("") // Refresh search results to update statuses
        }
    }

    fun rejectRequest(requestId: String) {
        launchWithErrorHandler {
            rejectRequestUseCase(requestId)
            removeFriendRequest(requestId)
            refreshData()
            searchFriends("") // Refresh search results to update statuses
        }
    }

    fun deleteFriend(friendId: String) {
        launchWithErrorHandler {
            deleteFriendUseCase(friendId)
            _friends.value = _friends.value.filter { it.userId != friendId }
        }
    }

    private fun refreshData() {
        launchWithErrorHandler {
            _friends.value = getFriendsUseCase()
            _friendRequests.value = getFriendRequestsUseCase()
        }
    }

    private fun removeFriendRequest(requestId: String) {
        _friendRequests.value = _friendRequests.value.filter { it.requestId != requestId }
    }


    private fun launchWithErrorHandler(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
