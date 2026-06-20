package com.example.authority.utils;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 本地缓存工具类（无需Redis服务器，使用ConcurrentHashMap实现）
 * 兼容原RedisUtils的接口方法
 *
 * @program: design3
 * @ClassName:RedisUtils
 * @description: redis工具类（本地内存版）
 * @author:dyy
 * @Version 3.0
 **/
@Component
public class RedisUtils {

    // 数据存储
    private final Map<String, Object> dataMap = new ConcurrentHashMap<>();
    // Hash存储：外层key -> (内层field -> value)
    private final Map<String, Map<String, Object>> hashMap = new ConcurrentHashMap<>();
    // 过期时间存储（毫秒时间戳）
    private final Map<String, Long> expireMap = new ConcurrentHashMap<>();

    /**
     * 清理已过期的key
     */
    private void cleanExpired(String key) {
        Long expireTime = expireMap.get(key);
        if (expireTime != null && System.currentTimeMillis() > expireTime) {
            dataMap.remove(key);
            hashMap.remove(key);
            expireMap.remove(key);
        }
    }

    /**
     * 指定缓存失效时间
     *
     * @param key  键
     * @param time 时间(秒)
     * @return
     */
    public boolean expire(String key, long time) {
        try {
            if (time > 0) {
                expireMap.put(key, System.currentTimeMillis() + time * 1000);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据key 获取过期时间
     *
     * @param key 键 不能为null
     * @return 时间(秒) 返回0代表为永久有效
     */
    public long getExpire(String key) {
        Long expireTime = expireMap.get(key);
        if (expireTime == null) {
            return -1;
        }
        long剩余 = expireTime - System.currentTimeMillis();
        return剩余 > 0 ?剩余 / 1000 : 0;
    }

    /**
     * 判断key是否存在
     *
     * @param key 键
     * @return true 存在 false不存在
     */
    public boolean hasKey(String key) {
        try {
            cleanExpired(key);
            return dataMap.containsKey(key) || hashMap.containsKey(key);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除缓存
     *
     * @param key 可以传一个值 或多个
     */
    @SuppressWarnings("unchecked")
    public void del(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                dataMap.remove(key[0]);
                hashMap.remove(key[0]);
                expireMap.remove(key[0]);
            } else {
                for (String k : key) {
                    dataMap.remove(k);
                    hashMap.remove(k);
                    expireMap.remove(k);
                }
            }
        }
    }

    //============================String=============================

    /**
     * 普通缓存获取
     *
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        if (key == null) return null;
        cleanExpired(key);
        return dataMap.get(key);
    }

    /**
     * 普通缓存放入
     *
     * @param key   键
     * @param value 值
     * @return true成功 false失败
     */
    public boolean set(String key, Object value) {
        try {
            dataMap.put(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 普通缓存放入并设置时间
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒) time要大于0 如果time小于等于0 将设置无限期
     * @return true成功 false 失败
     */
    public boolean set(String key, Object value, long time) {
        try {
            dataMap.put(key, value);
            if (time > 0) {
                expireMap.put(key, System.currentTimeMillis() + time * 1000);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 递增
     *
     * @param key   键
     * @param delta 要增加几(大于0)
     * @return
     */
    public long incr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于0");
        }
        Object val = dataMap.get(key);
        long newVal = (val instanceof Number ? ((Number) val).longValue() : 0) + delta;
        dataMap.put(key, newVal);
        return newVal;
    }

    /**
     * 递减
     *
     * @param key   键
     * @param delta 要减少几(小于0)
     * @return
     */
    public long decr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递减因子必须大于0");
        }
        Object val = dataMap.get(key);
        long newVal = (val instanceof Number ? ((Number) val).longValue() : 0) - delta;
        dataMap.put(key, newVal);
        return newVal;
    }

    //================================Map=================================

    /**
     * HashGet
     *
     * @param key  键 不能为null
     * @param item 项 不能为null
     * @return 值
     */
    public Object hget(String key, String item) {
        cleanExpired(key);
        Map<String, Object> map = hashMap.get(key);
        return map != null ? map.get(item) : null;
    }

    /**
     * 获取hashKey对应的所有键值
     *
     * @param key 键
     * @return 对应的多个键值
     */
    public Map<Object, Object> hmget(String key) {
        cleanExpired(key);
        Map<String, Object> map = hashMap.get(key);
        if (map == null) {
            return new HashMap<>();
        }
        return new HashMap<>(map);
    }

    /**
     * HashSet
     *
     * @param key 键
     * @param map 对应多个键值
     * @return true 成功 false 失败
     */
    public boolean hmset(String key, Map<String, Object> map) {
        try {
            hashMap.computeIfAbsent(key, k -> new ConcurrentHashMap<>()).putAll(map);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * HashSet 并设置时间
     *
     * @param key  键
     * @param map  对应多个键值
     * @param time 时间(秒)
     * @return true成功 false失败
     */
    public boolean hmset(String key, Map<String, Object> map, long time) {
        try {
            hashMap.computeIfAbsent(key, k -> new ConcurrentHashMap<>()).putAll(map);
            if (time > 0) {
                expireMap.put(key, System.currentTimeMillis() + time * 1000);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     *
     * @param key   键
     * @param item  项
     * @param value 值
     * @return true 成功 false失败
     */
    public boolean hset(String key, String item, Object value) {
        try {
            hashMap.computeIfAbsent(key, k -> new ConcurrentHashMap<>()).put(item, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     *
     * @param key   键
     * @param item  项
     * @param value 值
     * @param time  时间(秒)  注意:如果已存在的hash表有时间,这里将会替换原有的时间
     * @return true 成功 false失败
     */
    public boolean hset(String key, String item, Object value, long time) {
        try {
            hashMap.computeIfAbsent(key, k -> new ConcurrentHashMap<>()).put(item, value);
            if (time > 0) {
                expireMap.put(key, System.currentTimeMillis() + time * 1000);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除hash表中的值
     *
     * @param key  键 不能为null
     * @param item 项 可以使多个 不能为null
     */
    public void hdel(String key, Object... item) {
        Map<String, Object> map = hashMap.get(key);
        if (map != null && item != null) {
            for (Object i : item) {
                map.remove(i.toString());
            }
        }
    }

    /**
     * 判断hash表中是否有该项的值
     *
     * @param key  键 不能为null
     * @param item 项 不能为null
     * @return true 存在 false不存在
     */
    public boolean hHasKey(String key, String item) {
        cleanExpired(key);
        Map<String, Object> map = hashMap.get(key);
        return map != null && map.containsKey(item);
    }

    /**
     * hash递增 如果不存在,就会创建一个 并把新增后的值返回
     *
     * @param key  键
     * @param item 项
     * @param by   要增加几(大于0)
     * @return
     */
    public double hincr(String key, String item, double by) {
        Map<String, Object> map = hashMap.computeIfAbsent(key, k -> new ConcurrentHashMap<>());
        Object val = map.get(item);
        double newVal = (val instanceof Number ? ((Number) val).doubleValue() : 0) + by;
        map.put(item, newVal);
        return newVal;
    }

    /**
     * hash递减
     *
     * @param key  键
     * @param item 项
     * @param by   要减少记(小于0)
     * @return
     */
    public double hdecr(String key, String item, double by) {
        return hincr(key, item, -by);
    }

    //============================set=============================

    /**
     * 根据key获取Set中的所有值
     *
     * @param key 键
     * @return
     */
    public Set<Object> sGet(String key) {
        try {
            cleanExpired(key);
            Object val = dataMap.get(key);
            if (val instanceof Set) {
                return (Set<Object>) val;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据value从一个set中查询,是否存在
     *
     * @param key   键
     * @param value 值
     * @return true 存在 false不存在
     */
    public boolean sHasKey(String key, Object value) {
        try {
            cleanExpired(key);
            Object val = dataMap.get(key);
            if (val instanceof Set) {
                return ((Set<Object>) val).contains(value);
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将数据放入set缓存
     *
     * @param key    键
     * @param values 值 可以是多个
     * @return 成功个数
     */
    public long sSet(String key, Object... values) {
        try {
            Set<Object> set = (Set<Object>) dataMap.computeIfAbsent(key, k -> new HashSet<>());
            long count = 0;
            for (Object v : values) {
                if (set.add(v)) count++;
            }
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 将set数据放入缓存
     *
     * @param key    键
     * @param time   时间(秒)
     * @param values 值 可以是多个
     * @return 成功个数
     */
    public long sSetAndTime(String key, long time, Object... values) {
        try {
            long count = sSet(key, values);
            if (time > 0) expire(key, time);
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 获取set缓存的长度
     *
     * @param key 键
     * @return
     */
    public long sGetSetSize(String key) {
        try {
            cleanExpired(key);
            Object val = dataMap.get(key);
            if (val instanceof Set) {
                return ((Set<?>) val).size();
            }
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 移除值为value的
     *
     * @param key    键
     * @param values 值 可以是多个
     * @return 移除的个数
     */
    public long setRemove(String key, Object... values) {
        try {
            Object val = dataMap.get(key);
            if (val instanceof Set) {
                Set<Object> set = (Set<Object>) val;
                long count = 0;
                for (Object v : values) {
                    if (set.remove(v)) count++;
                }
                return count;
            }
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    //===============================list=================================

    /**
     * 获取list缓存的内容
     *
     * @param key   键
     * @param start 开始
     * @param end   结束  0 到 -1代表所有值
     * @return
     */
    public List<Object> lGet(String key, long start, long end) {
        try {
            cleanExpired(key);
            Object val = dataMap.get(key);
            if (val instanceof List) {
                List<Object> list = (List<Object>) val;
                if (start < 0) start = 0;
                if (end < 0 || end >= list.size()) end = list.size() - 1;
                if (start > end) return new ArrayList<>();
                return new ArrayList<>(list.subList((int) start, (int) end + 1));
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取list缓存的长度
     *
     * @param key 键
     * @return
     */
    public long lGetListSize(String key) {
        try {
            cleanExpired(key);
            Object val = dataMap.get(key);
            if (val instanceof List) {
                return ((List<?>) val).size();
            }
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 通过索引 获取list中的值
     *
     * @param key   键
     * @param index 索引  index>=0时， 0 表头，1 第二个元素，依次类推；index<0时，-1，表尾，-2倒数第二个元素，依次类推
     * @return
     */
    public Object lGetIndex(String key, long index) {
        try {
            cleanExpired(key);
            Object val = dataMap.get(key);
            if (val instanceof List) {
                List<Object> list = (List<Object>) val;
                int idx = index < 0 ? list.size() + (int) index : (int) index;
                if (idx >= 0 && idx < list.size()) {
                    return list.get(idx);
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @return
     */
    public boolean lSet(String key, Object value) {
        try {
            List<Object> list = (List<Object>) dataMap.computeIfAbsent(key, k -> new ArrayList<>());
            list.add(value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     * @return
     */
    public boolean lSet(String key, Object value, long time) {
        try {
            lSet(key, value);
            if (time > 0) expire(key, time);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @return
     */
    public boolean lSet(String key, List<Object> value) {
        try {
            List<Object> list = (List<Object>) dataMap.computeIfAbsent(key, k -> new ArrayList<>());
            list.addAll(value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     * @return
     */
    public boolean lSet(String key, List<Object> value, long time) {
        try {
            lSet(key, value);
            if (time > 0) expire(key, time);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据索引修改list中的某条数据
     *
     * @param key   键
     * @param index 索引
     * @param value 值
     * @return
     */
    public boolean lUpdateIndex(String key, long index, Object value) {
        try {
            Object val = dataMap.get(key);
            if (val instanceof List) {
                List<Object> list = (List<Object>) val;
                int idx = index < 0 ? list.size() + (int) index : (int) index;
                if (idx >= 0 && idx < list.size()) {
                    list.set(idx, value);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 移除N个值为value
     *
     * @param key   键
     * @param count 移除多少个
     * @param value 值
     * @return 移除的个数
     */
    public long lRemove(String key, long count, Object value) {
        try {
            Object val = dataMap.get(key);
            if (val instanceof List) {
                List<Object> list = (List<Object>) val;
                long removed = 0;
                Iterator<Object> it = list.iterator();
                while (it.hasNext()) {
                    if (Objects.equals(it.next(), value)) {
                        it.remove();
                        removed++;
                        if (count > 0 && removed >= count) break;
                    }
                }
                return removed;
            }
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    //================有序集合 sort set===================

    /**
     * 有序set添加元素
     *
     * @param key
     * @param value
     * @param score
     * @return
     */
    public boolean zSet(String key, Object value, double score) {
        // 简化实现：使用Map存储，实际功能较简单
        try {
            Map<Object, Double> zset = (Map<Object, Double>) dataMap.computeIfAbsent(key, k -> new ConcurrentHashMap<>());
            zset.put(value, score);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public long batchZSet(String key, Set<ZSetOperations.TypedTuple> typles) {
        // 简化实现
        try {
            for (ZSetOperations.TypedTuple tuple : typles) {
                zSet(key, tuple.getValue(), tuple.getScore());
            }
            return typles.size();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void zIncrementScore(String key, Object value, long delta) {
        try {
            Map<Object, Double> zset = (Map<Object, Double>) dataMap.computeIfAbsent(key, k -> new ConcurrentHashMap<>());
            Double current = zset.get(value);
            zset.put(value, (current != null ? current : 0) + delta);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void zUnionAndStore(String key, Collection otherKeys, String destKey) {
        // 简化实现
    }

    /**
     * 获取zset数量
     *
     * @param key
     * @param value
     * @return
     */
    public long getZsetScore(String key, Object value) {
        try {
            Map<Object, Double> zset = (Map<Object, Double>) dataMap.get(key);
            if (zset != null) {
                Double score = zset.get(value);
                return score != null ? score.longValue() : 0;
            }
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 获取有序集 key 中成员 member 的排名 。
     * 其中有序集成员按 score 值递减 (从大到小) 排序。
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    public Set<ZSetOperations.TypedTuple> getZSetRank(String key, long start, long end) {
        // 简化实现
        return new HashSet<>();
    }

    // 内部类，用于ZSet操作
    public static class ZSetOperations {
        public interface TypedTuple {
            Object getValue();
            double getScore();
        }
    }

}
