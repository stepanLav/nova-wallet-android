package io.novafoundation.nova.feature_wallet_api.data.cache

import io.novafoundation.nova.common.data.network.runtime.binding.AccountInfo
import io.novafoundation.nova.common.data.network.runtime.binding.bindAccountInfo
import io.novafoundation.nova.core_db.model.AssetLocal
import io.novafoundation.nova.feature_wallet_api.data.network.blockhain.types.Balance
import io.novafoundation.nova.runtime.multiNetwork.chain.model.Chain
import jp.co.soramitsu.fearless_utils.runtime.AccountId
import jp.co.soramitsu.fearless_utils.runtime.RuntimeSnapshot

suspend fun AssetCache.updateAsset(
    metaId: Long,
    chainAsset: Chain.Asset,
    accountInfo: AccountInfo,
) = updateAsset(metaId, chainAsset, nativeBalanceUpdater(accountInfo))

suspend fun AssetCache.updateAsset(
    accountId: AccountId,
    chainAsset: Chain.Asset,
    accountInfo: AccountInfo,
) = updateAsset(accountId, chainAsset, nativeBalanceUpdater(accountInfo))

suspend fun AssetCache.updateNonLockableAsset(
    metaId: Long,
    chainAsset: Chain.Asset,
    assetBalance: Balance,
) {
    updateAsset(metaId, chainAsset) {
        it.copy(freeInPlanks = assetBalance, frozenInPlanks = Balance.ZERO, reservedInPlanks = Balance.ZERO)
    }
}

private fun nativeBalanceUpdater(accountInfo: AccountInfo) = { asset: AssetLocal ->
    val data = accountInfo.data

    asset.copy(
        freeInPlanks = data.free,
        frozenInPlanks = data.frozen,
        reservedInPlanks = accountInfo.data.reserved
    )
}

fun bindAccountInfoOrDefault(hex: String?, runtime: RuntimeSnapshot): AccountInfo {
    return hex?.let { bindAccountInfo(it, runtime) } ?: AccountInfo.empty()
}
