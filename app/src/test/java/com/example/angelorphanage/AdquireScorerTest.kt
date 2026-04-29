package com.example.angelorphanage

import com.example.angelorphanage.domain.ScorerInstance
import com.example.angelorphanage.domain.adquire
import junit.framework.TestCase.assertTrue
import org.junit.Test

class AdquireScorerTest {
    @Test
    fun adquire_isCorrect(){
        var scorerInstance: ScorerInstance
        for (i in 0..100){
            scorerInstance = adquire()
            assertTrue(adquire() is ScorerInstance)
            assert(scorerInstance.name.isNotEmpty())
        }
    }
}