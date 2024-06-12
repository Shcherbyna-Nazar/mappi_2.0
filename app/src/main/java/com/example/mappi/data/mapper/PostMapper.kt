package com.example.mappi.data.mapper

import com.example.mappi.data.datasource.remote.dto.PostDto
import com.example.mappi.domain.model.Post

object PostMapper {
    fun mapToDomain(postDto: PostDto): Post {
        return Post(
            url = postDto.url,
            latitude = postDto.latitude,
            longitude = postDto.longitude
        )
    }
}