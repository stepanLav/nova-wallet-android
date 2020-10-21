package jp.co.soramitsu.feature_wallet_impl.presentation.balance.detail

import android.content.Context
import android.os.Bundle
import androidx.annotation.StringRes
import jp.co.soramitsu.common.view.bottomSheet.BaseFixedListBottomSheet
import jp.co.soramitsu.feature_wallet_impl.R
import jp.co.soramitsu.feature_wallet_impl.presentation.model.AssetModel
import jp.co.soramitsu.feature_wallet_impl.util.format
import java.math.BigDecimal

class FrozenTokensBottomSheet(
    context: Context,
    private val payload: AssetModel
) : BaseFixedListBottomSheet(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val title = context.getString(R.string.wallet_balance_frozen_template, payload.token.displayName)
        setTitle(title)

        addCurrencyItem(R.string.wallet_balance_locked, payload.locked)
        addCurrencyItem(R.string.wallet_balance_bonded, payload.bonded)
        addCurrencyItem(R.string.wallet_balance_reserved, payload.reserved)
        addCurrencyItem(R.string.wallet_balance_redeemable, payload.redeemable)
        addCurrencyItem(R.string.wallet_balance_unbonding, payload.unbonding)
    }

    private fun addCurrencyItem(@StringRes label: Int, value: BigDecimal) {
        addItem(label, value.format())
    }
}