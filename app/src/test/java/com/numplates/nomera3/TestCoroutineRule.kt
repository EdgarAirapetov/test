package com.numplates.nomera3

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.createTestCoroutineScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement


/**
 * Good article about test dispatchers -
 * https://craigrussell.io/2022/01/19/comparing-standardtestdispatcher-and-unconfinedtestdispatcher
 * */
@ExperimentalCoroutinesApi
class TestCoroutineRule :  TestRule{
	private val testCoroutineDispatcher = StandardTestDispatcher()

	override fun apply(base: Statement, description: Description): Statement = object : Statement() {
		@Throws(Throwable::class)
		override fun evaluate() {
			Dispatchers.setMain(testCoroutineDispatcher)

			base.evaluate()

			Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
		}
	}
}
