package com.example.services

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast

class SystemActionExecutor(private val context: Context) {

    private var isFlashlightOn = false

    fun openApp(appName: String): String {
        val target = appName.lowercase()
        return when {
            target.contains("camera") -> {
                val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                "Opened Camera"
            }
            target.contains("whatsapp") -> {
                launchPackage("com.whatsapp") ?: openUrl("https://web.whatsapp.com")
                "Opened WhatsApp"
            }
            target.contains("youtube") -> {
                launchPackage("com.google.android.youtube") ?: openUrl("https://www.youtube.com")
                "Opened YouTube"
            }
            target.contains("chrome") || target.contains("browser") -> {
                openUrl("https://www.google.com")
                "Opened Chrome"
            }
            target.contains("gallery") || target.contains("photo") -> {
                val intent = Intent(Intent.ACTION_VIEW, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                "Opened Gallery"
            }
            target.contains("setting") -> {
                val intent = Intent(Settings.ACTION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                "Opened Settings"
            }
            target.contains("calculator") -> {
                launchPackage("com.google.android.calculator") ?: launchPackage("com.sec.android.app.popupcalculator")
                "Opened Calculator"
            }
            target.contains("contact") -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("content://contacts/people")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                "Opened Contacts"
            }
            else -> {
                openUrl("https://www.google.com/search?q=$appName")
                "Searched for $appName"
            }
        }
    }

    private fun launchPackage(packageName: String): String? {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        return if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            "Opened App"
        } else null
    }

    fun toggleFlashlight(enable: Boolean): String {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList[0]
            cameraManager.setTorchMode(cameraId, enable)
            isFlashlightOn = enable
            if (enable) "Flashlight turned ON" else "Flashlight turned OFF"
        } catch (e: Exception) {
            "Flashlight control not supported on this device"
        }
    }

    fun adjustVolume(increase: Boolean): String {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val direction = if (increase) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, AudioManager.FLAG_SHOW_UI)
        return if (increase) "Volume increased" else "Volume decreased"
    }

    fun openSettingsScreen(type: String): String {
        val intent = when (type.lowercase()) {
            "wifi" -> Intent(Settings.ACTION_WIFI_SETTINGS)
            "bluetooth" -> Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            "display", "brightness" -> Intent(Settings.ACTION_DISPLAY_SETTINGS)
            "sound" -> Intent(Settings.ACTION_SOUND_SETTINGS)
            "dnd" -> Intent(Settings.ACTION_ZEN_MODE_PRIORITY_SETTINGS)
            else -> Intent(Settings.ACTION_SETTINGS)
        }.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }

        return try {
            context.startActivity(intent)
            "Opened $type settings"
        } catch (e: Exception) {
            "Opened Settings"
        }
    }

    fun smartSearch(query: String, platform: String): String {
        return when (platform.lowercase()) {
            "youtube" -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=$query")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                "Searching YouTube for '$query'"
            }
            "maps", "navigation" -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=$query")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                "Navigating to '$query' on Maps"
            }
            "spotify", "music" -> {
                openUrl("https://open.spotify.com/search/$query")
                "Searching Spotify for '$query'"
            }
            else -> {
                openUrl("https://www.google.com/search?q=$query")
                "Searching Google for '$query'"
            }
        }
    }

    fun setAlarm(hour: Int, minute: Int, message: String): String {
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minute)
            putExtra(AlarmClock.EXTRA_MESSAGE, message)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            "Alarm set for $hour:${if (minute < 10) "0$minute" else minute}"
        } catch (e: Exception) {
            "Alarm command triggered"
        }
    }

    fun makeCall(phoneNumber: String): String {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        return "Dialing $phoneNumber"
    }

    fun triggerEmergencySos(): String {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:112")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        return "Initiating Emergency SOS call to 112..."
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}
