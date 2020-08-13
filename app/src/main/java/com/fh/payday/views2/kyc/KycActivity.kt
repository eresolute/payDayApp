package com.fh.payday.views2.kyc

import android.Manifest
import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.webkit.URLUtil
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.datasource.models.CustomerSummary
import com.fh.payday.datasource.models.Profile
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.*
import com.fh.payday.utilities.AccessMatrix.Companion.EID_UPDATE
import com.fh.payday.utilities.AccessMatrix.Companion.EMAIL_UPDATE
import com.fh.payday.utilities.AccessMatrix.Companion.MOBILE_NUMBER_UPDATE
import com.fh.payday.utilities.AccessMatrix.Companion.PP_UPDATE
import com.fh.payday.viewmodels.kyc.KycViewModel
import com.fh.payday.views2.dashboard.MainActivity
import com.fh.payday.views2.help.HelpActivity
import com.fh.payday.views2.kyc.update.KycOptionActivity
import com.fh.payday.views2.kyc.update.emergencycontact.EditEmergencyActivity
import com.fh.payday.views2.kyc.update.passport.PassportUpdateActivity
import com.fh.payday.views2.loancalculator.LoanCalculatorActivity
import com.fh.payday.views2.locator.LocatorActivity
import com.fh.payday.views2.message.ContactUsActivity
import com.fh.payday.views2.settings.SettingOptionActivity
import com.fh.payday.views2.settings.fingerprint.FingerprintSettings
import com.fh.payday.views2.shared.activity.SelfieActivity
import com.fh.payday.views2.shared.custom.PermissionsDialog
import kotlinx.android.synthetic.main.activity_kyc.*
import kotlinx.android.synthetic.main.content_kyc.*
import kotlinx.android.synthetic.main.toolbar.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

private const val REQUEST_CODE: Int = 100

class KycActivity : BaseActivity() {
    private lateinit var bottomSheet: BottomSheetDialog
    private lateinit var tvUsername: TextView
    private lateinit var tvValidEmirates: TextView
    private lateinit var tvValidPassport: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvMobile: TextView
    private lateinit var tvPassportNumber: TextView
    private lateinit var tvEmergencyName: TextView
    private lateinit var tvEmergencyRelation: TextView
    private lateinit var tvEmergencyMobile: TextView
    private lateinit var tvDob: TextView
    private lateinit var tvChangePassword: TextView
    private lateinit var tvUpdateBiometricAuth: TextView
    private lateinit var viewModel: KycViewModel
    private lateinit var selfie: File
    private lateinit var progressBar: ProgressBar
    private lateinit var ivProfilePic: ImageView
    private var summary: CustomerSummary? = null
    private var cardStatus: String? = null
    private var isExpired = false


    companion object {
        const val SELECT_IMAGE = 1
        const val REQUEST_CODE_SELFIE = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            tv_auth.visibility = View.GONE
            tv_update_auth.visibility = View.GONE
        }
        viewModel = ViewModelProviders.of(this).get(KycViewModel::class.java)
        isExpired = intent.getBooleanExtra("isExpired", false)

        if (!intent.hasExtra("summary")) {
            editKycOption(intent.getIntExtra("index", -1))
            finish()
        }

        summary = intent?.getParcelableExtra("summary") ?: return
        if (isNotEmptyList(summary?.cards))
            cardStatus = summary?.cards?.get(0)?.cardStatus
        setPersonalDetail(summary)

        ivProfilePic.isEnabled = false
        addSelfieObserver()
        handleBottomBar()
        addObserver()
    }

    private fun setPersonalDetail(summary: CustomerSummary?) {
        summary ?: return

        summary.loginDate ?: return
        summary.username ?: return

        tvUsername.text = summary.username
        val lastLogin = DateTime.parse(summary.loginDate, "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
                "dd-MM-yyyy hh:mm a")
        tvDob.text = String.format(getString(R.string.last_login), lastLogin)
    }

    override fun onResume() {
        super.onResume()
        getProfile()

    }

    private fun getProfile() {
        hideNoInternetView()
        if (!isNetworkConnected()) {
            scroll_view.visibility = View.GONE
            return showNoInternetView { getProfile() }
        }
        scroll_view.visibility = View.VISIBLE
        val user = UserPreferences.instance.getUser(this)
        if (user != null)
            viewModel.getProfile(user.token, user.sessionId, user.refreshToken, user.customerId.toLong())
    }

    private fun addObserver() {
        viewModel.profileState.observe(this, Observer {
            val customerProfileState = it?.getContentIfNotHandled() ?: return@Observer

            if (customerProfileState is NetworkState2.Loading<Profile>) {
                showProgress(getString(R.string.processing))
                return@Observer
            }
            hideProgress()

            when (customerProfileState) {
                is NetworkState2.Success -> {
                    val profileData = (customerProfileState).data ?: return@Observer
                    setSelfie(profileData.selfie)
                    setProfileDetails(profileData)
                }
                is NetworkState2.Error -> {
                    if (customerProfileState.isSessionExpired)
                        return@Observer onSessionExpired(customerProfileState.message)

                    val (message) = customerProfileState
//                    onError(message)
                    handleErrorCode(customerProfileState.errorCode.toInt(), message)
                }
                is NetworkState2.Failure -> onFailure(findViewById(R.id.layout), customerProfileState.throwable)
                else -> onFailure(findViewById(R.id.layout), CONNECTION_ERROR)
            }
        })
    }

    private fun setProfileDetails(profileData: Profile) {
//        tvUsername.text = profileData.cardName

        if (profileData.emiratesVerified == EMIRATES_REJECTED || profileData.emiratesVerified == EMIRATES_NOT_VERIFIED) {
            tvValidEmirates.setTextColor(ContextCompat.getColor(this, R.color.not_verified))
        } else {
            tvValidEmirates.setTextColor(ContextCompat.getColor(this, R.color.verified))
        }
        tvValidEmirates.text = profileData.emiratesVerified

        if (profileData.passportVerified) {
            tvValidPassport.text = getString(R.string.verified)
            tvValidPassport.setTextColor(ContextCompat.getColor(this, R.color.verified))
        } else {
            tvValidPassport.text = getString(R.string.not_verified)
            tvValidPassport.setTextColor(ContextCompat.getColor(this, R.color.not_verified))
        }
        //tvMobile.addHypen(profileData.mobile)
        tvMobile.replaceZero(profileData.mobile)
        tvEmail.text = profileData.emailId
        tvPassportNumber.text = profileData.passportNo
//        tvDob.text = DateTime.parse(profileData.dob)
        profileData.emergencyContacts ?: return

        tvEmergencyName.text = profileData.emergencyContacts[0].name
        tvEmergencyMobile.text = profileData.emergencyContacts[0].mobileNumber
        tvEmergencyRelation.text = profileData.emergencyContacts[0].relation

    }


    override fun getLayout(): Int {
        return R.layout.activity_kyc
    }

    override fun init() {

        tvUsername = findViewById(R.id.tv_user_name)
        tvValidEmirates = findViewById(R.id.tv_valid_emirates)
        tvValidPassport = findViewById(R.id.tv_valid_passport)
        tvEmail = findViewById(R.id.tv_email_id)
        tvMobile = findViewById(R.id.tv_mobile_number)
        tvEmergencyName = findViewById(R.id.tv_emergency_name)
        tvEmergencyMobile = findViewById(R.id.tv_emergency_contact_no)
        tvEmergencyRelation = findViewById(R.id.tv_relation)
        tvDob = findViewById(R.id.tv_dob)
        progressBar = findViewById(R.id.progress_bar)
        ivProfilePic = findViewById(R.id.ivProfilePic)
        tvChangePassword = findViewById(R.id.tv_change_password)
        tvUpdateBiometricAuth = findViewById(R.id.tv_update_auth)
        tvPassportNumber = findViewById(R.id.tv_upload_passport)



        toolbar_title.visibility = View.GONE
        toolbar_back.setOnClickListener { onBackPressed() }

        findViewById<ImageView>(R.id.iv_edit_email_id).setOnClickListener {
            editKycOption(0)
        }

        findViewById<ImageView>(R.id.iv_edit_mobile_no).setOnClickListener {
            editKycOption(1)
        }

        findViewById<ImageView>(R.id.img_update_passport).setOnClickListener {
            //            showBottomSheet(2)
            if (isEmptyList(summary?.cards) || hasAccess(cardStatus, PP_UPDATE))
                startActivity(Intent(this, PassportUpdateActivity::class.java))
            else showCardStatusError(cardStatus)
        }

        findViewById<TextView>(R.id.tv_upload_emirate_id).setOnClickListener {
            //            showBottomSheet(3)
            editKycOption(3)
        }

        findViewById<ImageView>(R.id.ivProfilePic).setOnClickListener {
            showBottomSheet(4)
        }

        findViewById<ImageView>(R.id.iv_edit_emergency_contact).setOnClickListener {
            val i = Intent(this, EditEmergencyActivity::class.java)
            startActivity(i)
        }

        tvUpdateBiometricAuth.setOnClickListener {
            startActivity(Intent(this, FingerprintSettings::class.java))
        }

        tvChangePassword.setOnClickListener {
            updatePassword()
        }

        findViewById<ImageView>(R.id.iv_fb).setOnClickListener {
            if (URLUtil.isHttpUrl(FH_FB) || URLUtil.isHttpsUrl(FH_FB)) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(FH_FB))
                startActivity(intent)
            }
        }
        findViewById<ImageView>(R.id.iv_instagram).setOnClickListener {
            if (URLUtil.isHttpUrl(FH_INSTAGRAM) || URLUtil.isHttpsUrl(FH_INSTAGRAM)) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(FH_INSTAGRAM))
                startActivity(intent)
            }
        }
        findViewById<ImageView>(R.id.iv_twitter).setOnClickListener {
            if (URLUtil.isHttpUrl(FH_TWITTER) || URLUtil.isHttpsUrl(FH_TWITTER)) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(FH_TWITTER))
                startActivity(intent)
            }
        }
        findViewById<ImageView>(R.id.iv_linkedin).setOnClickListener {
            if (URLUtil.isHttpUrl(FH_LINKEDIN) || URLUtil.isHttpsUrl(FH_LINKEDIN)) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(FH_LINKEDIN))
                startActivity(intent)
            }
        }
    }

    private fun updatePassword() {
        val intent = Intent(this, SettingOptionActivity::class.java)
        intent.putExtra("index", 0)

        if (isExpired) {
            intent.putExtra("isExpired", true)
        }
        startActivity(intent)
    }

    private fun handleBottomBar() {

        findViewById<TextView>(R.id.btm_home).setOnClickListener { view ->
            val i = Intent(view.context, MainActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(i)
        }

        findViewById<TextView>(R.id.btm_menu_branch).setOnClickListener { v ->
            val i = Intent(v.context, LocatorActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(i)
        }

        findViewById<TextView>(R.id.btm_menu_support).setOnClickListener {
            val i = Intent(it.context, ContactUsActivity::class.java)
            startActivity(i)
        }

        findViewById<TextView>(R.id.btm_menu_loan_calc).setOnClickListener {
            val i = Intent(it.context, LoanCalculatorActivity::class.java)
            startActivity(i)
        }

        findViewById<TextView>(R.id.toolbar_help).setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java))
        }
    }

    private fun setSelfie(selfie: String?) {
        val ivProfilePic = findViewById<ImageView>(R.id.ivProfilePic)
        progressBar.visibility = View.VISIBLE
        GlideApp.with(this)
                .load(Uri.parse(BASE_URL + selfie))
                .fitCenter()
                .error(R.drawable.ic_profile_pic)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: com.bumptech.glide.request.target.Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        progressBar.visibility = View.GONE
                        ivProfilePic.isEnabled = true
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any?, target: com.bumptech.glide.request.target.Target<Drawable>?, dataSource: com.bumptech.glide.load.DataSource?, isFirstResource: Boolean): Boolean {
                        progressBar.visibility = View.GONE
                        ivProfilePic.isEnabled = true
                        return false
                    }
                })
                .into(ivProfilePic)
    }


    private fun editKycOption(id: Int) {
        val intent = Intent(this, KycOptionActivity::class.java)
        when (id) {
            0 -> {
                if (isEmptyList(summary?.cards) || hasAccess(cardStatus, EMAIL_UPDATE)) {
                    intent.putExtra("index", 0)
                    startActivity(intent)
                } else showCardStatusError(cardStatus)
            }
            1 -> {
                if (isEmptyList(summary?.cards) || hasAccess(cardStatus, MOBILE_NUMBER_UPDATE)) {
                    intent.putExtra("index", 1)
                    startActivity(intent)
                } else showCardStatusError(cardStatus)
            }
            2 -> {
                intent.putExtra("index", 2)
                startActivity(intent)
            }
            3 -> {
                if (isEmptyList(summary?.cards) || hasAccess(cardStatus, EID_UPDATE)) {
                    intent.putExtra("index", 3)
                    startActivity(intent)
                } else showCardStatusError(cardStatus)
            }
            4 -> {
                startActivityForResult(Intent(this, SelfieActivity::class.java), REQUEST_CODE_SELFIE)
            }
            5 -> {
                updatePassword()
            }
            else -> return
        }
    }


    private fun showBottomSheet(index: Int) {

        val view = LayoutInflater.from(this).inflate(R.layout.layout_upload_document, findViewById(R.id.content), false)
        bottomSheet = BottomSheetDialog(this)
        bottomSheet.setContentView(view)
        bottomSheet.show()
        (view.parent as View).setBackgroundColor(ContextCompat.getColor(this, R.color.transparent))
        bottomSheet.show()

        view.findViewById<TextView>(R.id.tv_camera).setOnClickListener {
            /* val intent = Intent(this, CameraActivity::class.java)
             intent.putExtra("title", "Update Emirates ID")
             intent.putExtra("instruction", "Position Emirates ID in this frame")
             intent.putExtra("button_text", "Scan")
             startActivity(intent)*/
            editKycOption(index)
            bottomSheet.dismiss()
        }
        view.findViewById<TextView>(R.id.tv_gallery).setOnClickListener {
            openGallery(index)
            bottomSheet.dismiss()
        }
        view.findViewById<TextView>(R.id.tv_cancel).setOnClickListener { bottomSheet.dismiss() }
    }

    private fun openGallery(index: Int) {

        when (index) {
            4 -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                  openGallery()
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        PermissionsDialog.Builder()
                            .setTitle(getString(R.string.storage_permission_title))
                            .setDescription(getString(R.string.storage_permission_description))
                            .setNegativeText(getString(R.string.app_settings))
                            .setPositiveText(getString(R.string.not_now))
                            .build()
                            .show(this)
                    } else {
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE)
                    }
                }
            }
            else -> return
        }


    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode) {
            REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery()
                }
            }
        }
    }

    private fun openGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_IMAGE)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SELECT_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    try {
                        val bitmap = decodeUri(this, data.data) ?: return
                        val user = UserPreferences.instance.getUser(this)
                                ?: return
                        if (getFile(bitmap) == null)
                            return
                        if (!isNetworkConnected()) {
                            onFailure(findViewById(R.id.layout), getString(R.string.no_internet_connectivity))
                            return
                        }
                        viewModel.uploadSelfie(user.token, user.sessionId, user.refreshToken, user.customerId.toLong(), getFile(bitmap)!!)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } catch (e: OutOfMemoryError) {
                        e.printStackTrace()
                    }
                }
            }
        } else if (requestCode == REQUEST_CODE_SELFIE) {
            if (resultCode == Activity.RESULT_OK) {

                if (data != null) {
                    try {
                        /*  val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, data.data)
                          Log.e("image", "is$bitmap")*/
                        selfie = data.getSerializableExtra("data") as File
                        val filePath = selfie.path
                        val bitmap = BitmapFactory.decodeFile(filePath)
                        val user = UserPreferences.instance.getUser(this)
                                ?: return
                        if (bitmap == null) return
                        if (!isNetworkConnected()) {
                            onFailure(findViewById(R.id.layout), getString(R.string.no_internet_connectivity))
                            return
                        }
                        viewModel.uploadSelfie(user.token, user.sessionId, user.refreshToken, user.customerId.toLong(), getFile(bitmap)!!)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                bottomSheet.dismiss()
            }
        }
    }

    private fun addSelfieObserver() {
        viewModel.uploadSelfieState.observe(this, Observer {
            it ?: return@Observer

            val state = it.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                progressBar.visibility = View.VISIBLE
                ivProfilePic.isEnabled = false
                return@Observer
            }

            progressBar.visibility = View.GONE
            ivProfilePic.isEnabled = true

            when (state) {
                is NetworkState2.Success -> {
                    if (state.data == null || TextUtils.isEmpty(state.data)) return@Observer
                    setSelfie(state.data)
                    UserPreferences.instance.updateProfilePic(this, state.data)
                }
                is NetworkState2.Error -> {
                    if (state.isSessionExpired)
                        return@Observer onSessionExpired(state.message)
                    handleErrorCode(state.errorCode.toInt(), state.message)
                }
                is NetworkState2.Failure -> {
                    onFailure(findViewById(R.id.layout), state.throwable)
                }
                else -> {
                    onFailure(findViewById(R.id.layout)!!, CONNECTION_ERROR)
                }
            }
        })
    }

    private fun getFile(bitmap: Bitmap, name: String = "image"): File? {
        val filesDir = this.filesDir
        val imageFile = File(filesDir, "$name.jpg")
        val os: OutputStream
        return try {
            os = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, os)
            os.flush()
            os.close()
            imageFile
        } catch (e: Exception) {
            null
        }
    }


}
