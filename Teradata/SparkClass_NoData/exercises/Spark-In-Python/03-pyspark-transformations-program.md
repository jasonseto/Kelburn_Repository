![](../../images/ThinkBig_logo_ORANGE-RGB_tiny.png)
# Spark in Scala
## Spark Transformations Program
In this lab, you should type the following into your spark-shell:

```python
fib = sc.parallelize([1, 2, 3, 5, 8, 13, 21, 34])
```

Then, using the REPL, use both named functions and anonymous functions to do the following:

* Compute all the squares
* Return those squares that are divisible by 3

You'll want to use the .map and .filter transformations on fib to invoke your functions

-

The answer is

```python
/>>> ## Compute all the squares and sum all the values provided
... fib = sc.parallelize([1, 2, 3, 5, 8, 13, 21, 34])
>>> 
>>> def square(x): return x * x
... 
>>> def divisible(x): return (x % 3 == 0)
... 
>>> ## First using named functions
... fib.map(square).filter(divisible).collect()
[9, 441]
>>> 
>>> ## Now use functional literals
... fib.map(lambda x: x*x).filter(lambda y: y % 3 == 0).collect()
[9, 441]
>>> 

```

