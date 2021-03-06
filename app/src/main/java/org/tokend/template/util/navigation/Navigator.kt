package org.tokend.template.util.navigation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.app.Fragment
import android.view.View
import org.jetbrains.anko.clearTop
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask
import org.jetbrains.anko.singleTop
import org.tokend.sdk.keyserver.models.WalletInfo
import org.tokend.template.R
import org.tokend.template.activities.MainActivity
import org.tokend.template.activities.SingleFragmentActivity
import org.tokend.template.features.assets.model.Asset
import org.tokend.template.features.trade.pairs.model.AssetPairRecord
import org.tokend.template.features.assets.model.AssetRecord
import org.tokend.template.features.history.model.BalanceChange
import org.tokend.template.features.history.model.details.BalanceChangeCause
import org.tokend.template.extensions.getBigDecimalExtra
import org.tokend.template.features.assets.buy.BuyWithAtomicSwapActivity
import org.tokend.template.features.assets.buy.view.AtomicSwapAsksFragment
import org.tokend.template.features.assets.details.view.AssetDetailsActivity
import org.tokend.template.features.assets.view.ExploreAssetsFragment
import org.tokend.template.features.changepassword.ChangePasswordActivity
import org.tokend.template.features.deposit.view.DepositAmountActivity
import org.tokend.template.features.deposit.view.DepositFragment
import org.tokend.template.features.fees.view.FeesActivity
import org.tokend.template.features.invest.model.SaleRecord
import org.tokend.template.features.invest.view.InvestmentConfirmationActivity
import org.tokend.template.features.invest.view.SaleActivity
import org.tokend.template.features.invest.view.SaleInvestActivity
import org.tokend.template.features.limits.view.LimitsActivity
import org.tokend.template.features.localaccount.importt.view.ImportLocalAccountActivity
import org.tokend.template.features.localaccount.view.LocalAccountDetailsActivity
import org.tokend.template.features.offers.CreateOfferActivity
import org.tokend.template.features.offers.OfferConfirmationActivity
import org.tokend.template.features.offers.OffersActivity
import org.tokend.template.features.offers.model.OfferRecord
import org.tokend.template.features.offers.model.OfferRequest
import org.tokend.template.features.offers.view.details.PendingInvestmentDetailsActivity
import org.tokend.template.features.offers.view.details.PendingOfferDetailsActivity
import org.tokend.template.features.qr.ShareQrFragment
import org.tokend.template.features.recovery.RecoveryActivity
import org.tokend.template.features.send.PaymentConfirmationActivity
import org.tokend.template.features.send.SendFragment
import org.tokend.template.features.send.model.PaymentRequest
import org.tokend.template.features.signin.AuthenticatorSignInActivity
import org.tokend.template.features.signin.LocalAccountSignInActivity
import org.tokend.template.features.signin.SignInActivity
import org.tokend.template.features.signin.unlock.UnlockAppActivity
import org.tokend.template.features.signup.SignUpActivity
import org.tokend.template.features.trade.TradeActivity
import org.tokend.template.features.history.details.*
import org.tokend.template.features.balances.view.BalanceDetailsActivity
import org.tokend.template.features.withdraw.WithdrawFragment
import org.tokend.template.features.withdraw.WithdrawalConfirmationActivity
import org.tokend.template.features.withdraw.model.WithdrawalRequest
import org.tokend.template.util.IntentLock
import java.math.BigDecimal

/**
 * Performs transitions between screens.
 * 'open-' will open related screen as a child.<p>
 * 'to-' will open related screen and finish current.
 */
class Navigator private constructor() {
    private var activity: Activity? = null
    private var fragment: Fragment? = null
    private var context: Context? = null

    companion object {
        fun from(activity: Activity): Navigator {
            val navigator = Navigator()
            navigator.activity = activity
            navigator.context = activity
            return navigator
        }

        fun from(fragment: Fragment): Navigator {
            val navigator = Navigator()
            navigator.fragment = fragment
            navigator.context = fragment.requireContext()
            return navigator
        }

        fun from(context: Context): Navigator {
            val navigator = Navigator()
            navigator.context = context
            return navigator
        }
    }

    private fun performIntent(intent: Intent?, requestCode: Int? = null, bundle: Bundle? = null) {
        if (intent != null) {
            if (!IntentLock.checkIntent(intent, context)) return
            activity?.let {
                if (requestCode != null) {
                    it.startActivityForResult(intent, requestCode, bundle ?: Bundle.EMPTY)
                } else {
                    it.startActivity(intent, bundle ?: Bundle.EMPTY)
                }
                return
            }

            fragment?.let {
                if (requestCode != null) {
                    it.startActivityForResult(intent, requestCode, bundle ?: Bundle.EMPTY)
                } else {
                    it.startActivity(intent, bundle ?: Bundle.EMPTY)
                }
                return
            }

            context?.startActivity(intent.newTask(), bundle ?: Bundle.EMPTY)
        }
    }

    private fun fadeOut(activity: Activity) {
        ActivityCompat.finishAfterTransition(activity)
        activity.overridePendingTransition(0, R.anim.activity_fade_out)
        activity.finish()
    }

    private fun createTransitionBundle(activity: Activity, vararg pairs: Pair<View?, String>): Bundle {
        val sharedViews = arrayListOf<android.support.v4.util.Pair<View, String>>()

        pairs.forEach {
            val view = it.first
            if (view != null) {
                sharedViews.add(android.support.v4.util.Pair(view, it.second))
            }
        }

        return if (sharedViews.isEmpty()) {
            Bundle.EMPTY
        } else {
            ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
                    *sharedViews.toTypedArray()).toBundle() ?: Bundle.EMPTY
        }
    }

    fun openSignUp() {
        context?.intentFor<SignUpActivity>()
                ?.also { performIntent(it) }
    }

    fun openRecovery(email: String? = null) {
        context?.intentFor<RecoveryActivity>()
                ?.putExtras(RecoveryActivity.getBundle(email))
                ?.also { performIntent(it) }
    }

    fun toSignIn(finishAffinity: Boolean = false) {
        context?.intentFor<SignInActivity>()
                ?.singleTop()
                ?.clearTop()
                ?.also { performIntent(it) }
        activity?.let {
            if (finishAffinity) {
                it.setResult(Activity.RESULT_CANCELED, null)
                ActivityCompat.finishAffinity(it)
            } else {
                it.finish()
            }
        }
    }

    fun toUnlock() {
        context?.intentFor<UnlockAppActivity>()
                ?.singleTop()
                ?.clearTop()
                ?.also { performIntent(it) }
        activity?.finish()
    }

    fun toMainActivity(finishAffinity: Boolean = false) {
        context?.intentFor<MainActivity>()
                ?.also { performIntent(it) }
        activity?.let {
            if (finishAffinity) {
                it.setResult(Activity.RESULT_CANCELED, null)
                ActivityCompat.finishAffinity(it)
            } else {
                fadeOut(it)
            }
        }
    }

    fun openQrShare(title: String,
                    data: String,
                    shareLabel: String,
                    shareText: String? = data,
                    topText: String? = null,
                    bottomText: String? = null
    ) = ActivityRequest.withoutResultData().also { request ->
        context?.intentFor<SingleFragmentActivity>()
                ?.putExtras(SingleFragmentActivity.getBundle(
                        ShareQrFragment.ID,
                        ShareQrFragment.getBundle(data, title, shareLabel, shareText, topText, bottomText)
                ))
                ?.also { performIntent(it, request.code) }
    }

    fun openPasswordChange() = ActivityRequest.withoutResultData().also { request ->
        context?.intentFor<ChangePasswordActivity>()
                ?.also { performIntent(it, request.code) }
    }

    fun openWithdrawalConfirmation(withdrawalRequest: WithdrawalRequest
    ) = ActivityRequest.withoutResultData().also { request ->
        context?.intentFor<WithdrawalConfirmationActivity>()
                ?.putExtras(WithdrawalConfirmationActivity.getBundle(withdrawalRequest))
                ?.also { performIntent(it, request.code) }
    }

    fun openSend(asset: String? = null) = ActivityRequest.withoutResultData().also { request ->
        context?.intentFor<SingleFragmentActivity>()
                ?.putExtras(SingleFragmentActivity.getBundle(
                        SendFragment.ID,
                        SendFragment.getBundle(asset, true)
                ))
                ?.also { performIntent(it, request.code) }
    }

    fun openAssetDetails(asset: AssetRecord,
                         cardView: View? = null
    ) = ActivityRequest.withoutResultData().also { request ->
        val transitionBundle = activity?.let {
            createTransitionBundle(it,
                    cardView to it.getString(R.string.transition_asset_card)
            )
        } ?: fragment?.let {
            createTransitionBundle(it.requireActivity(),
                    cardView to it.getString(R.string.transition_asset_card)
            )
        }
        context?.intentFor<AssetDetailsActivity>()
                ?.putExtras(AssetDetailsActivity.getBundle(asset))
                ?.also { performIntent(it, request.code, transitionBundle) }
    }

    fun openPaymentConfirmation(paymentRequest: PaymentRequest
    ) = ActivityRequest.withoutResultData().also { request ->
        context?.intentFor<PaymentConfirmationActivity>()
                ?.putExtras(PaymentConfirmationActivity.getBundle(paymentRequest))
                ?.also { performIntent(it, request.code) }
    }

    fun openOfferConfirmation(offerRequest: OfferRequest
    ) = ActivityRequest.withoutResultData().also { request ->
        context?.intentFor<OfferConfirmationActivity>()
                ?.putExtras(OfferConfirmationActivity.getBundle(offerRequest))
                ?.also { performIntent(it, request.code) }
    }

    fun openInvestmentConfirmation(investmentRequest: OfferRequest,
                                   displayToReceive: Boolean = true,
                                   saleName: String? = null
    ) = ActivityRequest.withoutResultData().also { request ->
        context?.intentFor<InvestmentConfirmationActivity>()
                ?.putExtras(InvestmentConfirmationActivity
                        .getBundle(investmentRequest, displayToReceive, saleName))
                ?.also { performIntent(it, request.code) }
    }

    fun openPendingOffers(onlyPrimary: Boolean = false
    ) = ActivityRequest.withoutResultData().also { request ->
        context?.intentFor<OffersActivity>()
                ?.putExtras(OffersActivity.getBundle(onlyPrimary))
                ?.also { performIntent(it, request.code) }
    }

    fun openSale(sale: SaleRecord) = ActivityRequest.withoutResultData().also { request ->
        context?.intentFor<SaleActivity>()
                ?.putExtras(SaleActivity.getBundle(sale))
                ?.also { performIntent(it, request.code) }
    }

    fun openAuthenticatorSignIn() = ActivityRequest.withoutResultData().also { request ->
        context?.intentFor<AuthenticatorSignInActivity>()
                ?.also { performIntent(it, request.code) }
    }

    fun openBalanceChangeDetails(change: BalanceChange) {
        val activityClass = when (change.cause) {
            is BalanceChangeCause.AmlAlert -> AmlAlertDetailsActivity::class.java
            is BalanceChangeCause.Investment -> InvestmentDetailsActivity::class.java
            is BalanceChangeCause.MatchedOffer -> OfferMatchDetailsActivity::class.java
            is BalanceChangeCause.Issuance -> IssuanceDetailsActivity::class.java
            is BalanceChangeCause.Payment -> PaymentDetailsActivity::class.java
            is BalanceChangeCause.WithdrawalRequest -> WithdrawalDetailsActivity::class.java
            is BalanceChangeCause.Offer -> {
                openPendingOfferDetails(OfferRecord.fromBalanceChange(change))
                return
            }
            is BalanceChangeCause.AssetPairUpdate -> AssetPairUpdateDetailsActivity::class.java
            else -> DefaultBalanceChangeDetailsActivity::class.java
        }

        Intent(context, activityClass)
                .putExtras(BalanceChangeDetailsActivity.getBundle(change))
                .also { performIntent(it) }
    }

    fun openPendingOfferDetails(offer: OfferRecord
    ) = ActivityRequest.withoutResultData().also { request ->
        val activityClass =
                if (offer.isInvestment)
                    PendingInvestmentDetailsActivity::class.java
                else
                    PendingOfferDetailsActivity::class.java

        Intent(context, activityClass)
                .putExtras(PendingOfferDetailsActivity.getBundle(offer))
                .also { performIntent(it, request.code) }
    }

    fun openTrade(assetPair: AssetPairRecord) {
        context?.intentFor<TradeActivity>()
                ?.putExtras(TradeActivity.getBundle(assetPair))
                ?.also { performIntent(it) }
    }

    fun openCreateOffer(baseAsset: Asset,
                        quoteAsset: Asset,
                        requiredPrice: BigDecimal? = null) {
        context?.intentFor<CreateOfferActivity>()
                ?.putExtras(CreateOfferActivity.getBundle(baseAsset, quoteAsset, requiredPrice))
                ?.also { performIntent(it) }
    }

    fun openLimits() {
        context?.intentFor<LimitsActivity>()
                ?.also { performIntent(it) }
    }

    fun openFees(asset: String? = null, feeType: Int = -1) {
        context?.intentFor<FeesActivity>()
                ?.putExtras(FeesActivity.getBundle(asset, feeType))
                ?.also { performIntent(it) }
    }

    fun openDeposit(asset: String) = ActivityRequest.withoutResultData().also { request ->
        context?.intentFor<SingleFragmentActivity>()
                ?.putExtras(SingleFragmentActivity.getBundle(
                        DepositFragment.ID,
                        DepositFragment.getBundle(asset)
                ))
                ?.also { performIntent(it, request.code) }
    }

    fun openWithdraw(asset: String) = ActivityRequest.withoutResultData().also { request ->
        context?.intentFor<SingleFragmentActivity>()
                ?.putExtras(SingleFragmentActivity.getBundle(
                        WithdrawFragment.ID,
                        WithdrawFragment.getBundle(asset)
                ))
                ?.also { performIntent(it, request.code) }
    }

    fun openInvest(sale: SaleRecord) {
        context?.intentFor<SaleInvestActivity>()
                ?.putExtras(SaleInvestActivity.getBundle(sale))
                ?.also { performIntent(it) }
    }

    fun openAssetsExplorer() {
        context?.intentFor<SingleFragmentActivity>()
                ?.putExtras(SingleFragmentActivity.getBundle(
                        ExploreAssetsFragment.ID,
                        null
                ))
                ?.also { performIntent(it) }
    }

    fun openBalanceDetails(balanceId: String) {
        context?.intentFor<BalanceDetailsActivity>()
                ?.putExtras(BalanceDetailsActivity.getBundle(balanceId))
                ?.also { performIntent(it) }
    }

    fun openAccountQrShare(walletInfo: WalletInfo) {
        openAccountQrShare(walletInfo.accountId)
    }

    private fun openAccountQrShare(accountId: String) {
        openQrShare(
                data = accountId,
                title = context!!.getString(R.string.account_id_title),
                shareLabel = context!!.getString(R.string.share_account_id)
        )
    }

    fun openAtomicSwapBuy(assetCode: String,
                          askId: String) {
        context?.intentFor<BuyWithAtomicSwapActivity>()
                ?.putExtras(BuyWithAtomicSwapActivity.getBundle(assetCode, askId))
                ?.also { performIntent(it) }
    }

    fun openAtomicSwapsAsks(assetCode: String) {
        context?.intentFor<SingleFragmentActivity>()
                ?.putExtras(SingleFragmentActivity.getBundle(
                        AtomicSwapAsksFragment.ID,
                        AtomicSwapAsksFragment.getBundle(assetCode)
                ))
                ?.also { performIntent(it) }
    }

    fun openLocalAccountSignIn() = ActivityRequest.withoutResultData().also { request ->
        context?.intentFor<LocalAccountSignInActivity>()
                ?.also { performIntent(it, request.code) }
    }

    fun openLocalAccountDetails() {
        context?.intentFor<LocalAccountDetailsActivity>()
                ?.also { performIntent(it) }
    }

    fun openLocalAccountImport() {
        context?.intentFor<ImportLocalAccountActivity>()
                ?.also { performIntent(it) }
    }

    fun openDepositAmountInput(assetCode: String) = ActivityRequest { intent ->
        intent?.getBigDecimalExtra(DepositAmountActivity.RESULT_AMOUNT_EXTRA)
                ?.takeIf { it.signum() > 0 }
    }.also { request ->
        context?.intentFor<DepositAmountActivity>()
                ?.putExtras(DepositAmountActivity.getBundle(assetCode))
                ?.also { performIntent(it, request.code) }
    }
}
