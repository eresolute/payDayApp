package com.fh.payday.views2.offer.adapter

import android.arch.paging.PagedListAdapter
import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.fh.payday.R
import com.fh.payday.datasource.models.OfferDetail
import com.fh.payday.utilities.BASE_URL
import com.fh.payday.utilities.GlideApp
import com.fh.payday.views2.offer.OfferActivity

class OfferPageAdapter constructor(
        private val mCtx: Context,
        private val onOfferListener: OfferActivity.OnOfferClickListener
) : PagedListAdapter<OfferDetail, OfferPageAdapter.ItemViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(mCtx).inflate(R.layout.item_offer, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = getItem(position)
        item?.let { holder.bindTo(it, onOfferListener) }
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        private val ivOffer: ImageView = itemView.findViewById(R.id.iv_offer)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progress_bar)
        private val tvBanner: TextView = itemView.findViewById(R.id.tv_banner)

        fun bindTo(item: OfferDetail, onOfferListener: OfferActivity.OnOfferClickListener) {

            progressBar.visibility = View.VISIBLE
            tvTitle.text = item.bannerTitle
            GlideApp.with(itemView)
                    .load(Uri.parse(BASE_URL + "/" + item.fileUrl))
                    .error(R.mipmap.payday_card)
                    .fitCenter()
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: com.bumptech.glide.request.target.Target<Drawable>?, isFirstResource: Boolean): Boolean {
                            progressBar.visibility = View.GONE
                            if (item.fileUrl == null){
                                tvBanner.visibility = View.VISIBLE
                                tvBanner.text = item.bannerText
                            } else{
                                tvBanner.visibility = View.GONE
                            }
                            return false
                        }
                        override fun onResourceReady(resource: Drawable?, model: Any?, target: com.bumptech.glide.request.target.Target<Drawable>?, dataSource: com.bumptech.glide.load.DataSource?, isFirstResource: Boolean): Boolean {
                            progressBar.visibility = View.GONE
                            return false
                        }


                    })
                    .into(ivOffer)

            ivOffer.setOnClickListener { onOfferListener.onOfferClick(item.linkTo) }
        }
    }

    companion object {

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<OfferDetail>() {
            override fun areItemsTheSame(oldItem: OfferDetail, newItem: OfferDetail): Boolean {
                return oldItem.Id == newItem.Id
            }

            override fun areContentsTheSame(oldItem: OfferDetail, newItem: OfferDetail): Boolean {
                return oldItem == newItem
            }
        }
    }
}
