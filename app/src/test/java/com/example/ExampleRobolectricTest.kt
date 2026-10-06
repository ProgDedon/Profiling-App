package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.CustomField
import com.example.data.model.DossierProfile
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("greymmc", appName)
  }

  @Test
  fun `database and profile operations succeed`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = AppDatabase.getDatabase(context)
    val dao = db.dossierDao()

    var profile = DossierProfile.createEmpty().copy(
        fullName = "Test User",
        tiktok = "@testuser",
        email = "user@test.org",
        websites = "https://user.org"
    )
    profile = profile.withPersonalInfoList(listOf(CustomField(label = "NIN", value = "12345678901")))
    profile = profile.withOtherInfoList(listOf(CustomField(label = "Party", value = "Progressive")))

    val newId = dao.insertProfile(profile)
    val fetched = dao.getProfileById(newId)
    assertNotNull(fetched)
    assertEquals("Test User", fetched?.fullName)
    assertEquals("@testuser", fetched?.tiktok)
    assertEquals("user@test.org", fetched?.email)
    assertEquals("https://user.org", fetched?.websites)
    assertEquals(1, fetched?.getPersonalInfoList()?.size)
    assertEquals("NIN", fetched?.getPersonalInfoList()?.first()?.label)
    assertEquals(1, fetched?.getOtherInfoList()?.size)
    assertEquals("Party", fetched?.getOtherInfoList()?.first()?.label)
  }
}
