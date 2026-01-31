package com.hooman.transim.domain.recorder

interface MicPermissionController {
    suspend fun ensurePermission(): Boolean
}