本地启动redis,执行：
```shell script
set spKey:10101 500
del userKey:10101
```
启动项目，使用ab并发测试：（启动cmd窗口目录下新建123.txt,内容是spId=10101，代笔入参数据）
```shell script
ab -n 1000 -c 100 -p 123.txt -T application/x-www-form-urlencoded -k -r http://localhost:9090/doseckill
ab -n 1000 -c 100 -p 123.txt -T application/x-www-form-urlencoded -k -r http://localhost:9090/doseckill_lua
```
查看效果：redis执行
```shell script
get spKey:10101
scard userKey:10101
```
spKey:10101为0代表库存卖光了，userKey:10101为500代表有500个用户抢购到商品，set里存着500个用户id。


doseckill方法使用redis乐观锁，会有剩余商品问题，解决超卖问题。
doseckill_lua使用lua脚本，解决超卖和剩余商品问题。原理是利用redis的单线程，一次性执行一段Lua脚本，就只能一下把命令挨个执行完，才轮到下一个Lua脚本执行，所以不会有任何问题，相当于任务队列的模型。



RedisLockController和RedisConfig是springboot-redis实现分布式锁，小demo，分布式条件下，将某个值自增（ab工具模拟加1000次）。
