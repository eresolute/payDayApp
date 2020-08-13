package com.fh.payday.views2.emiratesidscanner


import android.os.Bundle
import android.support.annotation.DrawableRes
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fh.payday.R
import kotlinx.android.synthetic.main.fragment_emirates_id_scanner_overlay.view.*

/**
 * A simple [Fragment] subclass.
 */
class EmiratesIdScannerOverlayFragment : Fragment() {

    private val defaultRes = R.drawable.camera_frame

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_emirates_id_scanner_overlay, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.frame.setImageResource(arguments?.getInt("res", defaultRes) ?: defaultRes)

        val mode = arguments?.getInt("mode")

        val description = if (mode == EmiratesIdScannerActivity.MODE_CAPTURE) {
            when (val type = arguments?.getInt("type", -1)) {
                TYPE_FRONT -> getString(R.string.capture_emirates_id_front)
                TYPE_BACK -> getString(R.string.capture_emirates_id_back)
                else -> throw IllegalStateException("Unexpected type $type")
            }
        } else {
            when (val type = arguments?.getInt("type", -1)) {
                TYPE_FRONT -> getString(R.string.capture_front_emirates_id)
                TYPE_BACK -> getString(R.string.capture_back_emirates_id)
                else -> throw IllegalStateException("Unexpected type $type")
            }
        }

        view.tv_instructions.text = description
    }

    companion object {
        fun newInstance(
            type: Int,
            mode: Int,
            @DrawableRes res: Int = R.drawable.camera_frame
        ): EmiratesIdScannerOverlayFragment {
            return EmiratesIdScannerOverlayFragment().apply {
                val bundle = Bundle()
                bundle.putInt("res", res)
                bundle.putInt("type", type)
                bundle.putInt("mode", mode)
                arguments = bundle
            }
        }

        const val TYPE_FRONT = 1
        const val TYPE_BACK = 2
    }

}
