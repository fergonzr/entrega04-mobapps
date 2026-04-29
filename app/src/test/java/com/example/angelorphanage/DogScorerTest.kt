package com.example.angelorphanage

import com.example.angelorphanage.domain.ScorerInstance
import com.example.angelorphanage.domain.ScorerType
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNotEquals
import org.junit.Test

class DogScorerTest {

    @Test
    fun generation_isCorrect(){
        val dog = ScorerInstance(ScorerType.DOG)
        assertNotNull(dog.name)
        assertTrue(dog.name.isNotEmpty())
    }

    @Test
    fun name_shouldBeRandomDogName(){
        val dog1 = ScorerInstance(ScorerType.DOG)
        val dog2 = ScorerInstance(ScorerType.DOG)
        
        assertNotEquals(dog1.name, dog2.name)
        assertTrue(dog1.name.isNotEmpty())
        assertTrue(dog2.name.isNotEmpty())
    }
}
