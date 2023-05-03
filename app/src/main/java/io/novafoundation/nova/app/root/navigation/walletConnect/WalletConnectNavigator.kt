package io.novafoundation.nova.app.root.navigation.walletConnect

import io.novafoundation.nova.app.R
import io.novafoundation.nova.app.root.navigation.BaseNavigator
import io.novafoundation.nova.app.root.navigation.NavigationHolder
import io.novafoundation.nova.feature_wallet_connect_impl.WalletConnectRouter
import io.novafoundation.nova.feature_wallet_connect_impl.presentation.sessions.details.WalletConnectSessionDetailsFragment
import io.novafoundation.nova.feature_wallet_connect_impl.presentation.sessions.details.WalletConnectSessionDetailsPayload

class WalletConnectNavigator(navigationHolder: NavigationHolder) : BaseNavigator(navigationHolder), WalletConnectRouter {
    override fun openSessionDetails(payload: WalletConnectSessionDetailsPayload) = performNavigation(
        actionId = R.id.action_walletConnectSessionsFragment_to_walletConnectSessionDetailsFragment,
        args = WalletConnectSessionDetailsFragment.getBundle(payload)
    )

    override fun openScanPairingQrCode() = performNavigation(R.id.action_open_scanWalletConnect)
}
