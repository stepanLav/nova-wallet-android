package io.novafoundation.nova.feature_assets.di

import dagger.BindsInstance
import dagger.Component
import io.novafoundation.nova.common.di.CommonApi
import io.novafoundation.nova.common.di.scope.FeatureScope
import io.novafoundation.nova.core_db.di.DbApi
import io.novafoundation.nova.feature_account_api.di.AccountFeatureApi
import io.novafoundation.nova.feature_account_api.presenatation.account.wallet.list.SelectAddressCommunicator
import io.novafoundation.nova.feature_assets.presentation.AssetsRouter
import io.novafoundation.nova.feature_assets.presentation.balance.detail.di.BalanceDetailComponent
import io.novafoundation.nova.feature_assets.presentation.balance.filters.di.AssetFiltersComponent
import io.novafoundation.nova.feature_assets.presentation.balance.list.di.BalanceListComponent
import io.novafoundation.nova.feature_assets.presentation.balance.list.view.GoToNftsView
import io.novafoundation.nova.feature_assets.presentation.balance.search.di.AssetSearchComponent
import io.novafoundation.nova.feature_assets.presentation.buy.flow.di.AssetBuyFlowComponent
import io.novafoundation.nova.feature_assets.presentation.receive.di.ReceiveComponent
import io.novafoundation.nova.feature_assets.presentation.receive.flow.di.AssetReceiveFlowComponent
import io.novafoundation.nova.feature_assets.presentation.send.amount.di.SelectSendComponent
import io.novafoundation.nova.feature_assets.presentation.send.confirm.di.ConfirmSendComponent
import io.novafoundation.nova.feature_assets.presentation.send.flow.di.AssetSendFlowComponent
import io.novafoundation.nova.feature_assets.presentation.tokens.add.enterInfo.di.AddTokenEnterInfoComponent
import io.novafoundation.nova.feature_assets.presentation.tokens.add.selectChain.di.AddTokenSelectChainComponent
import io.novafoundation.nova.feature_assets.presentation.tokens.manage.chain.di.ManageChainTokensComponent
import io.novafoundation.nova.feature_assets.presentation.tokens.manage.di.ManageTokensComponent
import io.novafoundation.nova.feature_assets.presentation.transaction.detail.di.ExtrinsicDetailComponent
import io.novafoundation.nova.feature_assets.presentation.transaction.detail.di.RewardDetailComponent
import io.novafoundation.nova.feature_assets.presentation.transaction.detail.di.TransactionDetailComponent
import io.novafoundation.nova.feature_assets.presentation.transaction.filter.di.TransactionHistoryFilterComponent
import io.novafoundation.nova.feature_crowdloan_api.di.CrowdloanFeatureApi
import io.novafoundation.nova.feature_currency_api.di.CurrencyFeatureApi
import io.novafoundation.nova.feature_nft_api.NftFeatureApi
import io.novafoundation.nova.feature_wallet_api.di.WalletFeatureApi
import io.novafoundation.nova.feature_wallet_connect_api.di.WalletConnectFeatureApi
import io.novafoundation.nova.runtime.di.RuntimeApi
import io.novafoundation.nova.web3names.di.Web3NamesApi

@Component(
    dependencies = [
        AssetsFeatureDependencies::class
    ],
    modules = [
        AssetsFeatureModule::class,
    ]
)
@FeatureScope
interface AssetsFeatureComponent : AssetsFeatureApi {

    fun balanceListComponentFactory(): BalanceListComponent.Factory

    fun balanceDetailComponentFactory(): BalanceDetailComponent.Factory

    fun chooseAmountComponentFactory(): SelectSendComponent.Factory

    fun confirmTransferComponentFactory(): ConfirmSendComponent.Factory

    fun transactionDetailComponentFactory(): TransactionDetailComponent.Factory

    fun transactionHistoryComponentFactory(): TransactionHistoryFilterComponent.Factory

    fun rewardDetailComponentFactory(): RewardDetailComponent.Factory

    fun extrinsicDetailComponentFactory(): ExtrinsicDetailComponent.Factory

    fun receiveComponentFactory(): ReceiveComponent.Factory

    fun assetFiltersComponentFactory(): AssetFiltersComponent.Factory

    fun assetSearchComponentFactory(): AssetSearchComponent.Factory

    fun manageTokensComponentFactory(): ManageTokensComponent.Factory

    fun manageChainTokensComponentFactory(): ManageChainTokensComponent.Factory

    fun addTokenSelectChainComponentFactory(): AddTokenSelectChainComponent.Factory

    fun addTokenEnterInfoComponentFactory(): AddTokenEnterInfoComponent.Factory

    fun sendFlowComponent(): AssetSendFlowComponent.Factory

    fun receiveFlowComponent(): AssetReceiveFlowComponent.Factory

    fun buyFlowComponent(): AssetBuyFlowComponent.Factory

    fun inject(view: GoToNftsView)

    @Component.Factory
    interface Factory {

        fun create(
            @BindsInstance accountRouter: AssetsRouter,
            @BindsInstance selectAddressCommunicator: SelectAddressCommunicator,
            deps: AssetsFeatureDependencies
        ): AssetsFeatureComponent
    }

    @Component(
        dependencies = [
            CommonApi::class,
            DbApi::class,
            RuntimeApi::class,
            NftFeatureApi::class,
            WalletFeatureApi::class,
            AccountFeatureApi::class,
            CurrencyFeatureApi::class,
            CrowdloanFeatureApi::class,
            Web3NamesApi::class,
            WalletConnectFeatureApi::class
        ]
    )
    interface AssetsFeatureDependenciesComponent : AssetsFeatureDependencies
}
