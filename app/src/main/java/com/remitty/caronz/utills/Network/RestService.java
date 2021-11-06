package com.remitty.caronz.utills.Network;

import com.google.gson.JsonObject;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.HeaderMap;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

/**
 * Created by apple on 12/18/17.
 */

public interface RestService {

    @GET("app_extra/")
    Call<ResponseBody> getAppExtraSettings(
            @HeaderMap Map<String, String> headers
    );

    @POST("app_extra/feedback/")
    Call<ResponseBody> postSendFeedback(
            @Body JsonObject login,
            @HeaderMap Map<String, String> headers
    );

    //Login user
    @POST("oauth/token")
    Call<ResponseBody> postLogin(
            @Body JsonObject login,
            @HeaderMap Map<String, String> headers
    );

    //Register New user
    @POST("signup/")
    Call<ResponseBody> postRegister(
            @Body JsonObject register,
            @HeaderMap Map<String, String> headers
    );

    //Send email to user
    @POST("forgot/")
    Call<ResponseBody> postForgotPassword(
            @Body JsonObject forgotPassword,
            @HeaderMap Map<String, String> headers
    );

    @POST("reset/")
    Call<ResponseBody> postResetPassword(
            @Body JsonObject resetPassword,
            @HeaderMap Map<String, String> headers
    );

    //Send email to user
    @POST("page/")
    Call<ResponseBody> postGetCustomePages(
            @Body JsonObject getCustomPage,
            @HeaderMap Map<String, String> headers
    );

    //Get Verify account views data
    @GET("login/confirm/")
    Call<ResponseBody> getVerifyAccountViewDetails(
            @HeaderMap Map<String, String> headers
    );

    //Confirm user account
    @POST("login/confirm/")
    Call<ResponseBody> postConfirmAccount(
            @Body JsonObject getCustomPage,
            @HeaderMap Map<String, String> headers
    );

    //endregion

    //region Related To new AdPost Endpoints
    @POST("car/")
    //Post New Ad
    Call<ResponseBody> postAdNewPost(
            @Body JsonObject adNewPost,
            @HeaderMap Map<String, String> headers
    );

    @POST("car/detail")
        //Post New Ad
    Call<ResponseBody> getDetail(
            @Body JsonObject adPost,
            @HeaderMap Map<String, String> headers
    );

    @POST("car/activate")
    Call<ResponseBody> activateCar(
            @Body JsonObject param,
            @HeaderMap Map<String, String> headers
    );

    //Get search data from menu bar
    @GET("categories/")
    Call<ResponseBody> getCategories(
            @HeaderMap Map<String, String> headers
    );
    //endregion

    //Get Sub Categories Details on Spinner click
    @POST("post_ad/subcats/")
    Call<ResponseBody> postGetSubCategories(
            @Body JsonObject getSubCat,
            @HeaderMap Map<String, String> headers
    );

    //Get Sub Location Details on Spinner click
    @POST("post_ad/sublocations/")
    Call<ResponseBody> postGetSubLocations(
            @Body JsonObject getSubLocations,
            @HeaderMap Map<String, String> headers
    );

    //Get Dynamic Field on Spinner click if Category template is on
    @POST("post_ad/dynamic_fields/")
    Call<ResponseBody> postGetDynamicFields(
            @Body JsonObject getDynamicFields,
            @HeaderMap Map<String, String> headers
    );

    //Delete image from New Ad post
    @POST("car/image/delete/")
    Call<ResponseBody> postDeleteImages(
            @Body JsonObject deleteImages,
            @HeaderMap Map<String, String> headers
    );

    //Ad new Images in New Ad post
    @Multipart
    @POST("car/image/")
    Call<ResponseBody> postUploadImage(
            @Part("car_id") RequestBody adid,
            @Part MultipartBody.Part parts
//            @HeaderMap Map<String, String> headers
    );

    //region Ad Details and Bids EndPoints
    //Get all data for Specific Ad
    @POST("car/detail")
    Call<ResponseBody> getAdsDetail(
            @Body JsonObject postBid,
            @HeaderMap Map<String, String> headers
    );

    @POST("car/feedback")
    Call<ResponseBody> getAdsFeedback(
            @Body JsonObject postBid,
            @HeaderMap Map<String, String> headers
    );

    //send report from Ad Details Activity
    @POST("ad_post/report/")
    Call<ResponseBody> postSendReport(
            @Body JsonObject sendReport,
            @HeaderMap Map<String, String> headers
    );

    //Ad is added in favourite from Ad Details Activity
    @POST("ad_post/favourite/")
    Call<ResponseBody> postAddToFavourite(
            @Body JsonObject favourite,
            @HeaderMap Map<String, String> headers
    );

    //Ad is added in featured from Ad Details Activity
    @POST("ad_post/featured/")
    Call<ResponseBody> postMakeFeatured(
            @Body JsonObject featured,
            @HeaderMap Map<String, String> headers
    );

    //Post Ad Rating
    @POST("ad_post/ad_rating/new/")
    Call<ResponseBody> postRating(
            @Body JsonObject postRating,
            @HeaderMap Map<String, String> headers
    );


    //region Menu and Dynamic Search Endpoints
    //Get all Search Dynmaic Views
    @POST("car/search/")
    Call<ResponseBody> getSearchDetails(
            @Body JsonObject params,
            @HeaderMap Map<String, String> headers
    );

    //Get load more ADs or search in Search Activity
    @POST("driver/search/")
    Call<ResponseBody> postDriverSearch(
            @Body JsonObject searchNdMore,
            @HeaderMap Map<String, String> headers
    );

    //Get Sub Categories Details on spinner click
    @POST("ad_post/subcats/")
    Call<ResponseBody> postGetSearcSubCats(
            @Body JsonObject searchSubCats,
            @HeaderMap Map<String, String> headers
    );

    //Get Sub Location Details on spinner click
    @POST("ad_post/sublocations/")
    Call<ResponseBody> postGetSearcSubLocation(
            @Body JsonObject searchSubCats,
            @HeaderMap Map<String, String> headers
    );

    //get Dynamic Fields data for search activity using categories_id
    @POST("ad_post/dynamic_widget/")
    Call<ResponseBody> postGetSearchDynamicFields(
            @Body JsonObject searchSubCats,
            @HeaderMap Map<String, String> headers
    );




    //region Home and Profile Endpoints
    //Get Home Details
    @GET("home")
    Call<ResponseBody> getHomeDetails(
            @HeaderMap Map<String, String> headers
    );

    //Set firebase id with server
    @POST("home")
    Call<ResponseBody> postFirebaseId(
            @Body JsonObject firebaseId,
            @HeaderMap Map<String, String> headers
    );

    //change NearBy Status and update lcoation
    @POST("search/nearby/")
    Call<ResponseBody> postChangeNearByStatus(
            @Body JsonObject changeStatus,
            @HeaderMap Map<String, String> headers
    );

    //Get All locations and Categories
    @POST("terms/")
    Call<ResponseBody> getAllLocAndCat(
            @Body JsonObject jsonObject,
            @HeaderMap Map<String, String> headers
    );
    //Change location of app
    @POST("site-location/")
    Call<ResponseBody> postLocationID(
            @Body JsonObject jsonObject,
            @HeaderMap Map<String, String> headers

    );
    //Get Profile Details
    @GET("profile/")
    Call<ResponseBody> getProfileDetails(
            @HeaderMap Map<String, String> headers
    );

    //Get Edit Profile Details
    @GET("profile/")
    Call<ResponseBody> getEditProfileDetails(
            @HeaderMap Map<String, String> headers
    );

    @POST("profile/")
    Call<ResponseBody> postUpdateProfile(
            @Body JsonObject editProfile,
            @HeaderMap Map<String, String> headers
    );

    //Upload image in Edit page
    @Multipart
    @POST("profile/image/")
    Call<ResponseBody> postUploadProfileImage(
            @Part MultipartBody.Part file1,
            @HeaderMap Map<String, String> headers
    );

    //Upload image in Edit page
    @Multipart
    @POST("profile/documents/")
    Call<ResponseBody> postUploadProfileDocument(
            @Part MultipartBody.Part file1,
            @Part MultipartBody.Part file2,
            @Part MultipartBody.Part file3,
            @Part MultipartBody.Part file4,
            @HeaderMap Map<String, String> headers
    );



    @POST("seller/profile")
    Call<ResponseBody> getSellerProfile(
            @Body JsonObject editProfile,
            @HeaderMap Map<String, String> headers
    );

    @POST("profile/delete/user_account/")
    Call<ResponseBody> postDeleteAccount(
            @Body JsonObject deleteAccount,
            @HeaderMap Map<String, String> headers
    );

    @POST("profile/public/")
    Call<ResponseBody> postGetPublicProfile(
            @Body JsonObject editProfile,
            @HeaderMap Map<String, String> headers
    );

    @GET("profile/phone_number/")
    Call<ResponseBody> getVerifyCode(
            @HeaderMap Map<String, String> headers
    );

    @POST("profile/phone_number/verify/")
    Call<ResponseBody> postVerifyPhoneNumber(
            @Body JsonObject editProfile,
            @HeaderMap Map<String, String> headers
    );


    //Change password in Edit Profile
    @POST("profile/reset_pass")
    Call<ResponseBody> postChangePasswordEditProfile(
            @Body JsonObject retingDetails,
            @HeaderMap Map<String, String> headers
    );

    //Get ratting Details
    @POST("profile/ratting_get/")
    Call<ResponseBody> postGetRatingDetails(
            @Body JsonObject retingDetails,
            @HeaderMap Map<String, String> headers
    );

    //post profile rating
    @POST("profile/ratting/")
    Call<ResponseBody> postProfileRating(
            @Body JsonObject profileRating,
            @HeaderMap Map<String, String> headers
    );
    //endregion


    //region Related To Ads Endpoints
    //Get My Ad details
    @GET("ad/")
    Call<ResponseBody> getMyAdsDetails(
            @HeaderMap Map<String, String> headers
    );
    @GET("ad/expire-sold/")
    Call<ResponseBody> getExpiredandSoldAds(
            @HeaderMap Map<String, String> headers
    );

    //Load More Myds
    @POST("ad/")
    Call<ResponseBody> postGetLoadMoreMyAds(
            @Body JsonObject loadMoreMyAds,
            @HeaderMap Map<String, String> headers
    );

    //Delete My ads
    @HTTP(method = "DELETE", path = "ad/delete/", hasBody = true)
    Call<ResponseBody> deleteMyAds(
            @Body JsonObject jsonObject,
            @HeaderMap Map<String, String> headers
    );

    @POST("realty/delete")
    Call<ResponseBody> deleteads(
            @Body JsonObject jsonObject,
            @HeaderMap Map<String, String> headers
    );

    //Get Featured AD details
    @GET("ad/featured/")
    Call<ResponseBody> getFeaturedAdsDetails(
            @HeaderMap Map<String, String> headers
    );

    //Load more Favourite Ads
    @POST("ad/featured/")
    Call<ResponseBody> postGetLoadMoreFeaturedAds(
            @Body JsonObject loadMoreFavourite,
            @HeaderMap Map<String, String> headers
    );

    //Get In AD details
    @GET("ad/inactive/")
    Call<ResponseBody> getInactiveAdsDetails(
            @HeaderMap Map<String, String> headers
    );

    //Get Inactive AD details
    @POST("ad/inactive/")
    Call<ResponseBody> postGetLoadMoreInactiveAds(
            @Body JsonObject loadMoreInactive,
            @HeaderMap Map<String, String> headers
    );
    //Get Rejected AD details

    @GET("ad/rejected/")
    Call<ResponseBody> getRejectedAdsDetails(
            @HeaderMap Map<String, String> headers
    );
    //Get Favourite AD details
    @GET("ad/favourite/")
    Call<ResponseBody> getFavouriteAdsDetails(
            @HeaderMap Map<String, String> headers
    );

    //Load more Favourite Ads
    @POST("ad/favourite/")
    Call<ResponseBody> postGetLoadMoreFavouriteAds(
            @Body JsonObject loadMoreFavourite,
            @HeaderMap Map<String, String> headers
    );

    //Remove ad From Favourite
    @POST("ad/favourite/remove/")
    Call<ResponseBody> postRemoveFavAd(
            @Body JsonObject removeFavAd,
            @HeaderMap Map<String, String> headers
    );

    //Update Ad Status
    @POST("ad/update/status/")
    Call<ResponseBody> postUpdateAdStatus(
            @Body JsonObject updateStatus,
            @HeaderMap Map<String, String> headers
    );
    //endregion


    //region Related to packages and payment Endpoints
    //Get packages Details
    @GET("packages/")
    Call<ResponseBody> getPackagesDetails(
            @HeaderMap Map<String, String> headers
    );

    //Get Stripe Payment View Details
    @GET("payment/card/")
    Call<ResponseBody> getStripeDetailsView(
            @HeaderMap Map<String, String> headers
    );

    @POST("card/")
    Call<ResponseBody> createCard(
            @Body JsonObject cardObj,
            @HeaderMap Map<String, String> headers
    );

    //Checkout endpoint in checkout proocess activity
    @POST("payment/invoice")
    Call<ResponseBody> postInvoice(
            @Body JsonObject invoiceData,
            @HeaderMap Map<String, String> headers
    );

    //Checkout endpoint in checkout proocess activity
    @POST("payment/")
    Call<ResponseBody> postCheckout(
            @Body JsonObject updateStatus,
            @HeaderMap Map<String, String> headers
    );
    @POST("buy/")
    Call<ResponseBody> postBuy(
            @Body JsonObject updateStatus,
            @HeaderMap Map<String, String> headers
    );
    @POST("booking/")
    Call<ResponseBody> postBooking(
            @Body JsonObject updateStatus,
            @HeaderMap Map<String, String> headers
    );

    @POST("hire/")
    Call<ResponseBody> postHiring(
            @Body JsonObject updateStatus,
            @HeaderMap Map<String, String> headers
    );

    //Get Data When Payment is Completed Successfully
    @GET("payment/complete")
    Call<ResponseBody> getPaymentCompleteData(
            @HeaderMap Map<String, String> headers
    );

    @POST("card/")
    Call<ResponseBody> postCard(
            @Body JsonObject card,
            @HeaderMap Map<String, String> headers
    );

    @GET("card/")
    Call<ResponseBody> cardlist(
            @HeaderMap Map<String, String> headers
    );

    @POST("card/delete")
    Call<ResponseBody> deleteCard(
            @Body JsonObject jsonObject,
            @HeaderMap Map<String, String> headers
    );

    @POST("buy/confirm")
    Call<ResponseBody> confirmBuy(
            @Body JsonObject updateStatus,
            @HeaderMap Map<String, String> headers
    );

    @GET("booking/list")
    Call<ResponseBody> bookinglist(
            @HeaderMap Map<String, String> headers
    );

    @POST("booking/confirm")
    Call<ResponseBody> bookingConfirm(
            @Body JsonObject jsonObject,
            @HeaderMap Map<String, String> headers
    );

    @POST("booking/decline")
    Call<ResponseBody> bookingDecline(
            @Body JsonObject jsonObject,
            @HeaderMap Map<String, String> headers
    );

    @POST("booking/complete")
    Call<ResponseBody> bookingcomplete(
            @Body JsonObject jsonObject,
            @HeaderMap Map<String, String> headers
    );

    @POST("booking/cancel")
    Call<ResponseBody> bookingcancel(
            @Body JsonObject jsonObject,
            @HeaderMap Map<String, String> headers
    );

    @POST("booking/cancel/info")
    Call<ResponseBody> bookingcancelinfo(
            @Body JsonObject jsonObject,
            @HeaderMap Map<String, String> headers
    );

    @GET("hire/list")
    Call<ResponseBody> hireList(
            @HeaderMap Map<String, String> headers
    );

    @POST("hire/confirm")
    Call<ResponseBody>hireConfirm(
            @Body JsonObject jsonObject,
            @HeaderMap Map<String, String> headers
    );

    @POST("hire/decline")
    Call<ResponseBody>hireDecline(
            @Body JsonObject jsonObject,
            @HeaderMap Map<String, String> headers
    );

    @POST("hire/complete")
    Call<ResponseBody>hireComplete(
            @Body JsonObject jsonObject,
            @HeaderMap Map<String, String> headers
    );

    @POST("hire/cancel")
    Call<ResponseBody> hireCancel(
            @Body JsonObject jsonObject,
            @HeaderMap Map<String, String> headers
    );

    @POST("hire/cancel/info")
    Call<ResponseBody> hireCancelInfo(
            @Body JsonObject jsonObject,
            @HeaderMap Map<String, String> headers
    );

    @POST("hire/delete")
    Call<ResponseBody> hireDelete(
            @Body JsonObject jsonObject,
            @HeaderMap Map<String, String> headers
    );

    @GET("seller/car/list")
    Call<ResponseBody> sellerCarList(
            @HeaderMap Map<String, String> headers
    );

    @GET("stripe/connect")
    Call<ResponseBody> connectStripe(
            @HeaderMap Map<String, String> headers
    );

    @GET("seller/account")
    Call<ResponseBody> getAccount(
            @HeaderMap Map<String, String> headers
    );

    @POST("cashout")
    Call<ResponseBody> withdraw(
            @Body JsonObject jsonObject,
            @HeaderMap Map<String, String> headers
    );

    @GET("cashout/history")
    Call<ResponseBody> getWithdrawHistory(
            @HeaderMap Map<String, String> headers
    );

    @GET("plaid/linktoken")
    Call<ResponseBody> getPlaidLinkToken(
            @HeaderMap Map<String, String> headers
    );

    @POST("plaid/pubtoken")
    Call<ResponseBody> postPlaidPubToken(
            @Body JsonObject jsonObject,
            @HeaderMap Map<String, String> headers
    );

    @POST("plaid/bank/create")
    Call<ResponseBody> postPlaidCreateBank(
            @Body JsonObject jsonObject,
            @HeaderMap Map<String, String> headers
    );

    //endregion

    @POST("dial/masked_number")
    Call<ResponseBody>maskedNumber(
            @Body JsonObject jsonObject,
            @HeaderMap Map<String, String> headers
    );

    @POST("dial/end")
    Call<ResponseBody>dialend(
            @Body JsonObject jsonObject,
            @HeaderMap Map<String, String> headers
    );
    //region Message EndPoints
    //Get Received offers details
    @GET("message/inbox/")
    Call<ResponseBody> getRecievedOffers(
            @HeaderMap Map<String, String> headers
    );

    ///delete chat item
    @POST("message/inbox/delete")
    Call<ResponseBody> deleteChat(
            @Body JsonObject recievedList,
            @HeaderMap Map<String, String> headers
    );

    //Load More messages in Recieved offers
    @POST("message/inbox/")
    Call<ResponseBody> postLoadMoreRecievedOffer(
            @Body JsonObject loadMoreReceieved,
            @HeaderMap Map<String, String> headers
    );

    //Get Send Offers Details
    @GET("message/")
    Call<ResponseBody> getSendOffers(
            @HeaderMap Map<String, String> headers
    );

    //Load more messgae list in send offeres
    @POST("message_post/")
    Call<ResponseBody> postLoadMoreSendOffers(
            @Body JsonObject loadSendOffers,
            @HeaderMap Map<String, String> headers
    );

    //Message popup view in Ad Details
    @POST("message/chat/request/")
    Call<ResponseBody> postSendMessageFromAd(
            @Body JsonObject postBid,
            @HeaderMap Map<String, String> headers
    );

    //Get complete chat or load more chat in
    @POST("message/chat/post/")
    Call<ResponseBody> postGetChatORLoadMore(
            @Body JsonObject getChat,
            @HeaderMap Map<String, String> headers
    );
    @POST("message/chat/userblock/")
    Call<ResponseBody> postUserBlock(
            @Body JsonObject Block,
            @HeaderMap Map<String, String> headers);
    @POST("message/chat/userUnblock/")
    Call<ResponseBody> postUserUnBlock(
            @Body JsonObject UnBlock,
            @HeaderMap Map<String, String> headers);

    //Send Message
    @POST("message/chat/")
    Call<ResponseBody> postSendMessage(
            @Body JsonObject sendMessage,
            @HeaderMap Map<String, String> headers
    );

    @POST("message/chat/close")
    Call<ResponseBody> chatClose(
            @Body JsonObject sendMessage,
            @HeaderMap Map<String, String> headers
    );


    //endregion

    //region User block and UnBlock
    @POST("user/block/")
    Call<ResponseBody> postBlockUser(
            @Body JsonObject blockUser,
            @HeaderMap Map<String, String> headers
    );

    @GET("user/block/")
    Call<ResponseBody> getBlockedUsers(
            @HeaderMap Map<String, String> headers
    );
    @GET("message/chat/userblocklist/")
    Call<ResponseBody> getMessageBlockedUsers(
            @HeaderMap Map<String, String> headers
    );

    @POST("user/unblock/")
    Call<ResponseBody> postUnblockUser(
            @Body JsonObject unblockUser,
            @HeaderMap Map<String, String> headers
    );
    //endregion

    //region Shop

    //Get Shop items
    @GET("shop/")
    Call<ResponseBody> getShopItemDetails(
            @HeaderMap Map<String, String> headers
    );

    //load more shop items and sort shop
    @POST("shop/")
    Call<ResponseBody> getMoreShopItems(
            @Body JsonObject jsonObject,
            @HeaderMap Map<String, String> headers
    );

    //Get shop item detail
    @POST("shop/detail")
    Call<ResponseBody> getShopProductDetail(
            @Body JsonObject jsonObject,
            @HeaderMap Map<String, String> headers
    );

    //Get shop item reviews
    @POST("shop/get_review")
    Call<ResponseBody> getShopItemReviews(
            @Body JsonObject jsonObject,
            @HeaderMap Map<String, String> headers
    );

    //Post review on shop item
    @POST("shop/submit_review")
    Call<ResponseBody> postShopItemReviews(
            @Body JsonObject jsonObject,
            @HeaderMap Map<String, String> headers
    );

    //Get Cart
    @GET("cart/")
    Call<ResponseBody> getCartList(
            @HeaderMap Map<String, String> headers
    );

    //Remove item from Cart
    @POST("cart/remove_item")
    Call<ResponseBody> postRemoveCartItem(
            @Body JsonObject jsonObject,
            @HeaderMap Map<String, String> headers
    );

    //Add item to cart
    @POST("cart/add")
    Call<ResponseBody> postAdToCart(
            @Body JsonObject jsonObject,
            @HeaderMap Map<String, String> headers
    );
    //endregion

    //region sellers
    @GET("sellers/")
    Call<ResponseBody> getSellersList(
            @HeaderMap Map<String, String> headers
    );

    //Get more Sellers
    @POST("sellers/")
    Call<ResponseBody> getMoreSellersList(
            @Body JsonObject jsonObject,
            @HeaderMap Map<String, String> headers
    );

    @GET("help/")
    Call<ResponseBody> help(
            @HeaderMap Map<String, String> headers
    );
    //endregion
    @GET("notification")
    Call<ResponseBody> notifications(
            @HeaderMap Map<String, String> headers
    );
//    @HTTP(method = "DELETE", path = "notification", hasBody = false)
    @DELETE("notification/{id}")
    Call<ResponseBody> deleteNotification(
            @Path("id") String id,
            @HeaderMap Map<String, String> headers
    );

    @GET("coins/")
    Call<ResponseBody> getCoins(
            @HeaderMap Map<String, String> headers
    );

    //Get more Sellers
    @POST("coin/deposit")
    Call<ResponseBody> deposit(
            @Body JsonObject jsonObject,
            @HeaderMap Map<String, String> headers
    );

    @GET("coin/deposit")
    Call<ResponseBody> getActivities(
            @HeaderMap Map<String, String> headers
    );

    @POST("coin/delete")
    Call<ResponseBody> deleteCoin(
            @Body JsonObject jsonObject,
            @HeaderMap Map<String, String> headers
    );

    @POST("avis/locations")
    Call<ResponseBody> locationsAvis(
            @Body JsonObject jsonObject,
            @HeaderMap Map<String, String> headers
    );

    @POST("avis/cars")
    Call<ResponseBody> searchAvis(
            @Body JsonObject jsonObject,
            @HeaderMap Map<String, String> headers
    );

    @POST("avis/create")
    Call<ResponseBody> bookAvis(
            @Body JsonObject jsonObject,
            @HeaderMap Map<String, String> headers
    );

    @POST("avis/reservation")
    Call<ResponseBody> getAvis(
            @Body JsonObject jsonObject,
            @HeaderMap Map<String, String> headers
    );
}
