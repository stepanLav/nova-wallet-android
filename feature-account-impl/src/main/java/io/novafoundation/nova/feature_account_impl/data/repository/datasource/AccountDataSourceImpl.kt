package io.novafoundation.nova.feature_account_impl.data.repository.datasource

import io.novafoundation.nova.common.data.secrets.v1.SecretStoreV1
import io.novafoundation.nova.common.data.secrets.v2.ChainAccountSecrets
import io.novafoundation.nova.common.data.secrets.v2.KeyPairSchema
import io.novafoundation.nova.common.data.secrets.v2.MetaAccountSecrets
import io.novafoundation.nova.common.data.secrets.v2.SecretStoreV2
import io.novafoundation.nova.common.data.storage.Preferences
import io.novafoundation.nova.common.data.storage.encrypt.EncryptedPreferences
import io.novafoundation.nova.common.utils.inBackground
import io.novafoundation.nova.common.utils.mapList
import io.novafoundation.nova.common.utils.substrateAccountId
import io.novafoundation.nova.core.model.CryptoType
import io.novafoundation.nova.core.model.Language
import io.novafoundation.nova.core.model.Node
import io.novafoundation.nova.core_db.dao.MetaAccountDao
import io.novafoundation.nova.core_db.dao.NodeDao
import io.novafoundation.nova.core_db.model.chain.ChainAccountLocal
import io.novafoundation.nova.core_db.model.chain.MetaAccountLocal
import io.novafoundation.nova.core_db.model.chain.MetaAccountPositionUpdate
import io.novafoundation.nova.feature_account_api.domain.model.Account
import io.novafoundation.nova.feature_account_api.domain.model.AuthType
import io.novafoundation.nova.feature_account_api.domain.model.LightMetaAccount
import io.novafoundation.nova.feature_account_api.domain.model.MetaAccount
import io.novafoundation.nova.feature_account_api.domain.model.MetaAccountAssetBalance
import io.novafoundation.nova.feature_account_api.domain.model.MetaAccountOrdering
import io.novafoundation.nova.feature_account_impl.data.mappers.mapMetaAccountLocalToLightMetaAccount
import io.novafoundation.nova.feature_account_impl.data.mappers.mapMetaAccountLocalToMetaAccount
import io.novafoundation.nova.feature_account_impl.data.mappers.mapMetaAccountWithBalanceFromLocal
import io.novafoundation.nova.feature_account_impl.data.repository.datasource.migration.AccountDataMigration
import io.novafoundation.nova.runtime.ext.accountIdOf
import io.novafoundation.nova.runtime.multiNetwork.ChainRegistry
import io.novafoundation.nova.runtime.multiNetwork.chain.model.Chain
import jp.co.soramitsu.fearless_utils.extensions.asEthereumPublicKey
import jp.co.soramitsu.fearless_utils.extensions.toAccountId
import jp.co.soramitsu.fearless_utils.runtime.AccountId
import jp.co.soramitsu.fearless_utils.scale.EncodableStruct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val PREFS_AUTH_TYPE = "auth_type"
private const val PREFS_PIN_CODE = "pin_code"

class AccountDataSourceImpl(
    private val preferences: Preferences,
    private val encryptedPreferences: EncryptedPreferences,
    private val nodeDao: NodeDao,
    private val metaAccountDao: MetaAccountDao,
    private val chainRegistry: ChainRegistry,
    private val secretStoreV2: SecretStoreV2,
    secretStoreV1: SecretStoreV1,
    accountDataMigration: AccountDataMigration,
) : AccountDataSource, SecretStoreV1 by secretStoreV1 {

    init {
        migrateIfNeeded(accountDataMigration)
    }

    private fun migrateIfNeeded(migration: AccountDataMigration) = async {
        if (migration.migrationNeeded()) {
            migration.migrate(::saveSecuritySource)
        }
    }

    private val selectedMetaAccountLocal = metaAccountDao.selectedMetaAccountInfoFlow()
        .shareIn(GlobalScope, started = SharingStarted.Eagerly, replay = 1)

    private val selectedMetaAccountFlow = selectedMetaAccountLocal
        .filterNotNull()
        .map(::mapMetaAccountLocalToMetaAccount)
        .inBackground()
        .shareIn(GlobalScope, started = SharingStarted.Eagerly, replay = 1)

    override fun getAuthTypeFlow(): Flow<AuthType> {
        return preferences.stringFlow(PREFS_AUTH_TYPE).map { savedValue ->
            if (savedValue == null) {
                AuthType.PINCODE
            } else {
                AuthType.valueOf(savedValue)
            }
        }
    }

    override fun saveAuthType(authType: AuthType) {
        preferences.putString(PREFS_AUTH_TYPE, authType.toString())
    }

    override fun getAuthType(): AuthType {
        val savedValue = preferences.getString(PREFS_AUTH_TYPE)

        return if (savedValue == null) {
            AuthType.PINCODE
        } else {
            AuthType.valueOf(savedValue)
        }
    }

    override suspend fun savePinCode(pinCode: String) = withContext(Dispatchers.IO) {
        encryptedPreferences.putEncryptedString(PREFS_PIN_CODE, pinCode)
    }

    override suspend fun getPinCode(): String? {
        return withContext(Dispatchers.IO) {
            encryptedPreferences.getDecryptedString(PREFS_PIN_CODE)
        }
    }

    override suspend fun saveSelectedNode(node: Node) = withContext(Dispatchers.Default) {
        nodeDao.switchActiveNode(node.id)
    }

    override suspend fun getSelectedNode(): Node? = null

    override suspend fun anyAccountSelected(): Boolean = metaAccountDao.selectedMetaAccount() != null

    override suspend fun saveSelectedAccount(account: Account) = withContext(Dispatchers.Default) {
        // TODO remove compatibility stub
    }

    override suspend fun getSelectedMetaAccount(): MetaAccount {
        return selectedMetaAccountFlow.first()
    }

    override fun selectedMetaAccountFlow(): Flow<MetaAccount> = selectedMetaAccountFlow

    override suspend fun findMetaAccount(accountId: ByteArray): MetaAccount? {
        return metaAccountDao.getMetaAccountInfo(accountId)
            ?.let(::mapMetaAccountLocalToMetaAccount)
    }

    override suspend fun accountNameFor(accountId: AccountId): String? {
        return metaAccountDao.metaAccountNameFor(accountId)
    }

    override suspend fun allMetaAccounts(): List<MetaAccount> {
        return metaAccountDao.getJoinedMetaAccountsInfo().map(::mapMetaAccountLocalToMetaAccount)
    }

    override suspend fun allLightMetaAccounts(): List<LightMetaAccount> {
        return metaAccountDao.getMetaAccounts().map(::mapMetaAccountLocalToLightMetaAccount)
    }

    override fun allMetaAccountsFlow(): Flow<List<MetaAccount>> {
        return metaAccountDao.getJoinedMetaAccountsInfoFlow().map { accountsLocal ->
            accountsLocal.map(::mapMetaAccountLocalToMetaAccount)
        }
    }

    override fun metaAccountsWithBalancesFlow(): Flow<List<MetaAccountAssetBalance>> {
        return metaAccountDao.metaAccountsWithBalanceFlow().mapList(::mapMetaAccountWithBalanceFromLocal)
    }

    override fun metaAccountBalancesFlow(metaId: Long): Flow<List<MetaAccountAssetBalance>> {
        return metaAccountDao.metaAccountWithBalanceFlow(metaId).mapList(::mapMetaAccountWithBalanceFromLocal)
    }

    override suspend fun selectMetaAccount(metaId: Long) {
        metaAccountDao.selectMetaAccount(metaId)
    }

    override suspend fun updateAccountPositions(accountOrdering: List<MetaAccountOrdering>) = withContext(Dispatchers.Default) {
        val positionUpdates = accountOrdering.map {
            MetaAccountPositionUpdate(id = it.id, position = it.position)
        }

        metaAccountDao.updatePositions(positionUpdates)
    }

    override suspend fun getSelectedLanguage(): Language = withContext(Dispatchers.IO) {
        preferences.getCurrentLanguage() ?: throw IllegalArgumentException("No language selected")
    }

    override suspend fun changeSelectedLanguage(language: Language) = withContext(Dispatchers.IO) {
        preferences.saveCurrentLanguage(language.iso)
    }

    override suspend fun accountExists(accountId: AccountId): Boolean {
        return metaAccountDao.isMetaAccountExists(accountId)
    }

    override suspend fun getMetaAccount(metaId: Long): MetaAccount {
        val joinedMetaAccountInfo = metaAccountDao.getJoinedMetaAccountInfo(metaId)

        return mapMetaAccountLocalToMetaAccount(joinedMetaAccountInfo)
    }

    override fun metaAccountFlow(metaId: Long): Flow<MetaAccount> {
        return metaAccountDao.metaAccountInfoFlow(metaId).mapNotNull { local ->
            local?.let(::mapMetaAccountLocalToMetaAccount)
        }
    }

    override suspend fun updateMetaAccountName(metaId: Long, newName: String) {
        metaAccountDao.updateName(metaId, newName)
    }

    override suspend fun deleteMetaAccount(metaId: Long) {
        val joinedMetaAccountInfo = metaAccountDao.getJoinedMetaAccountInfo(metaId)
        val chainAccountIds = joinedMetaAccountInfo.chainAccounts.map(ChainAccountLocal::accountId)

        metaAccountDao.delete(metaId)
        secretStoreV2.clearSecrets(metaId, chainAccountIds)
    }

    override suspend fun insertMetaAccountFromSecrets(
        name: String,
        substrateCryptoType: CryptoType,
        secrets: EncodableStruct<MetaAccountSecrets>
    ) = withContext(Dispatchers.Default) {
        val substratePublicKey = secrets[MetaAccountSecrets.SubstrateKeypair][KeyPairSchema.PublicKey]
        val ethereumPublicKey = secrets[MetaAccountSecrets.EthereumKeypair]?.get(KeyPairSchema.PublicKey)

        val metaAccountLocal = MetaAccountLocal(
            substratePublicKey = substratePublicKey,
            substrateCryptoType = substrateCryptoType,
            substrateAccountId = substratePublicKey.substrateAccountId(),
            ethereumPublicKey = ethereumPublicKey,
            ethereumAddress = ethereumPublicKey?.asEthereumPublicKey()?.toAccountId()?.value,
            name = name,
            isSelected = false,
            position = metaAccountDao.nextAccountPosition(),
            type = MetaAccountLocal.Type.SECRETS
        )

        val metaId = metaAccountDao.insertMetaAccount(metaAccountLocal)
        secretStoreV2.putMetaAccountSecrets(metaId, secrets)

        metaId
    }

    override suspend fun insertChainAccount(
        metaId: Long,
        chain: Chain,
        cryptoType: CryptoType,
        secrets: EncodableStruct<ChainAccountSecrets>
    ) = withContext(Dispatchers.Default) {
        val publicKey = secrets[ChainAccountSecrets.Keypair][KeyPairSchema.PublicKey]
        val accountId = chain.accountIdOf(publicKey)

        val chainAccountLocal = ChainAccountLocal(
            metaId = metaId,
            chainId = chain.id,
            publicKey = publicKey,
            accountId = accountId,
            cryptoType = cryptoType
        )

        metaAccountDao.insertChainAccount(chainAccountLocal)
        secretStoreV2.putChainAccountSecrets(metaId, accountId, secrets)
    }

    private inline fun async(crossinline action: suspend () -> Unit) {
        GlobalScope.launch(Dispatchers.Default) {
            action()
        }
    }
}
