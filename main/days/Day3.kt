package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.solution
import me.reckter.aoc.solve
import me.reckter.aoc.toIntegers

class Day3 : Day {
    override val day = 3

    override fun solvePart1() {
        loadInput()
	        .map { it.split("").filter { it != ""}.toIntegers() }
	        .map {
				val firstDigit = it.dropLast(1).max()
		        val secondDigit  = it.dropWhile { it != firstDigit }
			        .drop(1)
			        .max()

		        firstDigit * 10 + secondDigit
			}
	        .sum()
	        .solution(1)
    }

	fun getBiggestDigit(digits: List<Int>,): Pair<Int, List<Int>> {
		val digit = digits.max()
		val rest = digits.dropWhile { it != digit }
			.drop(1)

		return digit to rest
	}

	fun getBiggestNumber(digits: List<Int>): Long {
		var tail = digits.takeLast(11)
		var list = digits.dropLast(11)
		return (0..11)
			.map {
				var (digit, nextList) = getBiggestDigit(list)
				if(tail.isNotEmpty())
					list = nextList + tail.first()
				tail = tail.drop(1)

				digit
			}
			.joinToString("")
			.toLong()
	}

    override fun solvePart2() {

	    loadInput()
		    .map { it.split("").filter { it != ""}.toIntegers() }
		    .map { getBiggestNumber(it) }
		    .sum()
		    .solution(2)

    }
}

fun main() = solve<Day3>()
