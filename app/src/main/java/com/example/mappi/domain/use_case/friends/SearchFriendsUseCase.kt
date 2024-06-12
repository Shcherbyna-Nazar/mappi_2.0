package com.example.mappi.domain.use_case.friends

import com.example.mappi.domain.repository.UserRepository
import javax.inject.Inject

class SearchFriendsUseCase @Inject constructor(private val userRepository: UserRepository) {
    suspend operator fun invoke(query: String) = userRepository.searchFriends(query)
}

