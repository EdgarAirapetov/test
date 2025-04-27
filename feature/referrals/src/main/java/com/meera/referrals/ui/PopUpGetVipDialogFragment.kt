package com.meera.referrals.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.jakewharton.rxbinding2.widget.RxTextView
import com.meera.core.di.CoreComponentProvider
import com.meera.core.extensions.clearText
import com.meera.core.extensions.clickAnimate
import com.meera.core.extensions.showKeyboard
import com.meera.core.extensions.visible
import com.meera.referrals.R
import com.meera.referrals.di.DaggerReferralFeatureComponent
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class PopUpGetVipDialogFragment : DialogFragment() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    private val viewModel by viewModels<ReferralViewModel> { factory }

    var successCheckCodeCallback: (code: String) -> Unit = {}
    var onDismissListener: () -> Unit = {}

    private val disposables = CompositeDisposable()

    private lateinit var dialog: AlertDialog.Builder
    private lateinit var alertDialog: AlertDialog
    private lateinit var wrongCodeCallback: () -> Unit
    private lateinit var rightCodeCallback: () -> Unit

    private lateinit var skipBtn: TextView
    private lateinit var activateBtn: Button
    private lateinit var codeText: EditText
    private lateinit var errorMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initDaggerInjection()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialog = AlertDialog.Builder(context)
        val mView = LayoutInflater.from(context).inflate(R.layout.dialog_pop_up_get_vip, null)
        dialog.setView(mView)
        initView(mView)
        initListeners()
        alertDialog = dialog.create()
        return alertDialog
    }

    override fun onResume() {
        super.onResume()
        initEditCodeObservable()
        initLiveObservables()
    }

    private fun initDaggerInjection() {
        DaggerReferralFeatureComponent
            .factory()
            .create((activity?.application as CoreComponentProvider).getCoreComponent())
            .inject(this)
    }

    private fun initView(v: View) {
        skipBtn = v.findViewById(R.id.pop_up_skip_btn)
        activateBtn = v.findViewById(R.id.pop_up_activate_code_btn)
        codeText = v.findViewById(R.id.input_sms_code)
        errorMessage = v.findViewById(R.id.pop_up_vip_error_message)
        codeText.requestFocus()
        codeText.showKeyboard()
    }

    private fun initEditCodeObservable(){
        disposables.add(
            RxTextView.textChanges(codeText)
                .subscribe({ text ->
                    activateBtn.isEnabled = text.isNotEmpty()
                }, { Log.e(this.javaClass.simpleName, it.toString())})
        )
    }

    private fun initListeners(){
        skipBtn.setOnClickListener{alertDialog.dismiss()}
        activateBtn.setOnClickListener{
            it.clickAnimate()
            trySend()
        }
        codeText.setOnClickListener{}
    }

    private fun trySend() {
        viewModel.checkReferralCode(codeText.text.toString())
    }

    private val eventObserver = Observer<ReferralViewEvent> { viewEvent ->
        when(viewEvent){
            is ReferralViewEvent.OnSuccessCheckCode -> { successCheckCodeCallback.invoke(codeText.text.toString()) }
            is ReferralViewEvent.OnFailCheckCode -> { showErrorWrongCode(viewEvent.errorMessage) }
            else -> {}
        }
    }

    private fun initLiveObservables(){
        viewModel.liveReferralDataViewEvent.observeForever(eventObserver)
    }

    private fun showErrorWrongCode(serverErrorText: String?){
        if (serverErrorText != null) {
            errorMessage.text = serverErrorText
        } else {
            errorMessage.text = getString(R.string.error_try_later)
        }
        errorMessage.visible()
        codeText.clearText()
        context?.let {
            codeText.background = ContextCompat.getDrawable(it, R.drawable.white_rounded_shape35_red_stroke)
        }

    }

    override fun onStop() {
        super.onStop()
        disposables.clear()
        viewModel.liveReferralDataViewEvent.removeObserver(eventObserver)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener.invoke()
    }

    fun setOnRightCodeListener(callback: () -> Unit){
        rightCodeCallback = callback
    }

    fun setOnWrongCodeListener(callback: () -> Unit){
        wrongCodeCallback = callback
    }

}
