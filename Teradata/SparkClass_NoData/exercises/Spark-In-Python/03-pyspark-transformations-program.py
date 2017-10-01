## Compute all the squares and sum all the values provided
fib = sc.parallelize([1, 2, 3, 5, 8, 13, 21, 34])

def square(x): return x * x

def divisible(x): return (x % 3 == 0)

## First using named functions
fib.map(square).filter(divisible).collect()

## Now use functional literals
fib.map(lambda x: x*x).filter(lambda y: y % 3 == 0).collect()



