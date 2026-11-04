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
@Config(sdk = [34])
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
        address = "12 Broad Street, Lagos",
        occupation = "Senior Engineer",
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
    assertEquals("12 Broad Street, Lagos", fetched?.address)
    assertEquals("Senior Engineer", fetched?.occupation)
    assertEquals("@testuser", fetched?.tiktok)
    assertEquals("user@test.org", fetched?.email)
    assertEquals("https://user.org", fetched?.websites)
    assertEquals(1, fetched?.getPersonalInfoList()?.size)
    assertEquals("NIN", fetched?.getPersonalInfoList()?.first()?.label)
    assertEquals(1, fetched?.getOtherInfoList()?.size)
    assertEquals("Party", fetched?.getOtherInfoList()?.first()?.label)
  }

  @Test
  fun `multi-page row building and pagination planning exceeds one page when content is large`() {
    var profile = DossierProfile.createEmpty().copy(
        fullName = "Alexander Maximillian",
        gender = "Male",
        lgaState = "Ikeja, Lagos State",
        address = "Plot 42, Innovation Hub Avenue, Victoria Island, Lagos, Nigeria",
        phoneNumber = "+2348011112222, +2348033334444",
        alsoKnownAs = "The Architect",
        dateOfBirth = "14th April 1980",
        facebook = "alex.max.fb",
        twitter = "@alex_arch",
        instagram = "@alex.maximillian",
        tiktok = "@alex_tok",
        youtube = "youtube.com/@alexmax",
        email = "alex.maximillian.official@corporation.example.org",
        websites = "https://alexmaximillian.org\nhttps://consulting.alex.org",
        otherSocialMedia = "LinkedIn: in/alexmax, Telegram: @alex_max",
        occupation = "Managing Director & Senior Consultant",
        education = "Ph.D. in Computer Engineering, M.Sc Software Systems",
        associatesPhoneNumbers = "+2348099998888 (Executive Assistant)\n+2348077776666 (Chief of Staff)"
    )

    val customFields = (1..25).map { i ->
        CustomField(label = "Custom Field $i", value = "Detailed Value Content for Custom Field $i - Testing long text wrapping and pagination")
    }
    profile = profile.withOtherInfoList(customFields)

    val sections = com.example.pdf.PdfGenerator.buildSections(profile)
    org.junit.Assert.assertEquals(3, sections.size)
    org.junit.Assert.assertEquals("PERSONAL & CONTACT INFORMATION", sections[0].title)
    org.junit.Assert.assertEquals("SOCIAL MEDIA", sections[1].title)
    org.junit.Assert.assertEquals("OTHER INFORMATION", sections[2].title)

    // Check that address is present in personal contact info
    val personalSection = sections[0]
    val hasAddress = personalSection.rows.any { row ->
        when (row) {
            is com.example.pdf.RenderRow.MultiLine -> row.entry.label == "Address"
            is com.example.pdf.RenderRow.Pair -> row.entry1.label == "Address" || row.entry2.label == "Address"
            is com.example.pdf.RenderRow.Single -> row.entry.label == "Address"
        }
    }
    org.junit.Assert.assertTrue("Address field must be present in Personal & Contact section", hasAddress)

    // Check that occupation label is 'Occupation/ Role'
    val otherSection = sections[2]
    val hasOccupationRole = otherSection.rows.any { row ->
        when (row) {
            is com.example.pdf.RenderRow.MultiLine -> row.entry.label == "Occupation/ Role"
            is com.example.pdf.RenderRow.Pair -> row.entry1.label == "Occupation/ Role" || row.entry2.label == "Occupation/ Role"
            is com.example.pdf.RenderRow.Single -> row.entry.label == "Occupation/ Role"
        }
    }
    org.junit.Assert.assertTrue("Occupation label must be 'Occupation/ Role'", hasOccupationRole)

    // Total rows across all sections should be significant
    val totalRows = sections.sumOf { it.rows.size }
    org.junit.Assert.assertTrue("Total rows should exceed 20 rows, got: $totalRows", totalRows > 20)

    val estimatedPages = com.example.pdf.PdfGenerator.estimatePageCount(profile)
    org.junit.Assert.assertTrue("Estimated page count should be at least 2, got: $estimatedPages", estimatedPages >= 2)
  }
}
