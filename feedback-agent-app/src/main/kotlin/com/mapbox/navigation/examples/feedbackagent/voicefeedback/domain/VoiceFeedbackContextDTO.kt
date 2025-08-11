package com.mapbox.navigation.examples.feedbackagent.voicefeedback.domain

import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Context required for Voice Feedback service to process the request.
 *
 * @property userContext required app context
 * @property appContext optional user context
 */
@Serializable
@ExperimentalPreviewMapboxNavigationAPI
internal data class VoiceFeedbackContextDTO(
    @SerialName("user_context")
    val userContext: VoiceFeedbackUserContextDTO,
    @SerialName("app_context")
    val appContext: VoiceFeedbackAppContextDTO? = null,
)

/**
 * @property locale IETF language tag (based on ISO 639), for example "en-US".
 * This locale will be used to influence the language the AI replies in.
 */
@Serializable
@ExperimentalPreviewMapboxNavigationAPI
internal data class VoiceFeedbackAppContextDTO(
    @SerialName("locale")
    val locale: String? = null,
)

/**
 * @property lat Latitude of the current location.
 * @property lon Longitude of the current location.
 * @property placeName The name of the place where the user currently is. For example:
 * - Neighborhood, a colloquial sub-city features often referred to in local parlance.
 * - Place, a cities, villages, municipalities, etc.
 * - Locality, a sub-city features present in countries where such an additional administrative layer is used in
 * postal addressing.
 * - District, smaller than top-level administrative features but typically larger than cities.
 * - Region, a top-level sub-national administrative features, such as states in the United States or provinces in
 * Canada or China.
 * - Country, Generally recognized countries or, in some cases like Hong Kong, an area of quasi-national administrative
 * status that has been given a designated country code under ISO 3166-1.
 *
 * The provided name should be the most granular available to be determined (for example, a Neighborhood should be
 * preferred over Place, if available).
 */
@Serializable
@ExperimentalPreviewMapboxNavigationAPI
internal data class VoiceFeedbackUserContextDTO(
    val lat: String,
    val lon: String,
    @SerialName("place_name")
    val placeName: String,
)
