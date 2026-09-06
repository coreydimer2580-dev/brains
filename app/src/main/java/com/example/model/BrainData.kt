package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

data class BrainLobe(
    val id: String,
    val name: String,
    val latinName: String,
    val subtitle: String,
    val description: String,
    val accentColor: Color,
    val functions: List<String>,
    val keyNeurotransmitters: List<String>,
    val clinicalInsight: String,
    val trainingTip: String,
    val relativePositionX: Float, // 0.0 to 1.0 on visual diagram
    val relativePositionY: Float
)

data class Neurotransmitter(
    val name: String,
    val formulaSymbol: String,
    val role: String,
    val description: String,
    val naturalBoosters: List<String>,
    val color: Color
)

data class NeuroMyth(
    val myth: String,
    val fact: String,
    val explanation: String
)

data class BrainQuizQuestion(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val targetLobe: String
)

data class NeuroFlashcard(
    val id: Int,
    val term: String,
    val category: String,
    val definition: String,
    val keyTakeaway: String
)

object BrainDataRepository {

    val brainLobes = listOf(
        BrainLobe(
            id = "FRONTAL",
            name = "Frontal Lobe",
            latinName = "Lobus frontalis",
            subtitle = "Executive Function & Decision Making",
            description = "Occupying the front of the brain, the frontal lobe is your cognitive command center. It orchestrates conscious decision-making, working memory, impulse inhibition, goal-directed planning, voluntary movement, and expressive language.",
            accentColor = AccentFrontal,
            functions = listOf(
                "Executive cognitive control & planning",
                "Working memory manipulation",
                "Impulse inhibition & emotional regulation",
                "Speech production (Broca's area)",
                "Voluntary movement (Primary motor cortex)"
            ),
            keyNeurotransmitters = listOf("Dopamine", "Acetylcholine", "Norepinephrine"),
            clinicalInsight = "The prefrontal cortex is the last major brain region to reach full myelination and structural maturity—typically finishing around age 25.",
            trainingTip = "Strengthened by inhibitory conflict exercises (like the Stroop challenge) and complex strategic planning.",
            relativePositionX = 0.28f,
            relativePositionY = 0.35f
        ),
        BrainLobe(
            id = "PARIETAL",
            name = "Parietal Lobe",
            latinName = "Lobus parietalis",
            subtitle = "Sensory Integration & Spatial Logic",
            description = "Situated at the upper posterior cortex, the parietal lobe merges sensory feedback into a unified internal map. It handles touch and spatial awareness, manipulates 3D mental geometry, and calculates numerical relationships.",
            accentColor = AccentParietal,
            functions = listOf(
                "Somatosensory processing (touch, temperature, pain)",
                "Spatial navigation and mental rotation",
                "Numerical computation & mathematical logic",
                "Body proprioception and coordinate mapping"
            ),
            keyNeurotransmitters = listOf("Glutamate", "GABA"),
            clinicalInsight = "The angular gyrus links mathematical representation with language. Lesions here cause dyscalculia and disorientation.",
            trainingTip = "Trained through rapid mental arithmetic, geometric puzzles, and spatial navigation tasks.",
            relativePositionX = 0.62f,
            relativePositionY = 0.30f
        ),
        BrainLobe(
            id = "OCCIPITAL",
            name = "Occipital Lobe",
            latinName = "Lobus occipitalis",
            subtitle = "Visual Cortex & Pattern Processing",
            description = "Nestled at the back of the skull, the occipital lobe houses the primary visual cortex (V1). It decodes optical signals from the retina into perceived shapes, depth, high-speed motion, color frequencies, and object recognition.",
            accentColor = AccentOccipital,
            functions = listOf(
                "Primary visual processing (Area V1)",
                "Edge, orientation, and motion detection (V5/MT)",
                "Color wavelength distinction (V4)",
                "Visual scene reconstruction and retinotopy"
            ),
            keyNeurotransmitters = listOf("GABA", "Glutamate"),
            clinicalInsight = "Information travels from the retina via the optic chiasm and lateral geniculate nucleus (LGN) straight to the calcarine sulcus.",
            trainingTip = "Sharpened by visual pattern recognition, matrix reasoning, and rapid color-ink discrimination.",
            relativePositionX = 0.85f,
            relativePositionY = 0.48f
        ),
        BrainLobe(
            id = "TEMPORAL",
            name = "Temporal Lobe",
            latinName = "Lobus temporalis",
            subtitle = "Memory Encoding & Language Audio",
            description = "Resting along the sides of the cerebrum near your ears, the temporal lobe is the core hub for auditory processing, speech comprehension (Wernicke's area), and converting short-term experiences into long-term memories via the hippocampus.",
            accentColor = AccentTemporal,
            functions = listOf(
                "Hippocampal memory consolidation",
                "Auditory cortex & rhythm processing",
                "Language comprehension (Wernicke's area)",
                "Facial recognition (Fusiform face area)"
            ),
            keyNeurotransmitters = listOf("Acetylcholine", "Serotonin"),
            clinicalInsight = "Patient H.M.'s bilateral hippocampal resection proved that long-term memory formation is distinct from procedural motor memory.",
            trainingTip = "Enhanced through spaced retrieval practice, sequential pattern recall, and auditory learning.",
            relativePositionX = 0.45f,
            relativePositionY = 0.62f
        ),
        BrainLobe(
            id = "CEREBELLUM",
            name = "Cerebellum",
            latinName = "Cerebellum (Little Brain)",
            subtitle = "Motor Precision, Balance & Timing",
            description = "Located beneath the occipital and temporal lobes, the cerebellum packs over 50 billion densely organized neurons. It fine-tunes motor coordination, maintains equilibrium, and synchronizes milliseconds-accurate timing for learned movements.",
            accentColor = AccentCerebellum,
            functions = listOf(
                "Precision muscle coordination and balance",
                "Sub-second timing and rhythmic sync",
                "Procedural muscle memory automation",
                "Internal predictive motor models"
            ),
            keyNeurotransmitters = listOf("GABA", "Glutamate"),
            clinicalInsight = "The cerebellum houses over 50% of the brain's total neurons (mainly granule cells), despite occupying barely 10% of brain volume.",
            trainingTip = "Stimulated by fast reflex timing, rhythmic tapping tasks, and rapid motor coordination drills.",
            relativePositionX = 0.72f,
            relativePositionY = 0.78f
        ),
        BrainLobe(
            id = "LIMBIC",
            name = "Limbic & Hippocampus",
            latinName = "Systema limbicum",
            subtitle = "Emotion, Salience & Neurogenesis",
            description = "Deep within the brain's core sits the limbic system, enclosing the amygdala and hippocampus. It regulates primal emotional responses, stress survival hormones, memory indexing, and is one of the few sites where adult neurogenesis continuously occurs.",
            accentColor = AccentLimbic,
            functions = listOf(
                "Emotional salience & fear conditioning (Amygdala)",
                "Adult neurogenesis (Dentate gyrus of Hippocampus)",
                "Autonomic and hormonal homeostasis (Hypothalamus)",
                "Episodic memory indexing"
            ),
            keyNeurotransmitters = listOf("Dopamine", "Norepinephrine", "BDNF"),
            clinicalInsight = "Aerobic physical exercise upregulates BDNF (Brain-Derived Neurotrophic Factor), directly promoting neurogenesis in the hippocampus.",
            trainingTip = "Supported by consistent sleep cycles (glymphatic clearance), aerobic cardio, and stress downregulation.",
            relativePositionX = 0.50f,
            relativePositionY = 0.46f
        )
    )

    val neurotransmitters = listOf(
        Neurotransmitter(
            name = "Dopamine",
            formulaSymbol = "DA",
            role = "Reward, Drive & Anticipation",
            description = "Drives motivation, prediction-error signaling, and goal-directed action. Released when anticipating high-value outcomes.",
            naturalBoosters = listOf("Completing micro-goals", "Cold water exposure", "Sunlight exposure within 1 hour of waking", "Tyrosine-rich foods"),
            color = Color(0xFFF59E0B)
        ),
        Neurotransmitter(
            name = "Acetylcholine",
            formulaSymbol = "ACh",
            role = "Attention, Focus & Plasticity",
            description = "Opens the window of neuroplasticity in the adult cortex. Spotlights attention on specific sensory and cognitive inputs.",
            naturalBoosters = listOf("Intense focused study bouts (45-90 mins)", "Choline-rich nutrition (eggs)", "Novel skill learning"),
            color = Color(0xFF06B6D4)
        ),
        Neurotransmitter(
            name = "Serotonin",
            formulaSymbol = "5-HT",
            role = "Mood, Contentment & Satiety",
            description = "Regulates mood, social rank perception, circadian cycles, and gastrointestinal signaling. High levels promote calm confidence.",
            naturalBoosters = listOf("Morning natural sunlight", "Aerobic exercise", "Tryptophan sources", "Positive social connection"),
            color = Color(0xFF10B981)
        ),
        Neurotransmitter(
            name = "GABA",
            formulaSymbol = "GABA",
            role = "Neural Inhibition & Calm",
            description = "The primary inhibitory neurotransmitter. Hyperpolarizes neurons to prevent cognitive overload, anxiety, and excitotoxicity.",
            naturalBoosters = listOf("Slow diaphragmatic breathing (physiological sigh)", "Magnesium L-threonate", "Yoga and meditation"),
            color = Color(0xFF8B5CF6)
        ),
        Neurotransmitter(
            name = "Norepinephrine",
            formulaSymbol = "NE",
            role = "Vigilance, Arousal & Energy",
            description = "Mobilizes brain and body for acute action. Heightens sensory acuity and elevates heart rate during cognitive demand.",
            naturalBoosters = listOf("High-intensity interval sprints", "Cardiovascular workouts", "Deliberate cold exposure"),
            color = Color(0xFFEF4444)
        ),
        Neurotransmitter(
            name = "Glutamate",
            formulaSymbol = "Glu",
            role = "Excitatory Synaptic Plasticity",
            description = "The dominant excitatory transmitter. Mediates Long-Term Potentiation (LTP)—the cellular basis of all learning and memory.",
            naturalBoosters = listOf("Deep intellectual learning", "Problem solving", "Balanced protein metabolism"),
            color = Color(0xFF3B82F6)
        )
    )

    val neuroMyths = listOf(
        NeuroMyth(
            myth = "We only use 10% of our brain capacity.",
            fact = "False! You use virtually 100% of your brain.",
            explanation = "Functional fMRI and PET scans demonstrate that even during deep sleep, almost all regions of the brain exhibit baseline neural firing and communication."
        ),
        NeuroMyth(
            myth = "People are strictly 'Left-Brained' or 'Right-Brained'.",
            fact = "False! Both hemispheres work in concert continuously.",
            explanation = "While language has lateralized hubs (e.g. left Broca's area), complex thought, math, and creativity activate dense bilateral networks across the corpus callosum."
        ),
        NeuroMyth(
            myth = "Adult human brains cannot produce new neurons.",
            fact = "False! Adult neurogenesis occurs in the hippocampus.",
            explanation = "Research shows thousands of new granule cells are born daily in the subgranular zone of the dentate gyrus, influenced by aerobic exercise and learning."
        ),
        NeuroMyth(
            myth = "Listening to Mozart makes infants permanently smarter.",
            fact = "False! It provides only temporary arousal.",
            explanation = "The famous 'Mozart Effect' was a short-term spatial reasoning boost lasting barely 15 minutes due to heightened dopamine arousal, not permanent structural rewiring."
        ),
        NeuroMyth(
            myth = "Brain damage can never be rehabilitated.",
            fact = "False! Neuroplasticity allows functional reorganization.",
            explanation = "Undamaged cortical regions frequently rewire synaptic connections to take over duties of damaged tissue through dedicated constraint-induced therapy."
        )
    )

    val quizQuestions = listOf(
        BrainQuizQuestion(
            id = 1,
            question = "Which brain structure is primary responsible for converting short-term memories into long-term memories?",
            options = listOf("Hippocampus", "Amygdala", "Occipital Lobe", "Cerebellum"),
            correctIndex = 0,
            explanation = "The Hippocampus (located inside the medial temporal lobe) acts as the indexing hub for consolidating declarative memories into long-term storage.",
            targetLobe = "TEMPORAL"
        ),
        BrainQuizQuestion(
            id = 2,
            question = "What is the name of the cellular process considered the biological foundation of learning and memory?",
            options = listOf("Long-Term Potentiation (LTP)", "Apoptosis", "Action Potential Gating", "Myelinolysis"),
            correctIndex = 0,
            explanation = "Long-Term Potentiation (LTP) is the persistent strengthening of synapses based on recent patterns of activity, famously phrased as 'neurons that fire together wire together'.",
            targetLobe = "PARIETAL"
        ),
        BrainQuizQuestion(
            id = 3,
            question = "Which neurotransmitter is critical for marking salient attention and opening adult cortical neuroplasticity?",
            options = listOf("Acetylcholine", "Melatonin", "Insulin", "Histamine"),
            correctIndex = 0,
            explanation = "Acetylcholine released from the nucleus basalis acts like a cognitive spotlight, alerting the cortex to rewire during concentrated focus.",
            targetLobe = "FRONTAL"
        ),
        BrainQuizQuestion(
            id = 4,
            question = "Which lobe of the brain houses Broca's area, responsible for motor speech production?",
            options = listOf("Occipital Lobe", "Frontal Lobe", "Parietal Lobe", "Temporal Lobe"),
            correctIndex = 1,
            explanation = "Broca's area resides in the inferior frontal gyrus of the dominant (usually left) frontal lobe.",
            targetLobe = "FRONTAL"
        ),
        BrainQuizQuestion(
            id = 5,
            question = "What brain system flushes out metabolic waste and amyloid-beta plaques during deep non-REM sleep?",
            options = listOf("Glymphatic System", "Endocrine System", "Somatic System", "Cardiovascular Chiasm"),
            correctIndex = 0,
            explanation = "The Glymphatic system uses cerebrospinal fluid (CSF) flow to clear metabolic waste accumulated during wakefulness, operating up to 10x faster during deep sleep.",
            targetLobe = "LIMBIC"
        ),
        BrainQuizQuestion(
            id = 6,
            question = "The primary visual cortex (V1) is located in which anatomical lobe?",
            options = listOf("Frontal Lobe", "Temporal Lobe", "Occipital Lobe", "Parietal Lobe"),
            correctIndex = 2,
            explanation = "The occipital lobe at the back of the skull contains the primary visual cortex (Brodmann area 17), receiving direct retinal signals.",
            targetLobe = "OCCIPITAL"
        ),
        BrainQuizQuestion(
            id = 7,
            question = "Which factor is most strongly stimulated by aerobic cardiovascular exercise to trigger new neuron birth?",
            options = listOf("BDNF (Brain-Derived Neurotrophic Factor)", "Cortisol", "Amylase", "Prolactin"),
            correctIndex = 0,
            explanation = "Cardiovascular exercise promotes systemic release of BDNF, which acts like fertilizer for dendritic branching and hippocampal neurogenesis.",
            targetLobe = "LIMBIC"
        ),
        BrainQuizQuestion(
            id = 8,
            question = "What is the primary inhibitory neurotransmitter in the central nervous system?",
            options = listOf("GABA", "Glutamate", "Norepinephrine", "Thyroxine"),
            correctIndex = 0,
            explanation = "GABA (gamma-aminobutyric acid) is the main inhibitory neurotransmitter, keeping neural excitation in check to prevent seizures and anxiety.",
            targetLobe = "CEREBELLUM"
        )
    )

    val flashcards = listOf(
        NeuroFlashcard(
            id = 1,
            term = "Neuroplasticity",
            category = "Core Concept",
            definition = "The brain's lifelong ability to modify, change, and adapt both structure and function in response to experience, learning, or injury.",
            keyTakeaway = "Brains are not hardwired circuits; they are dynamic networks shaped by repetition and deliberate practice."
        ),
        NeuroFlashcard(
            id = 2,
            term = "Myelination",
            category = "Neuroanatomy",
            definition = "The process of forming a lipid-rich sheath around axons by oligodendrocytes, accelerating electrical action potential conduction up to 100x.",
            keyTakeaway = "Physical and cognitive skill mastery relies directly on progressive axon myelination."
        ),
        NeuroFlashcard(
            id = 3,
            term = "Glymphatic Clearance",
            category = "Brain Health",
            definition = "A macroscopic waste clearance system using astrocytic aquaporin-4 channels to flush cerebrospinal fluid and clear neurotoxins.",
            keyTakeaway = "Operates primarily during Slow-Wave Sleep; chronic sleep deprivation disrupts this cleansing mechanism."
        ),
        NeuroFlashcard(
            id = 4,
            term = "Long-Term Potentiation (LTP)",
            category = "Cellular Biology",
            definition = "Persistent strengthening of synapses based on recent patterns of co-activity through NMDA and AMPA receptor cascades.",
            keyTakeaway = "Forms the molecular foundation of all associative memory encoding."
        ),
        NeuroFlashcard(
            id = 5,
            term = "Prefrontal Cortex (PFC)",
            category = "Brain Anatomy",
            definition = "The cerebral cortex covering the front part of the frontal lobe, responsible for decision-making, working memory, and social conduct.",
            keyTakeaway = "Matures last in human development (~25 years old), guiding complex self-regulation."
        ),
        NeuroFlashcard(
            id = 6,
            term = "BDNF",
            category = "Biochemistry",
            definition = "Brain-Derived Neurotrophic Factor: a key neurotrophin protein that promotes survival, differentiation, and synaptogenesis of neurons.",
            keyTakeaway = "Spiked dramatically by aerobic exercise, heat exposure, and fasting intervals."
        )
    )
}
