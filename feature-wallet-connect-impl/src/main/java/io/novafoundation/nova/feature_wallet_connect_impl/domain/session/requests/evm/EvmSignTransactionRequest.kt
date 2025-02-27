package io.novafoundation.nova.feature_wallet_connect_impl.domain.session.requests.evm

import com.walletconnect.web3.wallet.client.Wallet
import com.walletconnect.web3.wallet.client.Wallet.Params.SessionRequestResponse
import io.novafoundation.nova.feature_external_sign_api.model.ExternalSignCommunicator.Response
import io.novafoundation.nova.feature_external_sign_api.model.signPayload.ExternalSignRequest
import io.novafoundation.nova.feature_external_sign_api.model.signPayload.evm.EvmChainSource
import io.novafoundation.nova.feature_external_sign_api.model.signPayload.evm.EvmSignPayload
import io.novafoundation.nova.feature_external_sign_api.model.signPayload.evm.EvmTransaction
import io.novafoundation.nova.feature_wallet_connect_impl.domain.sdk.approved
import io.novafoundation.nova.feature_wallet_connect_impl.domain.session.requests.SignWalletConnectRequest

class EvmSignTransactionRequest(
    private val transaction: EvmTransaction.Struct,
    private val chainId: Int,
    private val sessionRequest: Wallet.Model.SessionRequest
) : SignWalletConnectRequest(sessionRequest) {

    override suspend fun signedResponse(response: Response.Signed): SessionRequestResponse {
        return sessionRequest.approved(response.signature)
    }

    override fun toExternalSignRequest(): ExternalSignRequest {
        val signPayload = EvmSignPayload.ConfirmTx(
            transaction = transaction,
            originAddress = transaction.from,
            chainSource = EvmChainSource(chainId, EvmChainSource.UnknownChainOptions.MustBeKnown),
            action = EvmSignPayload.ConfirmTx.Action.SIGN
        )

        return ExternalSignRequest.Evm(id, signPayload)
    }
}
