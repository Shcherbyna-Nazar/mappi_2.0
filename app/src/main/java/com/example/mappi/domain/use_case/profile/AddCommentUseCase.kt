package com.example.mappi.domain.use_case.profile

import com.example.mappi.domain.model.Comment
import com.example.mappi.domain.repository.ProfileRepository
import javax.inject.Inject

class AddCommentUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(postId: String, comment: Comment) =
        repository.addComment(postId, comment)
}