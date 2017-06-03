package com.conferences.features.speakerdetail

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.support.constraint.ConstraintLayout
import android.support.v4.app.ActivityCompat
import android.util.AttributeSet
import android.view.LayoutInflater
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.conferences.R
import com.conferences.ext.getActivity
import com.conferences.ext.getComponent
import com.conferences.model.Speaker
import com.conferences.utils.DrawableUtils
import kotlinx.android.synthetic.main.view_speaker_detail.view.*
import javax.inject.Inject


class SpeakerDetailView(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) :
        ConstraintLayout(context, attrs, defStyle), SpeakerDetailMvp.View {

    @Inject lateinit var presenter: SpeakerDetailMvp.Presenter

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)

    init {
        context.getComponent<SpeakerDetailComponent>().inject(this)

        LayoutInflater.from(context).inflate(R.layout.view_speaker_detail, this, true)

        var speakerId = ""
        getActivity()?.let {
            speakerId = it.intent.getStringExtra("speaker_id")
        }

        // FIXME: the shared image is transition properly. The start/end locations are off
        //  ViewCompat.setTransitionName(image, "image_$speakerId")
        toolbar.setNavigationOnClickListener {
            if (context is Activity) {
                ActivityCompat.finishAfterTransition(context)
            }
        }

        presenter.onAttach(this)
    }

    override fun onDetachedFromWindow() {
        presenter.onDetach()
        super.onDetachedFromWindow()
    }

    fun setSpeakerId(speakerId: String) {
        presenter.setSpeakerId(speakerId)
    }

    override fun showSpeaker(speaker: Speaker) {
        name.text = speaker.name
        bio.text = speaker.bio

        if (speaker.twitter != null && speaker.twitter!!.isNotEmpty()) {
            twitter.visibility = VISIBLE
            twitter.text = speaker.twitter
            twitter.setCompoundDrawablesWithIntrinsicBounds(
                DrawableUtils.create(context, R.drawable.ic_logo_twitter),
                null,
                null,
                null)

          twitter.setOnClickListener {
              val twitterIntent = Intent(Intent.ACTION_VIEW).apply {
                  data = Uri.parse("https://www.twitter.com/${speaker.twitter?.removePrefix("@")}")
              }
              context.startActivity(twitterIntent)
          }
        } else {
            twitter.visibility = GONE
        }

        if (speaker.github?.isNotEmpty() ?: false) {
            github.visibility = VISIBLE
            github.text = speaker.github
            github.setCompoundDrawablesWithIntrinsicBounds(
                DrawableUtils.create(context, R.drawable.ic_logo_github),
                null,
                null,
                null)

            github.setOnClickListener {
                val githubIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://www.github.com/${speaker.github}")
                }
                context.startActivity(githubIntent)
            }
        }
        else {
            github.visibility = GONE
        }

        Glide.with(context)
                .load(speaker.avatar)
                .asBitmap()
                .centerCrop()
                .dontAnimate()
                .into(object : BitmapImageViewTarget(image) {
                    override fun setResource(resource: Bitmap?) {
                        image.setImageBitmap(resource)
                        ActivityCompat.startPostponedEnterTransition(getActivity())
                    }

                    override fun onLoadFailed(e: Exception?, errorDrawable: Drawable?) {
                        super.onLoadFailed(e, errorDrawable)
                        ActivityCompat.startPostponedEnterTransition(getActivity())
                    }
                })
    }

}