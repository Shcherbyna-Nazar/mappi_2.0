package com.example.mappi.domain.use_case.friends

import com.example.mappi.domain.repository.UserRepository
import javax.inject.Inject

class DeleteFriendUseCase @Inject constructor(
    private val friendsRepository: UserRepository
) {
    suspend operator fun invoke(friendId: String) = friendsRepository.deleteFriend(friendId)
}