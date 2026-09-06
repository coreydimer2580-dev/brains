package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.BrainDataRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
    
    // Verify each lobe has a valid short educational description
    BrainDataRepository.brainLobes.forEach { lobe ->
      assertTrue("Lobe ${lobe.name} should have educational description", lobe.description.isNotBlank())
      assertTrue("Lobe ${lobe.name} should have functions", lobe.functions.isNotEmpty())
    }
  }

  @Test
  fun `verify simple memory game initialization and pairs`() {
    val context = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = com.example.viewmodel.BrainViewModel(context)

    viewModel.startCardMatchGame()
    val cards = viewModel.cardList.value
    assertEquals("Card match should have 12 cards total", 12, cards.size)

    // Verify exactly 6 unique pairs (2 cards per pairId)
    val pairCounts = cards.groupBy { it.pairId }
    assertEquals("Should have 6 distinct pairs", 6, pairCounts.size)
    pairCounts.values.forEach { group ->
      assertEquals("Each pair should have exactly 2 cards", 2, group.size)
    }

    assertEquals("Initial moves should be 0", 0, viewModel.cardMoves.value)
    assertEquals("Initial matches should be 0", 0, viewModel.cardMatchedPairs.value)
  }

  @Test
  fun `verify pattern sequence starts with 3 lives and can be replayed`() {
    val context = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = com.example.viewmodel.BrainViewModel(context)

    viewModel.startPatternSequenceGame()
    assertEquals("Pattern sequence must provide 3 lives to prevent frustration", 3, viewModel.memoryLives.value)
    assertEquals("Level should start at 1", 1, viewModel.memoryLevel.value)
  }

  @Test
  fun `verify focus session recording and active game transitions`() {
    val context = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = com.example.viewmodel.BrainViewModel(context)

    viewModel.openGame(com.example.viewmodel.ActiveGame.FOCUS_TRAINER)
    assertEquals(com.example.viewmodel.ActiveGame.FOCUS_TRAINER, viewModel.activeGame.value)

    viewModel.recordFocusSession(30)
    viewModel.exitCurrentGame()
    assertEquals(com.example.viewmodel.ActiveGame.NONE, viewModel.activeGame.value)
  }

  @Test
  fun `verify 3D brain regions including frontal lobe and hippocampus have descriptive labels`() {
    val frontal = BrainDataRepository.brainLobes.find { it.id == "FRONTAL" }
    val limbicHippo = BrainDataRepository.brainLobes.find { it.id == "LIMBIC" }

    assertNotNull("Frontal lobe should exist", frontal)
    assertNotNull("Hippocampus/Limbic system should exist", limbicHippo)

    assertTrue("Frontal lobe should have description", frontal!!.description.isNotBlank())
    assertTrue("Frontal lobe should have executive functions", frontal.functions.isNotEmpty())

    assertTrue("Hippocampus should have description", limbicHippo!!.description.isNotBlank())
    assertTrue("Hippocampus should describe memory or neurogenesis", limbicHippo.description.contains("Hippocampus", ignoreCase = true) || limbicHippo.description.contains("memory", ignoreCase = true))

    // Verify 3D Point Projection math
    val pt = com.example.ui.components.Point3D(0f, 32f, 52f)
    val rotated = pt.rotate(0.5f, 0.2f)
    val projected = rotated.project(400f, 400f)

    assertTrue("Projected screenX should be within visible bounds", projected.screenX in 50f..350f)
    assertTrue("Projected screenY should be within visible bounds", projected.screenY in 50f..350f)
    assertTrue("Scale factor should be positive", projected.scale > 0f)
  }

  @Test
  fun `verify brain analytics export report generation`() {
    val sampleWorkouts = listOf(
        com.example.data.WorkoutEntity(
            gameType = "FOCUS_TRAINING",
            score = 100,
            accuracy = 1.0f,
            reactionTimeMs = 30000L
        ),
        com.example.data.WorkoutEntity(
            gameType = "MEMORY_GRID",
            score = 150,
            accuracy = 0.9f,
            reactionTimeMs = 45000L
        )
    )

    val report = com.example.ui.screens.generateAnalyticsReport(
        bq = 120,
        streak = 3,
        workouts = sampleWorkouts
    )

    assertTrue("Report should contain BRAIN ANALYTICS header", report.contains("BRAIN ANALYTICS REPORT"))
    assertTrue("Report should contain BQ score", report.contains("Brain Quotient (BQ): 120"))
    assertTrue("Report should contain streak", report.contains("Current Daily Streak: 3 days"))
    assertTrue("Report should list FOCUS_TRAINING", report.contains("FOCUS_TRAINING"))
    assertTrue("Report should list MEMORY_GRID", report.contains("MEMORY_GRID"))
  }
}
