package com.uniruta.app.data.storage

const val RECEIPTS_FOLDER = "receipts"
const val EVIDENCE_FOLDER = "evidence"

data class StoredImage(
    val displayName: String,
    val path: String,
    val sizeBytes: Long
)

data class CaptureTarget(
    val path: String,
    val contentUri: String
)

sealed interface ImageResult {
    data class Success(val image: StoredImage) : ImageResult
    data class Failure(val message: String) : ImageResult
}

interface ImageStore {
    suspend fun importImage(sourceUri: String, folder: String, baseName: String): ImageResult
    fun createCaptureTarget(folder: String, baseName: String): CaptureTarget?
    suspend fun confirmCapture(target: CaptureTarget): ImageResult
    suspend fun delete(path: String)
    suspend fun clear(folder: String)
}
