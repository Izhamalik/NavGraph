package com.izharmalik.taskapp.kotlin.api

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Photos(
    val albumId: Int,
    val id: Int,
    val title: String,
    val url: String,
    val thumbnailUrl: String
)  : Parcelable

