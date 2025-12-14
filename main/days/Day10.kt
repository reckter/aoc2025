package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.allCombinations
import me.reckter.aoc.allEnumerations
import me.reckter.aoc.flipDimensions
import me.reckter.aoc.parseWithRegex
import me.reckter.aoc.print
import me.reckter.aoc.solution
import me.reckter.aoc.solve
import me.reckter.aoc.toIntegers
import kotlin.collections.fold
import kotlin.math.min
import kotlin.streams.asStream

class Day10 : Day {
	override val day = 10

	data class Machine(
		val goal: List<Boolean>,
		val buttons: List<List<Int>>,
		val joults: List<Int>
	)

	val machines by lazy {
		loadInput()
			.parseWithRegex("\\[(.*)] (.*?) \\{(.*)}")
			.map { (goalStr, buttonStr, joultStr) ->
				Machine(
					goal = goalStr.toCharArray().map { it == '#' },
					buttons = Regex("(\\(.*?)\\)").findAll(buttonStr)
						.map { it.groupValues.first().replace("(", "").replace(")", "").split(",").toIntegers() }
						.toList(),
					joults = joultStr.split(',').toIntegers()
				)
			}
	}


	fun List<Boolean>.pressButton1(toggles: List<Int>): List<Boolean> {
		return this.mapIndexed { index, it -> if (index in toggles) !it else it }
	}

	fun findFewestPresses1(machine: Machine): List<Int> {
		val queue = ArrayDeque<Pair<List<Int>, List<Boolean>>>()
		queue.add(emptyList<Int>() to machine.goal.map { false })

		while (queue.isNotEmpty()) {
			val (path, state) = queue.removeFirst()

			machine.buttons
				.forEachIndexed { index, button ->
					val nextState = state.pressButton1(button)
					if (nextState.zip(machine.goal).all { it.first == it.second }) {
						return path + index
					}

					queue.add(path + index to nextState)
				}
		}
		error("Could not find path for machine $machine")
	}

	fun List<Int>.pressButton2(toggles: List<Int>, times: Int): List<Int> {
		return this.mapIndexed { index, it -> it + if (index in toggles) times else 0 }
	}

	fun allButtonPresses(presses: Int, availableButtons: Int): Sequence<List<Int>> {
		if (availableButtons == 1) return sequenceOf(listOf(presses))

		return (presses downTo 0)
			.asSequence()
			.flatMap { firstPress ->
				val other = allButtonPresses(presses - firstPress, availableButtons - 1)

				other.map { listOf(firstPress) + it }
			}
	}

	fun findFewestPresses2(machine: Machine): Int {
		println("checking machine $machine")
		val queue = ArrayDeque<Pair<List<Int>, List<Int>>>()
		queue.add(emptyList<Int>() to machine.goal.map { 0 })

		return generateSequence(1) { it + 1 }
			.flatMap { times ->
				allButtonPresses(times, machine.buttons.size)
					.map { times to it }
			}
			.first { (count, presses) ->
				println("  checking $count: $presses")
				val state = presses.zip(machine.buttons)
					.fold(machine.joults.map { 0 }) { state, (times, button) ->
						state.pressButton2(button, times)
					}

				state.zip(machine.joults).all { it.first == it.second }
			}
			.first
	}

	val cache = mutableMapOf<Pair<List<Int>, List<List<Int>>>, Int?>()

	fun findMinPressSuffix(joults: List<Int>, buttonsStillNeeded: List<List<Int>>): Int? {
		val cacheKey = joults to buttonsStillNeeded
		if (cache.containsKey(cacheKey)) {
			return cache[cacheKey]
		}


		if (joults.all { it == 0 }) return 0
		if (buttonsStillNeeded.isEmpty()) return null

//		println("$joults: $buttonsStillNeeded")

		val nextButton = buttonsStillNeeded.first()
		val button = (0 until joults.size)
			.map { idx -> if (idx in nextButton) 1 else 0 }

		val max = button.zip(joults)
			.minOf { if (it.first == 0) Int.MAX_VALUE else it.second }

		if (max == Int.MAX_VALUE) {
			val ret = findMinPressSuffix(joults, buttonsStillNeeded.drop(1))
			cache[cacheKey] = ret
			return ret
		}

		var count = max
		var currentBest: Int? = null
		while (count >= 0) {
			val stillNeeded = joults.zip(button)
				.map { it.first - (it.second * count) }

			val cur = findMinPressSuffix(stillNeeded, buttonsStillNeeded.drop(1))
				?.let { it + count }

			if (cur != null) {
				if (currentBest == null || cur < currentBest)
					currentBest = cur
			}
			count--
		}
		cache[cacheKey] = currentBest
		return currentBest
	}

	fun greedyButtonPresses(machine: Machine, pressesSoFar: List<Int> = emptyList(), best: Int): Int? {
		val pressedValues =
			pressesSoFar.zip(machine.buttons)
				.fold(machine.joults.map { 0 }) { cur, acc -> cur.pressButton2(acc.second, acc.first) }

		if (pressedValues.zip(machine.joults).all { it.first == it.second }) {
			return pressesSoFar.sum()
		}
		if (pressesSoFar.size == machine.buttons.size) {
			return null
		}

		val stillNeeded = machine.joults.zip(pressedValues)
			.map { it.first - it.second }

		val nextButton = machine.buttons[pressesSoFar.size]
		val button = (0 until machine.joults.size)
			.map { idx -> if (idx in nextButton) 1 else 0 }

		val max = button.zip(stillNeeded)
			.minOf { if (it.first == 0) Int.MAX_VALUE else it.second }

		if (max == Int.MAX_VALUE) {
			return greedyButtonPresses(machine, pressesSoFar + 0, best)
		}

		val start = min(max, best - pressesSoFar.sum())

		var currentBest = best

		var it = start
		while (it >= 0) {
			val cur = greedyButtonPresses(machine, pressesSoFar + it, currentBest)
			if (cur != null && cur < currentBest)
				currentBest = cur
			it--
		}
		return currentBest
	}

	fun List<Int>.pressButtonParity(toggles: List<Int>): List<Int> {
		return this.mapIndexed { index, it -> it - if (index in toggles) 1 else 0 }
	}

	fun findFewestPressesForAllEven(
		joults: List<Int>,
		buttons: List<List<Int>>
	): Sequence<Pair<List<List<Int>>, List<Int>>> {
		return buttons.allCombinations()
			.map {
				it to it.fold(joults) { acc, cur -> acc.pressButtonParity(cur) }
			}
			.filter { it.second.all { it % 2 == 0 } }
			.filter { it.second.all { it >= 0 } }
	}

	val parityCache = mutableMapOf<Pair<List<Int>, List<List<Int>>>, Int?>()
	fun paritySolve(joults: List<Int>, buttons: List<List<Int>>): Int? {

		val cacheKey = joults to buttons
		if (parityCache.containsKey(cacheKey)) {
			return parityCache[cacheKey]
		}

		val combinationsToParity = findFewestPressesForAllEven(joults, buttons)
			.toList()

		val ret = combinationsToParity
			.mapNotNull { (buttonsToParity, left) ->
				if (left.all { it == 0 }) {
					buttonsToParity.size
				} else {
					paritySolve(left.map { it / 2 }, buttons)
						?.let { it * 2 + buttonsToParity.size }
				}
			}
			.minOrNull()
		parityCache[cacheKey] = ret
		return ret
	}


	// note basis ist list of 1 or 0, and EACH has the same size as goal
	fun solveBasisSwitch(goal: List<Int>, basis: List<List<Int>>): List<Int> {
		// goal = sum(N * button)
		// => triangalize

		val sortedBasis = basis
			.mapIndexed { index, basis -> index to basis }
			.sortedByDescending { it.second.count { it != 0 } }

		var equations = goal.zip(
			sortedBasis.map { it.second }.flipDimensions()
		)

		(0 until min(equations.size, basis.size))
			.map { round ->
				// used for elimination
				val curBase = equations.drop(round)
					.find { it.second[round] != 0 }

				if (curBase != null) {
					equations = equations.take(round) + curBase + equations
						.minus(curBase)
						.drop(round)

					// base is now at index round
					equations = equations
						.mapIndexed { index, it ->
							if (index < round) it
							else if (index == round) {
								if (it.second[index] == -1) {
									-it.first to it.second.map { -it }
								} else it
							} else if (it.second[round] == 0) it
							else {
								val baseFact = it.second[round]
								val rowFact = curBase.second[round]

								(it.first * rowFact - curBase.first * baseFact) to
									it.second.zip(curBase.second)
										.map { (it, base) -> it * rowFact - base * baseFact }
							}

						}
				}
			}

		var offset = 0

		val nonFreeVariables =
			equations.map { it.second.indexOfFirst { it != 0 } }
		val freeVariables = equations.first().second.indices.filter { it !in nonFreeVariables }

		val testUntil = goal.max()
		val testCases = freeVariables
			.map { idx -> (0..testUntil).map { idx to it } }
			.let { if (it.size > 0) it else listOf(listOf(-1 to 0)) }
			.allEnumerations()

		println("$testUntil: $freeVariables")

		return testCases
			.asStream()
			.parallel()
			.map { freeVariables ->
				val solvedVariables = freeVariables
					// filter out empty marker
					.filter { it.first != -1 }
					.toMutableList()
				while (solvedVariables.size < basis.size && solvedVariables.all { it.second >= 0 }) {
					val next = equations
						.find {
							it.second
								.mapIndexed { index, value ->
									value != 0 && !solvedVariables.any { it.first == index }
								}
								.count { it } == 1
						}
					if (next == null)
						error("No next variable to define found!")

					val nextVariable = next.second
						.mapIndexed { index, value -> index to value }
						.find { (index, value) ->
							value != 0 && !solvedVariables.any { it.first == index }
						}
						?: error("no variable")

					val other = next.second
						.mapIndexed { index, cur ->
							val variable = solvedVariables.find { it.first == index }
							if (variable == null) 0
							else variable.second * cur
						}
						.sum()

					val data = next.first - other
					if (data % nextVariable.second != 0) {
						// we have a reminder devision, which is bad
						// abort, no natural number solution
						return@map null
					}
					val value = data / nextVariable.second
					solvedVariables.add(nextVariable.first to value)
				}
				solvedVariables
			}
			.filter { it != null }
			.filter { it?.all { it.second >= 0 }!! }
			.toList()
			.filterNotNull()
			.minByOrNull { it.sumOf { it.second } }
			?.sortedBy { sortedBasis[it.first].first }
			.print("res")
			?.map { it.second }
			?.also {
				val result = basis.zip(it)
					.map { (list, amount) -> list.map { it * amount } }
					.reduce { acc, cur -> acc.zip(cur).map { (list, amount) -> list + amount } }

				if (goal.zip(result).any { it.first != it.second }) {
					error("Non matching result! $result\n$goal")
				}
			}
			?: error("no solution!")
//
//		return equations
//			.map { it.first to it.second.reversed() }
//			.filter { it.second.any { it != 0 } }
//			.reversed()
//			.fold(emptyList<Int>()) { solution, (goal, equation) ->
//				val other = solution.zip(equation)
//					.sumOf { it.first * it.second }
//
//				val currentFactor = equation.drop(solution.size).firstOrNull()
//					?: error("list null")
//
//				if (currentFactor == 0) {
//					solution + 0
//				} else
//					solution + ((goal - other) / currentFactor)
//			}
//			.reversed()
//
	}

	override fun solvePart1() {
		machines
			.sumOf { findFewestPresses1(it).size }
			.solution(1)
	}

	override fun solvePart2() {
		machines
			.mapNotNull {
				val width = it.joults.size
				val basis = it.buttons
					.map { (0 until width).map { idx -> if (idx in it) 1 else 0 } }
//				val basisSwitch = solveBasisSwitch(it.joults, basis)
				val parity = paritySolve(it.joults, it.buttons)

//				if(basisSwitch.sum() != parity) {
//					println("incorrect basisSwitch: ${basisSwitch} for $it (expected $parity sum)")
//				}

				parity
			}
			.sum()
//			.sumOf { it.sum() }
			.solution(2)

	}
}

fun main() = solve<Day10>()
