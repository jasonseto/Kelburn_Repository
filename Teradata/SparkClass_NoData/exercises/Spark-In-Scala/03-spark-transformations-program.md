![](../../images/ThinkBig_logo_ORANGE-RGB_tiny.png)
# Spark in Scala
## Spark Transformations Program
In this lab, you should type the following into your spark-shell:

```scala
val fib = Array(1, 2, 3, 5, 8, 13, 21, 34)
```

Then, using the REPL, use both named functions and anonymous functions to do the following:

* Compute all the squares
* Return those squares that are divisible by 3

You'll want to use the .map and .filter transformations on fib to invoke your functions

-

The answer is

```scala
// Compute all the squares and sum all the values provided

scala> def square(x: Int) = x * x
square: (x: Int)Int

scala> def divisible(x: Int) = (x % 3 == 0)
divisible: (x: Int)Boolean

scala> fib.map(square).filter(divisible)
res18: Array[Int] = Array(9, 441)

scala> fib.map(x => x*x).filter(_ % 3 == 0)
res19: Array[Int] = Array(9, 441)

```

