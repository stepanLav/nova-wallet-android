package io.novafoundation.nova.feature_account_api.presenatation.mixin.selectWallet

import io.novafoundation.nova.common.base.BaseFragment
import io.novafoundation.nova.feature_account_api.R
import io.novafoundation.nova.feature_account_api.view.AccountView

fun BaseFragment<*>.setupSelectWalletMixin(mixin: SelectWalletMixin, view: AccountView) {
    view.setActionIcon(R.drawable.ic_chevron_right)
    view.setShowBackground(true)

    mixin.selectedWalletModelFlow.observe {
        view.setTitle(it.title)
        view.setSubTitle(it.subtitle)
        view.setIcon(it.icon)
    }

    view.setOnClickListener { mixin.walletSelectorClicked() }
}
