package com.example.mappi.util.nearby

import com.example.mappi.domain.model.Place

data class NearbySearchResponseData(
    val places: List<Place>,
    val nextPageToken: String?
    )