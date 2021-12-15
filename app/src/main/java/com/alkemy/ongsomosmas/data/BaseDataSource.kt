package com.alkemy.ongsomosmas.data

import com.alkemy.ongsomosmas.data.model.ApiResponse
import retrofit2.Response
import java.io.Serializable

abstract class BaseDataSource {

    protected suspend fun <T> getResult(call: suspend () -> Response<ApiResponse<T>>): Resource<T> {
        try {
            val response = call()
//            if(response.isSuccessful) {
//                val body = response.body()?.data
//                if(body != null) return Resource.success(body)
//            }
            val body = response.body()?.data
            return Resource.success(body)
            //return Resource.error("${response.code()}: ${response.message()}")
        } catch (e: Exception) {
            return Resource.error(e.message ?: "Generic error")
        }
    }
}

/**
 * Clase de asistencia que permite encapsular las respuestas del repositorio
 * según su estados (mientras esta cargando, cuando se cargo con éxito y cuando ocurrió algún error)
 */
data class Resource<out T>(val status: Status, val data: T?, val message: String?) : Serializable {

    enum class Status {
        SUCCESS,
        ERROR,
        LOADING
    }

    companion object {
        fun <T> success(data: T?): Resource<T> {
            return Resource(
                Status.SUCCESS,
                data,
                null
            )
        }

        fun <T> error(message: String, data: T? = null): Resource<T> {
            return Resource(
                Status.ERROR,
                data,
                message
            )
        }

        fun <T> loading(data: T? = null): Resource<T> {
            return Resource(
                Status.LOADING,
                data,
                null
            )
        }
    }

    fun isSuccessful() = status == Status.SUCCESS

    fun isError() = status == Status.ERROR

    fun isLoading() = status == Status.LOADING
}