package com.numplates.nomera3.modules.remotestyle.presentation.formatter

import android.content.Context
import androidx.core.view.isVisible
import com.numplates.nomera3.databinding.FragmentAddMultipleMediaPostBinding
import com.numplates.nomera3.databinding.FragmentAddPostBinding
import com.numplates.nomera3.databinding.MeeraCreatePostFragmentBinding
import com.numplates.nomera3.databinding.MeeraFragmentAddPostBinding
import com.numplates.nomera3.modules.appInfo.data.entity.Settings
import com.numplates.nomera3.modules.remotestyle.data.posts.PostOnlyTextRemoteStyle
import com.numplates.nomera3.modules.remotestyle.presentation.applyStyle

/**
 * Утилита для форматирования поста при создании
 */
class NewPostMultipleMediaFormatter(
    private val context: Context,
    private val binding: FragmentAddMultipleMediaPostBinding,
    private val settings: Settings
) {
    private val newPostView = object : PostOnlyTextFormatter.PostOnlyTextRemoteStyleView {
        override fun bindStyle(style: PostOnlyTextRemoteStyle.Style) {
            binding.etWrite.applyStyle(context, false, style)
        }

        override fun canApplyOnlyTextStyle(): Boolean {
            return !binding.addPostMediaAttachmentViewPager.hasAttachments() && binding.flMusicContainer.isVisible.not()
        }

        override fun getTextLength(): Int {
            return binding.etWrite.length()
        }

        override fun getLinesCount(): Int {
            return binding.etWrite.lineCount
        }
    }

    private val onlyTextFormatter = PostOnlyTextFormatter.createNullable(
        rules = settings.postRules?.postOnlyTextTextStyle?.rules,
        styles = settings.postRules?.postOnlyTextTextStyle?.styles
    )

    fun onPostChanged() {
        onlyTextFormatter?.apply(newPostView)
    }

    companion object {
        fun createNullable(
            context: Context?,
            binding: FragmentAddMultipleMediaPostBinding?,
            settings: Settings?
        ): NewPostMultipleMediaFormatter? {
            val contextNonNull = context ?: return null
            val bindingNonNull = binding ?: return null
            val settingsNonNull = settings ?: return null

            return NewPostMultipleMediaFormatter(contextNonNull, bindingNonNull, settingsNonNull)
        }
    }
}

class NewPostFormatter(
    private val context: Context,
    private val binding: FragmentAddPostBinding,
    private val settings: Settings
) {
    private val newPostView = object : PostOnlyTextFormatter.PostOnlyTextRemoteStyleView {
        override fun bindStyle(style: PostOnlyTextRemoteStyle.Style) {
            binding.etWrite.applyStyle(context, false, style)
        }

        override fun canApplyOnlyTextStyle(): Boolean {
            return binding.apaiAttachment.isVisible.not() &&
                binding.flMusicContainer.isVisible.not()
        }

        override fun getTextLength(): Int {
            return binding.etWrite.length()
        }

        override fun getLinesCount(): Int {
            return binding.etWrite.lineCount
        }
    }

    private val onlyTextFormatter = PostOnlyTextFormatter.createNullable(
        rules = settings.postRules?.postOnlyTextTextStyle?.rules,
        styles = settings.postRules?.postOnlyTextTextStyle?.styles
    )

    fun onPostChanged() {
        onlyTextFormatter?.apply(newPostView)
    }

    companion object {
        fun createNullable(
            context: Context?,
            binding: FragmentAddPostBinding?,
            settings: Settings?
        ): NewPostFormatter? {
            val contextNonNull = context ?: return null
            val bindingNonNull = binding ?: return null
            val settingsNonNull = settings ?: return null

            return NewPostFormatter(contextNonNull, bindingNonNull, settingsNonNull)
        }
    }
}

class MeeraNewPostFormatter(
    private val context: Context,
    private val binding: MeeraFragmentAddPostBinding,
    private val settings: Settings
) {
    private val newPostView = object : PostOnlyTextFormatter.PostOnlyTextRemoteStyleView {
        override fun bindStyle(style: PostOnlyTextRemoteStyle.Style) {
            binding.etWrite.applyStyle(context, false, style)
        }

        override fun canApplyOnlyTextStyle(): Boolean {
            return binding.apaiAttachment.isVisible.not() &&
                binding.flMusicContainer.isVisible.not()
        }

        override fun getTextLength(): Int {
            return binding.etWrite.length()
        }

        override fun getLinesCount(): Int {
            return binding.etWrite.lineCount
        }
    }

    private val onlyTextFormatter = PostOnlyTextFormatter.createNullable(
        rules = settings.postRules?.postOnlyTextTextStyle?.rules,
        styles = settings.postRules?.postOnlyTextTextStyle?.styles
    )

    fun onPostChanged() {
        onlyTextFormatter?.apply(newPostView)
    }

    companion object {

        fun createNullable(
            context: Context?,
            binding: MeeraFragmentAddPostBinding?,
            settings: Settings?
        ): MeeraNewPostFormatter? {
            val contextNonNull = context ?: return null
            val bindingNonNull = binding ?: return null
            val settingsNonNull = settings ?: return null
            return MeeraNewPostFormatter(contextNonNull, bindingNonNull, settingsNonNull)
        }
    }
}

class MeeraMultipleMediaPostFormatter(
    private val context: Context,
    private val binding: MeeraCreatePostFragmentBinding,
    settings: Settings
) {
    private val newPostView = object : PostOnlyTextFormatter.PostOnlyTextRemoteStyleView {
        override fun bindStyle(style: PostOnlyTextRemoteStyle.Style) {
            binding.etWrite.applyStyle(context, false, style)
        }

        override fun canApplyOnlyTextStyle(): Boolean {
            return !binding.addPostMediaAttachmentViewPager.hasAttachments() && binding.flMusicContainer.isVisible.not()
        }

        override fun getTextLength(): Int {
            return binding.etWrite.length()
        }

        override fun getLinesCount(): Int {
            return binding.etWrite.lineCount
        }
    }

    private val onlyTextFormatter = PostOnlyTextFormatter.createNullable(
        rules = settings.postRules?.postOnlyTextTextStyle?.rules,
        styles = settings.postRules?.postOnlyTextTextStyle?.styles
    )

    fun onPostChanged() {
        onlyTextFormatter?.apply(newPostView)
    }

    companion object {
        fun createNullable(
            context: Context?,
            binding: MeeraCreatePostFragmentBinding?,
            settings: Settings?
        ): MeeraMultipleMediaPostFormatter? {
            val contextNonNull = context ?: return null
            val bindingNonNull = binding ?: return null
            val settingsNonNull = settings ?: return null

            return MeeraMultipleMediaPostFormatter(contextNonNull, bindingNonNull, settingsNonNull)
        }
    }
}
