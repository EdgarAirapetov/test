package com.numplates.nomera3.presentation.view.widgets.gallery.fullscreen

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.PhotoModel
import com.numplates.nomera3.presentation.model.IImageData
import com.numplates.nomera3.presentation.view.adapter.GalleryPreviewRecyclerAdapter
import com.numplates.nomera3.presentation.view.adapter.InfinityAdapter
import com.numplates.nomera3.presentation.view.callback.GalleryPreviewCallback
import com.numplates.nomera3.presentation.view.ui.StartSnapHelper
import com.viewpagerindicator.PageIndicator

class ImageRecyclerPagerIndicator
@JvmOverloads
constructor(context: Context,
            attrs: AttributeSet? = null,
            defStyle: Int = 0) : RelativeLayout(context, attrs, defStyle), PageIndicator {

    private var rvGallery: ExtendedRecyclerView

    private var visibleAmount = 5
    private var itemWidth = 0
    var adapter: GalleryPreviewRecyclerAdapter? = null
    var layoutManager: LinearLayoutManager? = null
    private var mViewPager: ViewPager? = null
    private var mListener: OnPageChangeListener? = null
    private var selectable = false
    private var spacing = 0

    init {
        val typedArray = context.theme.obtainStyledAttributes(
                attrs, R.styleable.FaceBarView, 0, 0)
        try {
            visibleAmount = typedArray.getInt(
                    R.styleable.FaceBarView_items_per_screen, visibleAmount)
            selectable = typedArray.getBoolean(
                    R.styleable.FaceBarView_selectable, selectable)
            spacing = typedArray.getDimensionPixelSize(
                    R.styleable.FaceBarView_spacing, spacing)
        } finally {
            typedArray.recycle()
        }
        (getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)
                as LayoutInflater).inflate(R.layout.recycler_pager_indicator, this)

        rvGallery = findViewById(R.id.rvGallery)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight
        itemWidth = widthSize / visibleAmount + paddingTop + paddingBottom
        val widthSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY)
        val heightSpec = MeasureSpec.makeMeasureSpec(itemWidth, MeasureSpec.EXACTLY)
        if (adapter != null) {
            adapter?.setItemWidth(itemWidth)
        }
        rvGallery.itemHeight = itemWidth
        super.onMeasure(widthSpec, heightSpec)
    }

    override fun onPageScrollStateChanged(arg0: Int) {
        if (mListener != null) {
            mListener?.onPageScrollStateChanged(arg0)
        }
    }

    override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {
        if (mListener != null) {
            mListener?.onPageScrolled(arg0, arg1, arg2)
        }
    }

    override fun onPageSelected(arg0: Int) {
        setCurrentItem(arg0)
        if (mListener != null) {
            mListener?.onPageSelected(arg0)
        }
    }

    fun initPosition(arg0: Int) {
        setInitialPosition(arg0)
        if (mListener != null) {
            mListener?.onPageSelected(arg0)
        }
    }

    fun getSelectedPosition(position: Int): Int {
        return adapter!!.getSelectedPosition(position)
    }

    override fun setViewPager(view: ViewPager) {
        if (mViewPager === view) {
            return
        }
        if (mViewPager != null) {
            mViewPager?.setOnPageChangeListener(null)
        }
        val adapter = view.adapter ?: error("ViewPager does not have adapter instance.")
        mViewPager = view
        view.setOnPageChangeListener(this)
        if (adapter.count == 1) {
            rvGallery.visibility = GONE //todo changed here
        } else if (adapter.count < visibleAmount) {
            val params = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.addRule(CENTER_HORIZONTAL, 0)
            rvGallery.layoutParams = params
        }
        initAdapter()
    }

    private fun initAdapter() {
        val iconAdapter = mViewPager?.adapter as InfinityAdapter?
        rvGallery.itemQuantityOnScreen = visibleAmount
        layoutManager = GalleryPreviewLayoutManager(context, RecyclerView.HORIZONTAL, false, rvGallery)
        adapter = GalleryPreviewRecyclerAdapter.Builder<PhotoModel>(context)
                .callback(object : GalleryPreviewCallback() {
                    override fun onClick(holder: RecyclerView.ViewHolder) {
                        onPageSelected(holder.adapterPosition)
                    }
                })
                .visibleAmount(visibleAmount)
                .selectable(selectable)
                .itemWidth(100)
                .data(iconAdapter?.gallery)
                .build()
        rvGallery.adapter = adapter
        rvGallery.setHasFixedSize(true)
        rvGallery.layoutManager = layoutManager
        val snapHelper: SnapHelper = StartSnapHelper()
        snapHelper.attachToRecyclerView(rvGallery)
    }

    fun init(gallery: List<IImageData?>?) {
        rvGallery.itemQuantityOnScreen = visibleAmount
        layoutManager = GalleryPreviewLayoutManager(context, RecyclerView.HORIZONTAL, false, rvGallery)
        adapter = GalleryPreviewRecyclerAdapter.Builder<PhotoModel>(context)
                .callback(object : GalleryPreviewCallback() {
                    override fun onClick(holder: RecyclerView.ViewHolder) {
                        onPageSelected(holder.adapterPosition)
                    }
                })
                .visibleAmount(visibleAmount)
                .itemWidth(100)
                .data(gallery)
                .build()
        rvGallery.adapter = adapter
        rvGallery.setHasFixedSize(true)
        rvGallery.layoutManager = layoutManager
        val snapHelper: SnapHelper = StartSnapHelper()
        snapHelper.attachToRecyclerView(rvGallery)
    }
    override fun notifyDataSetChanged() = Unit
    override fun setViewPager(view: ViewPager, initialPosition: Int) {
        setViewPager(view)
        setInitialPosition(initialPosition)
    }
    private fun setInitialPosition(item: Int) {
        if (mViewPager != null) {
            mViewPager?.currentItem = item
        }
        adapter?.setSelected(item)
        rvGallery.scrollToPosition(item)
    }

    override fun setCurrentItem(item: Int) {
        if (mViewPager != null) {
            mViewPager?.currentItem = item
        }
        adapter?.setSelected(item)
        rvGallery.scrollToPosition(item)
    }

    override fun setOnPageChangeListener(listener: OnPageChangeListener) {
        mListener = listener
    }

    fun setGone() {
        rvGallery.visibility = GONE
    }
}
