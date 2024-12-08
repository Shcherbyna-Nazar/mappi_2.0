package com.example.mappi.data.mapper

import com.example.mappi.data.datasource.remote.dto.PostDto
import com.example.mappi.domain.model.Post

object PostMapper {
    fun mapToDomain(postDto: PostDto): Post {
        return Post(
            id = postDto.id,
            url = postDto.url,
            latitude = postDto.latitude,
            longitude = postDto.longitude,
            userName = postDto.userName,
            rating = postDto.rating,
            comments = postDto.comments.map { commentDto ->
                CommentMapper.mapToDomain(commentDto)
            }
        )
    }
}