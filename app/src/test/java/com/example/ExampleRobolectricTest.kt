package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.UserDao
import com.example.data.model.UserEntity
import com.example.util.SecurityHelper
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var userDao: UserDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        userDao = db.userDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("EarnRewards", appName)
    }

    @Test
    fun `security helper hashes correctly and generates IDs`() {
        val hash1 = SecurityHelper.hashPassword("secret123")
        val hash2 = SecurityHelper.hashPassword("secret123")
        assertEquals(hash1, hash2)
        assertTrue(hash1.length >= 32)

        val txId = SecurityHelper.generateTransactionId("DEP")
        assertTrue(txId.startsWith("DEP-"))

        val formatted = SecurityHelper.formatCurrency(1500.0)
        assertTrue(formatted.contains("1,500") || formatted.contains("1500"))
    }

    @Test
    fun `dao inserts and retrieves user with balance`() = runBlocking {
        val user = UserEntity(
            name = "Test User",
            mobile = "03001234567",
            email = "test@example.com",
            passwordHash = SecurityHelper.hashPassword("pass123"),
            referralCode = "TEST01",
            balance = 500.0,
            totalEarnings = 500.0
        )
        val id = userDao.insertUser(user)
        assertTrue(id > 0)

        val retrieved = userDao.getUserByIdSuspend(id)
        assertNotNull(retrieved)
        assertEquals("Test User", retrieved?.name)
        assertEquals(500.0, retrieved?.balance ?: 0.0, 0.01)
    }
}
