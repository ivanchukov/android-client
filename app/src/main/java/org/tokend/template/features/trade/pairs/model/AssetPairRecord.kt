package org.tokend.template.features.trade.pairs.model

import com.fasterxml.jackson.databind.ObjectMapper
import org.tokend.sdk.api.generated.resources.AssetPairResource
import org.tokend.template.data.model.RecordWithLogo
import org.tokend.template.features.urlconfig.model.UrlConfig
import org.tokend.template.features.assets.model.Asset
import org.tokend.template.features.assets.model.AssetRecord
import org.tokend.template.features.assets.model.SimpleAsset
import org.tokend.template.util.RecordWithPolicy
import org.tokend.wallet.xdr.AssetPairPolicy
import java.io.Serializable
import java.math.BigDecimal

class AssetPairRecord(
        val base: Asset,
        val quote: Asset,
        val price: BigDecimal,
        override val policy: Int,
        override val logoUrl: String?
) : Serializable, RecordWithPolicy, RecordWithLogo {
    val id = "$base:$quote"

    fun isTradeable(): Boolean {
        return hasPolicy(AssetPairPolicy.TRADEABLE_SECONDARY_MARKET.value)
    }

    override fun equals(other: Any?): Boolean {
        return other is AssetPairRecord && other.id == this.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    companion object {
        @JvmStatic
        fun fromResource(resource: AssetPairResource,
                         urlConfig: UrlConfig?,
                         objectMapper: ObjectMapper): AssetPairRecord {
            return AssetPairRecord(
                    base = SimpleAsset(resource.baseAsset),
                    quote = SimpleAsset(resource.quoteAsset),
                    price = resource.price,
                    policy = resource.policies.value
                            ?: throw IllegalStateException("Asset pair must have a policy"),
                    logoUrl =
                    if (resource.baseAsset.isFilled)
                        AssetRecord.fromResource(resource.baseAsset, urlConfig, objectMapper)
                                .logoUrl
                    else
                        null
            )
        }
    }
}