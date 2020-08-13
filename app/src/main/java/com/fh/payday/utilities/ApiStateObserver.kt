package com.fh.payday.utilities

import android.arch.lifecycle.Observer
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.datasource.remote.NetworkState2

abstract class ApiStateObserver<T>(
    private val activity: BaseActivity?
) : Observer<Event<NetworkState2<T>>> {

    override fun onChanged(event: Event<NetworkState2<T>>?) {
        event ?: return
        val state = event.getContentIfNotHandled() ?: return

        if (state is NetworkState2.Loading) {
            return onLoading()
        }

        hideLoading()

        when (state) {
            is NetworkState2.Success -> onSuccess(state)
            is NetworkState2.Error -> onError(state)
            is NetworkState2.Failure -> onFailure(state)
            else -> onFailure(NetworkState2.Failure<T>(Throwable(CONNECTION_ERROR)))
        }
    }

    open fun onLoading() {
        activity?.showProgress(activity.getString(R.string.processing))
    }

    open fun hideLoading() {
        activity?.hideProgress()
    }

    abstract fun onSuccess(state: NetworkState2.Success<T>)

    open fun onError(errorState: NetworkState2.Error<*>) {
        if (errorState.isSessionExpired) {
            return activity?.onSessionExpired(errorState.message) ?: return
        }

        activity?.onError(errorState.message)
    }

    open fun onFailure(failureState: NetworkState2.Failure<*>) {
        val activity = activity ?: return
        activity.onFailure(activity.findViewById(R.id.root_view), failureState.throwable)
    }

}