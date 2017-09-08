// Compute all the squares and sum all the values provided

val fib = Array(1, 2, 3, 5, 8, 13, 21, 34)

def square(x: Int) = x * x
def divisible(x: Int) = (x % 3 == 0)

// First using named functions

fib.map(square).filter(divisible)

// Now use functional literals

fib.map(x => x*x).filter(_ % 3 == 0)



