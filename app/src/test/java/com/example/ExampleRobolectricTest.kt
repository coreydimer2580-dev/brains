package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.BrainDataRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
    assertEquals("Brain Learning", appName)
  }

  @Test
  fun `verify brain lobes and quiz data are loaded`() {
    assertTrue("Should have 6 brain lobes", BrainDataRepository.brainLobes.size == 6)
    assertTrue("Should have quiz questions", BrainDataRepository.quizQuestions.isNotEmpty())
    assertTrue("Should have neurotransmitters", BrainDataRepository.neurotransmitters.isNotEmpty())
  }
}
