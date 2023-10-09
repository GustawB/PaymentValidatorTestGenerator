#!/bin/bash

testcount=1

g++ -Wall -Wextra -O2 -std=c++20 parking.cc -o parking
javac ParkingTestGenerator.java

for ((i=0; i<$testcount; i++)) do
	java ParkingTestGenerator
	./parking < test.in > parkingResult.out
	if diff test.out parkingResult.out > /dev/null 
		then
			echo "Test $i passed"
			rm test.in &> /dev/null
			rm test.out &> /dev/null
			rm parkingResult.out &> /dev/null
		else
			echo "Test $i FAILED"
			exit
	fi

done
