import com.fh.payday.datasource.models.Notification
import com.fh.payday.datasource.remote.ApiResult
import com.fh.payday.utilities.apiVersion
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface NotificationApiInterface {
    @GET("$apiVersion/customers/{id}/notification")
    fun notifications(
        @Path("id") id: Long
    ): Call<ApiResult<List<Notification>>>
}