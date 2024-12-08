package com.example.mappi.data.mapper

import com.example.mappi.data.datasource.remote.dto.CommentDto
import com.example.mappi.domain.model.Comment

object CommentMapper {
    fun mapToDomain(commentDto: CommentDto): Comment {
        return Comment(
            text = commentDto.text,
            userName = commentDto.userName,
            timeStamp = commentDto.timestamp,
            ownerId = commentDto.ownerId,
            profilePictureUrl = commentDto.profilePictureUrl
        )
    }

    fun mapToDto(comment: Comment): CommentDto {
        return CommentDto(
            text = comment.text,
            userName = comment.userName,
            timestamp = comment.timeStamp,
            ownerId = comment.ownerId,
            profilePictureUrl = comment.profilePictureUrl
        )
    }

}
