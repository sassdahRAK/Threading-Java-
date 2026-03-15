><h4>To run program: </h4>
> >./gradlew run --info 2>&1 | grep -A 20 "Exception\|Error"

> <h4>To generate the data set (1000 int): Terminal</h4>
>> python3 -c "
>> import random 
>> nums = [random.randint(1, 99999) for _ in range(1000)]
>> print(','.join(map(str, nums)))
>> " > ~/Desktop/test_array.csv
> 
> <h4>To check:</h4>
> >1. wc -w ~/Desktop/test_array.csv  # should show 1
> >2. head -c 100 ~/Desktop/test_array.csv  # preview first 100 chars

> # 10,000 elements
>>python3 -c "import random; print(','.join(map(str, [random.randint(1,999999) for _ in range(10000)])))" > ~/Desktop/test_10k.csv

># 100,000 elements
>>python3 -c "import random; print(','.join(map(str, [random.randint(1,999999) for _ in range(100000)])))" > ~/Desktop/test_100k.csv

># 500,000 elements
>>python3 -c "import random; print(','.join(map(str, [random.randint(1,999999) for _ in range(500000)])))" > ~/Desktop/test_500k.csv

># 1,000,000 elements
>>python3 -c "import random; print(','.join(map(str, [random.randint(1,999999) for _ in range(1000000)])))" > ~/Desktop/test_1m.csv

># 2,000,000 elements
>>python3 -c "import random; print(','.join(map(str, [random.randint(1,999999) for _ in range(2000000)])))" > ~/Desktop/test_2m.csv

><h4>The rule is:   
> Array Size ----------------------> Winner</h4>
>>1. 100,000 ---------------->Single-thread (overhead dominates)
>
>>2. 500,000+ -------------->Multi-thread (parallel work dominates)
>
>>3. 1,000,000+ ----------->Multi-thread wins clearly

## file structure
> sovanvirak.vean/ <br>
>├── Main.java          → entry point only  <br>
>├── MainWindow.java    → UI only <br>
>├── MultiThread.java   → multithreaded sorting logic <br>
>├── SingleThread.java  → single-threaded sorting logic <br>
>└── Utils.java         → CSV read/write only <br>