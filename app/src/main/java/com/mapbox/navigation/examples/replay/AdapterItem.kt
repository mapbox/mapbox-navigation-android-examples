package com.mapbox.navigation.examples.replay

sealed class AdapterItem

data class ReplayPath(
    val title: String,
    val description: String,
    val path: String,
    val dataSource: ReplayDataSource
) : AdapterItem()

class Header(val title: String) : AdapterItem()
