package com.hooman.transim.domain.recorder

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFAudio.AVAudioSession
import kotlin.coroutines.resume

class AudioMicPermissionControllerIOS: MicPermissionController {
    override suspend fun ensurePermission(): Boolean =
        suspendCancellableCoroutine { cont ->
            AVAudioSession.sharedInstance()
                .requestRecordPermission { granted ->
                    cont.resume(granted)
                }
        }
}