package io.novafoundation.nova.feature_governance_impl.domain.delegation.delegate.list

import io.novafoundation.nova.common.utils.applyFilter
import io.novafoundation.nova.feature_governance_api.data.source.SupportedGovernanceOption
import io.novafoundation.nova.feature_governance_api.domain.delegation.delegate.list.DelegateListInteractor
import io.novafoundation.nova.feature_governance_api.domain.delegation.delegate.list.model.DelegateFiltering
import io.novafoundation.nova.feature_governance_api.domain.delegation.delegate.list.model.DelegatePreview
import io.novafoundation.nova.feature_governance_api.domain.delegation.delegate.list.model.DelegateSorting
import io.novafoundation.nova.feature_governance_api.domain.delegation.delegate.list.model.delegateComparator
import io.novafoundation.nova.feature_governance_api.domain.delegation.delegate.list.model.hasMetadata
import io.novafoundation.nova.feature_governance_impl.data.repository.DelegationBannerRepository
import io.novafoundation.nova.feature_governance_impl.presentation.delegation.delegate.detail.DelegatesSharedComputation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RealDelegateListInteractor(
    private val delegationBannerService: DelegationBannerRepository,
    private val delegatesSharedComputation: DelegatesSharedComputation
) : DelegateListInteractor {

    override fun shouldShowDelegationBanner(): Flow<Boolean> {
        return delegationBannerService.shouldShowBannerFlow()
    }

    override fun hideDelegationBanner() {
        delegationBannerService.hideBanner()
    }

    override suspend fun getDelegates(
        governanceOption: SupportedGovernanceOption,
        scope: CoroutineScope
    ): Flow<List<DelegatePreview>> {
        return delegatesSharedComputation.delegates(governanceOption, scope)
    }

    override suspend fun getUserDelegates(governanceOption: SupportedGovernanceOption, scope: CoroutineScope): Flow<List<DelegatePreview>> {
        return delegatesSharedComputation.delegates(governanceOption, scope).map { delegates ->
            delegates.filter { it.userDelegations.isNotEmpty() }
        }
    }

    override suspend fun applySortingAndFiltering(
        sorting: DelegateSorting,
        filtering: DelegateFiltering,
        delegates: List<DelegatePreview>
    ): List<DelegatePreview> {
        val filteredDelegates = delegates.applyFilter(filtering)
        return applySorting(sorting, filteredDelegates)
    }

    override suspend fun applySorting(
        sorting: DelegateSorting,
        delegates: List<DelegatePreview>
    ): List<DelegatePreview> {
        val comparator = getMetadataComparator()
            .thenComparing(sorting.delegateComparator())

        return delegates.sortedWith(comparator)
    }

    private fun getMetadataComparator(): Comparator<DelegatePreview> {
        return compareByDescending { it.hasMetadata() }
    }
}
