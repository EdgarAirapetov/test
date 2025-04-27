package com.numplates.nomera3.modules.featuretoggles

interface FeatureTogglesContainer {
    val chatMessageEditFeatureToggle: ChatMessageEditFeatureToggle
    val chatGroupComplaintsFeatureToggle: ChatGroupComplaintsFeatureToggle
    val chatStickerSuggestionsFeatureToggle: ChatStickerSuggestionsFeatureToggle
    val chatLastMessageRecognizedText: ChatLastMessageRecognizedText
    val detailedReactionsForPostFeatureToggle: DetailedReactionsForPostFeatureToggle
    val detailedReactionsForCommentsFeatureToggle: DetailedReactionsForCommentsFeatureToggle
    val hiddenAgeAndSexFeatureToggle: HiddenAgeAndSexFeatureToggle
    val avatarCarouselFeatureToggle: AvatarCarouselFeatureToggle
    val mapEventsFeatureToggle: MapEventsFeatureToggle
    val shakeFeatureToggle: ShakeFeatureToggle
    val videoEditorFeatureToggle: VideoEditorResizeFeatureToggle
    val chatSearchFeatureToggle: ChatListSearchFeatureToggle
    val roadVideoMaxDurationFeatureToggle: RoadVideoMaxDurationFeatureToggle
    val postsWithBackgroundFeatureToggle: PostsWithBackgroundsFeatureToggle
    val postMediaPositioningFeatureToggle: PostMediaPositioningFeatureToggle
    val timeOfDayReactionsFeatureToggle: TimeOfDayReactionsFeatureToggle
    val is18plusProfileFeatureToggle: Is18plusProfileFeatureToggle
    val momentsFeatureToggle: MomentsFeatureToggle
    val feedMediaExpandFeatureToggle: FeedMediaExpandFeatureToggle
    val editPostFeatureToggle: EditPostFeatureToggle
    val momentViewsFeatureToggle: MomentViewsFeatureToggle
    val mapFriendsFeatureToggle: MapFriendsFeatureToggle
}

class FeatureTogglesContainerImpl: FeatureTogglesContainer {
    override val chatMessageEditFeatureToggle = ChatMessageEditFeatureToggle()
    override val chatGroupComplaintsFeatureToggle = ChatGroupComplaintsFeatureToggle()
    override val chatStickerSuggestionsFeatureToggle = ChatStickerSuggestionsFeatureToggle()
    override val chatLastMessageRecognizedText = ChatLastMessageRecognizedText()
    override val detailedReactionsForPostFeatureToggle = DetailedReactionsForPostFeatureToggle()
    override val detailedReactionsForCommentsFeatureToggle = DetailedReactionsForCommentsFeatureToggle()
    override val hiddenAgeAndSexFeatureToggle = HiddenAgeAndSexFeatureToggle()
    override val avatarCarouselFeatureToggle = AvatarCarouselFeatureToggle()
    override val mapEventsFeatureToggle = MapEventsFeatureToggle()
    override val shakeFeatureToggle = ShakeFeatureToggle()
    override val chatSearchFeatureToggle = ChatListSearchFeatureToggle()
    override val videoEditorFeatureToggle = VideoEditorResizeFeatureToggle()
    override val roadVideoMaxDurationFeatureToggle = RoadVideoMaxDurationFeatureToggle()
    override val postsWithBackgroundFeatureToggle = PostsWithBackgroundsFeatureToggle()
    override val postMediaPositioningFeatureToggle = PostMediaPositioningFeatureToggle()
    override val timeOfDayReactionsFeatureToggle = TimeOfDayReactionsFeatureToggle()
    override val is18plusProfileFeatureToggle = Is18plusProfileFeatureToggle()
    override val momentsFeatureToggle = MomentsFeatureToggle()
    override val feedMediaExpandFeatureToggle = FeedMediaExpandFeatureToggle()
    override val editPostFeatureToggle = EditPostFeatureToggle()
    override val momentViewsFeatureToggle = MomentViewsFeatureToggle()
    override val mapFriendsFeatureToggle = MapFriendsFeatureToggle()
}
