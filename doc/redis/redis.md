## `Redis`面试题

#### 1.为什么使用`Redis`？

> 因为传统的数据库已经不能适用于所有场景了，比如秒杀扣除，首页访问流量高峰等。都很容易导致数据库崩溃，所以引入了缓存中间件。目前比较热门的缓存中间件有`Redis`和`memcached`，综合考虑比较之后选择了`Redis`。



#### 2. `Redis` 和`memcached`的区别？

- 都将数据存放在内存中，但`memcached`还可以缓存图片视频等其他数据；
- `memcached`只支持简单的key-value，`redis`还支持list/set/set/hash等数据结构；
- 当`redis`的内存满了，会利用虚拟内存将一些过期的key交换到磁盘中；
- `memcached`的过期时间在set的时候就设定，`redis`还可以使用expire设定
- `memcached`的数据只保存在内存中机器故障不可恢复，`redis`数据会定期flush到磁盘，可通过rdb和aof恢复
- `memcached`不支持复制，`redis`支持master-slave的主从复制
- `memcached`的读写速度高于`redis`，因为`memcached`是多线程，`redis`是单线程



#### 3.Redis的数据结构

> 常用的数据结构有：`String`，`List`，`Set`，`ZSet (sorted set)`，`Hash`
>
> 中级：`HyperLogLog`，`Geo`
>
> 高级：`Reids Module`，`Bloom Filter`，`Redis Search`，`Redis-ML`
>
> [布隆过滤器(Bloom Filter)](./bloom_filter.md)



#### 4. Redis的数据淘汰策略

> redis检查内存使用情况，如果已使用的内存大于max memory的值，则根据用户选择的内存淘汰策略来淘汰key。淘汰策略如下：

- `no-eviction`:：当内存达到阈值的时候，所有申请内存的命令会报错。（禁止驱逐数据）
- `allkeys-lru` ：从数据集 `server.db[i].dict` 中挑选最近最少使用的数据淘汰
- `allkeys-random`：从数据集中随机挑选数据淘汰
- `volatile-lru`：从已设置过期的数据集 `server.db[i].expires` 中挑选最近最少使用的数据淘汰
- `volatile-random`：从已设置过期的数据集中任意挑选数据淘汰
- `volatile-ttl`：从已设置过期的数据集中挑选将要过期的数据淘汰



#### 5. Redis集群方案什么情况下会导致集群不可用

> 主要在于是否有从节点。
>
> 假如集群中有三个节点A B C，如果B节点没有从节点，且B节点故障，会导致B节点分管的范围的槽不可用。



#### 6. Hash槽？

> Redis 集群 `redis cluster` 没有使用一致性hash，而是使用hash槽的概念。Redis集群有16385个hash槽，每个key通过CRC16校验对16384取模来决定防止的槽，集群的每个节点负责一部分hash槽。



#### 7. Redis集群会有写操作丢失吗？

> Redis 满足可用性(A)和分区容错性(P)，不能满足强一致性，有可能会发生写操作丢失的情况。redis是单线程，每个请求都是事务执行的，但是多个请求之间不能保证强一致性。

> C：consistency 强一致性：更新操作成功并返回客户端完成后，所有节点在同一时间的数据完全一致，所以，一致性，说的就是数据一致性
>
> A：availability 可用性：服务一直可用，而且是正常响应时间
>
> P：partition tolerance 分区容错性：分布式系统在遇到某节点或网络分区故障的时候，仍然能够对外提供满足一致性和可用性的服务。
>
> [分布式系统CAP理论](http://www.hollischuang.com/archives/666)

![CAP](http://ww1.sinaimg.cn/large/006fJlVugy1gc1g23egumj30m80m8djd.jpg)



#### 8. Redis集群之间是如何复制的？

> 异步复制，复制过程中 master/slaver 节点都是非阻塞的。在从节点同步数据时，采用的是旧数据集的数据。

[Redis--主从复制](https://blog.csdn.net/zhengzhaoyang122/article/details/99695747)

1. 当从节点连接到主节点时，会向主节点发送`psync`命令，附带参数`runID：主节点ID`和`offset数据位置偏移`
2. 如果主节点回复`FULLRESYNC`，那么触发全量复制
3. 如果主节点回复`CONTINUE`，那么触发部分复制
4. 如果主节点回复`ERR`，表示主节点不支持此命令

**主从全量复制：**复制过程中，主节点使用`bgsave`命令生成 RDB 文件并发送给从节点

**主从部分复制：**主节点与从节点断开连接，主节点只需要将缓冲区的部分数据同步到从节点就能够保证数据的一致性。主节点会将接收到的request数据写入“复制积压缓冲区”，默认1M，可通过`repl-backlog-size`配置



#### 9. Redis中的管道（pipeline）有什么作用？

> 将多个读写请求合并在一起提交给客户端出来，一并返回结果
>
> 优势是减少了单个请求在网络中耗费的时间，每个请求都需要通过网络发送并通过网络返回结果，管道就将这些没有联系的请求打包通过网络发送一次，并通过网络返回一个结果（这个结果包含多个请求的结果）



#### 10. Redis中的事务

[REDIS事务处理](http://www.redis.cn/topics/transactions.html)

[Redis事务介绍](https://blog.csdn.net/hechurui/article/details/49508749)

> 事务是一组命令的集合。需要有两个保证:
>
> 1. 隔离操作：事务队列中的命令都会被序列化并顺序执行，不会被别的操作影响。
> 2. 原子操作：事务队列中的命令要么全部被执行，要么都不执行。



> redis事务相关的命令：`MULTI`,`EXEC`,`WATCH`,`DISCARD`。此外，**使用脚本也可以实现事务**
>
> ```
> # 开启事务
> MULTI
> 
> # 事务队列 命令集合
> set a a
> set b b
> set c c
> 
> # 提交事务
> EXEC
> 
> # 放弃事务，清除事务队列中的内容，客户端退出事务状态
> DISCARD
> 
> # 监控一个或多个key，如果key在事务期间改变，则不执行事务（实现乐观锁）
> WATCH
> 
> # 解除监控
> UNWATCH
> ```

##### 10.1 如果在执行`EXEC`命令前，客户端故障，redis怎么处理？

> redis客户端会清空事务队列，事务中的所有命令都不会执行；但如果是执行了`EXEC`命令后，即使redis客户端故障，依然会执行事务，因为事务已经被保存在服务器中。

##### 10.2 redis事务错误处理

> 1. 语法错误：命令集合中存在语法错误的命令。
>
>    Redis 2.6.5之前版本会忽略错误的命令，执行事务中其他正常的命令。
>
>    Reids 2.6.5之后版本会忽略这个事务中的所有命令，都不执行。
>
> 2. 运行错误：命令执行过程中出错，比如GET一个Hash类型的key。
>
>    如果事务里有一天命令执行错误，其他命令依旧会被执行。

##### 10.3 为什么redis不支持rollback

> redis命令只会因为运行错误（语法错误在入队时就会被发现）而失败。从实用性的角度考虑，这些问题应该是在开发过程中被发现，而不是在生产环境中解决。
>
> 不支持rollback也使得redis的内部可以保持简单且快速。



#### 11. 为什么要做分区？分区方案有哪些？集群Cluster
> 分区可以让 redis 管理更大的内存，redis 将使用所有机器的内存。分区使redis的计算能力得到成倍提升，带宽也会随着计算机和网卡的增加而成倍增长。
>
> 1. 客户端分区：在客户端就已经决定数据会被存储到哪个节点或从哪个节点读取。大多数客户端已经实现了客户端分区。
> 2. 代理分区：客户端将请求发给代理，然后代理决定去哪个节点写数据/读数据。代理根据分区规则决定请求的节点，再根据此节点响应结果返回给客户端。
> 3. 查询路由：客户端随机请求任意一个节点，然后由redis将请求转发给正确的节点。

#### 12. Redis中的sentinel（哨兵）模式

> Sentinel：是redis的高可用实现方案。sentinel是一个可以管理多个redis实例的工具，它可以实现对redis的**监控，通知，自动故障转移 **。

>  **监控： **sentinel 会不断地检查 (`PING`) 着master节点和slaver节点的状态

> **通知： **

> **自动故障转移： **master节点故障时，sentinel开始一次自动的故障转移操作，在slaver中选择一个成为新的master，并更新其他的slaver节点的master。原master节点会被标记成slaver（更新配置文件`redis.conf`）

![哨兵模式](http://ww1.sinaimg.cn/large/006fJlVugy1gc8p6ux0dhj30da0fa74e.jpg)

#### 13. Redis 线程模型

> redis基于Reactor开发了网络事件处理器，也就是文件事件处理器。它由4各部分组成：
>
> - 多个socket
> - I/O多路复用程序
> - 文件事件分派器（队列，单线程）
> - 事件处理器（连接应答处理器，命令请求处理器，命令回复处理器）

![redis线程模型](http://ww1.sinaimg.cn/large/006fJlVugy1gc8qt3tgwtj30xc0djt94.jpg)

[redis线程模型](https://blog.csdn.net/zhengzhaoyang122/article/details/100848363)

**通信过程 **

![通信过程](http://ww1.sinaimg.cn/large/006fJlVugy1gc8qwfjmqyj30mv0bm412.jpg)



#### 13. Redis分布式锁

[分布式锁用Redis还是Zookeeper](https://zhuanlan.zhihu.com/p/73807097)

##### 13.1 `setnx + expire`命令

> `setnx lock unique_value`  // set if not exists
>
> `expire lock 10`  // 设置超时时间
>
> 缺点：`setn`x和`expire` 之间非原子性，有可能在`setnx`之后机器故障从而死锁

##### 13.2 Lua脚本 / `set lock unique_value NX PX 30000`

> 将上面的两步在一步执行，避免命令非原子性执行
>
> 缺点：在高可用、集群环境中，如果master down机，slave接管可能会导致**<u>锁丢失</u>**

##### 13.3 RedLock

> 在过半数节点创建锁

##### 13.4 Redission

> 企业级开源redis-client，所有命令都是Lua脚本原子性执行
>
> WatchDog可以解决长时间阻塞问题：它会在没到期前重新设置过期时间，如果业务执行完成或者节点down机就会释放锁