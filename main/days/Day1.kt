package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.parseWithRegex
import me.reckter.aoc.solution
import me.reckter.aoc.solve

class Day1 : Day {
	override val day = 1

	override fun solvePart1() {
		loadInput()
			.parseWithRegex("(R|L)(\\d+)")
			.map { (turn, distance) ->
				Pair(
					if (turn == "R") 1 else -1,
					distance.toInt()
				)
			}
			.runningFold(50) { cur, (turn, distance) ->
				(cur + turn * distance) % 100
			}
			.count { it == 0 }
			.solution(1)
	}

	fun foldToRange(input: Int, hasFolded: Boolean = false): Pair<Int, Int> {
		if (input in 0..<100) {
			return input to if (input == 0 && !hasFolded) 1 else 0
		}
		if (input < 0) {
			val next = foldToRange(input + 100, true)
			return next.first to next.second + 1
		}
		val next = foldToRange(input - 100, true)
		return next.first to next.second + 1
	}

	override fun solvePart2() {
		val instructions = loadInput()
			.parseWithRegex("(R|L)(\\d+)")
			.map { (turn, distance) ->
				Pair(
					if (turn == "R") 1 else -1,
					distance.toInt()
				)
			}

		instructions
			.map { (turn, distance) -> turn to ((distance % 100) + 100) % 100 }
			.runningFold(50 to 0) { (cur, _), (turn, distance) ->
				val next = (cur + turn * distance)

				val (realNext, foldCount) = foldToRange(next)

				val count = foldCount + (if (next < 0 && cur == 0) -1 else 0)


				(realNext to count)
			}
			.sumOf { it.second }
			.let { it + instructions.map { it.second }.filter { it > 100 }.sumOf { it / 100 } }
			.solution(2)
	}
}

fun main() = solve<Day1>()
