package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.solution
import me.reckter.aoc.solve

class Day11 : Day {
	override val day = 11

	val map by lazy {
		val orig = loadInput().associate {
			val (device, outputStr) = it.split(": ")
			val output = outputStr.split(" ")
			device to output
		}

		val invertedMap =
			orig.entries
				.flatMap { entry -> entry.value.map { entry.key to it } }
				.groupBy { it.second }
				.mapValues { it.value.map { it.first } }
		invertedMap
	}

	override fun solvePart1() {
		pathsFromAToB("you", "out", emptyList())
			.solution(1)
	}


	val pathCache = mutableMapOf<Pair<String, String>, Long>()
	fun pathsFromAToB(a: String, b: String, stopAt: List<String>): Long {
		if (!pathCache.containsKey(a to b)) {
			if (a == b) {
				pathCache[a to b] = 1L
			} else {
				val path = map[b]
					?.filter { it !in stopAt }
					?.mapNotNull { pathsFromAToB(a, it, stopAt) }
					?.sum() ?: 0L

				pathCache[a to b] = path
			}
		}

		return pathCache[a to b] ?: error("node $a not found")
	}

	override fun solvePart2() {
		val svrToFft = pathsFromAToB("svr", "fft", listOf("dac", "out"))
		val svrToDac = pathsFromAToB("svr", "dac", listOf("fft", "out"))

		val fftToDac = pathsFromAToB("fft", "dac", listOf("svr", "out"))
		val dacToFft = pathsFromAToB("dac", "fft", listOf("svr", "out"))

		val fftToOut = pathsFromAToB("fft", "out", listOf("svr", "dac"))
		val dacToOut = pathsFromAToB("dac", "out", listOf("svr", "fft"))

		val fftThenDac = svrToFft * fftToDac * dacToOut
		val dacThenFft = svrToDac * dacToFft * fftToOut

		(fftThenDac + dacThenFft).solution(2)

	}
}

fun main() = solve<Day11>()
