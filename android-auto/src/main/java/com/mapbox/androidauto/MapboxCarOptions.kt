package com.mapbox.androidauto

import com.mapbox.maps.Style
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.MapboxNavigation
import java.util.Locale

/**
 * The top level options for using Mapbox maps and navigation with Android Auto.
 *
 * @param navigationOptions Used to create an instance of [MapboxNavigation]
 * @param mapDayStyle Assigns a day style for the car map
 * @param mapNightStyle Assigns a day style for the car map, when null [mapDayStyle] is used
 * @param directionsLanguage The language used for audio guidance
 */
class MapboxCarOptions private constructor(
    val navigationOptions: NavigationOptions,
    val mapDayStyle: String,
    val mapNightStyle: String?,
    val directionsLanguage: String
) {

    /**
     * Get a builder to customize a subset of current options.
     */
    fun toBuilder(): Builder = Builder(navigationOptions).apply {
        navigationOptions(navigationOptions)
        mapDayStyle(mapDayStyle)
        mapNightStyle(mapNightStyle)
        directionsLanguage(directionsLanguage)
    }

    /**
     * Regenerate whenever a change is made
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MapboxCarOptions

        if (navigationOptions != other.navigationOptions) return false
        if (mapDayStyle != other.mapDayStyle) return false
        if (mapNightStyle != other.mapNightStyle) return false
        if (directionsLanguage != other.directionsLanguage) return false

        return true
    }

    /**
     * Regenerate whenever a change is made
     */
    override fun hashCode(): Int {
        var result = navigationOptions.hashCode()
        result = 31 * result + mapDayStyle.hashCode()
        result = 31 * result + (mapNightStyle?.hashCode() ?: 0)
        result = 31 * result + directionsLanguage.hashCode()
        return result
    }

    /**
     * Regenerate whenever a change is made
     */
    override fun toString(): String {
        return "MapboxCarOptions(navigationOptions=$navigationOptions," +
                " mapDayStyle='$mapDayStyle'," +
                " mapNightStyle=$mapNightStyle," +
                " directionsLanguage='$directionsLanguage'" +
                ")"
    }

    /**
     * Build a new [MapboxCarOptions]
     */
    class Builder(
        private var navigationOptions: NavigationOptions
    ) {
        private var mapDayStyle: String = Style.TRAFFIC_DAY
        private var mapNightStyle: String? = null
        private var directionsLanguage: String = Locale.getDefault().language

        /**
         * Allows you to override the navigation options at runtime.
         * Warning: doing this will not work for all values.
         */
        fun navigationOptions(navigationOptions: NavigationOptions): Builder = apply {
            this.navigationOptions = navigationOptions
        }

        /**
         * Automatically set style for android auto day mode.
         */
        fun mapDayStyle(mapDayStyle: String): Builder = apply {
            this.mapDayStyle = mapDayStyle
        }

        /**
         * Automatically set style for android auto day mode.
         * If this is not set, the [mapDayStyle] is used.
         */
        fun mapNightStyle(mapNightStyle: String?): Builder = apply {
            this.mapNightStyle = mapNightStyle
        }

        /**
         * This is temporary but required at the moment.
         * https://github.com/mapbox/mapbox-navigation-android/issues/4686
         */
        fun directionsLanguage(directionsLanguage: String): Builder = apply {
            this.directionsLanguage = directionsLanguage
        }

        /**
         * Build the [MapboxCarOptions]
         */
        fun build(): MapboxCarOptions {
            return MapboxCarOptions(
                navigationOptions = navigationOptions,
                mapDayStyle = mapDayStyle,
                mapNightStyle = mapNightStyle,
                directionsLanguage = directionsLanguage
            )
        }
    }
}
