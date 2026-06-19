package com.example.authority.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 订单号生成工具类
 * 生成格式：HZ + yyyyMMddHHmmss + 6位随机数
 * 例如：HZ202501151430256789012
 * 
 * @author design
 * @version 1.0
 */
public class OrderNumberGenerator {
    
    /**
     * 订单号前缀
     */
    private static final String ORDER_PREFIX = "HZ";
    
    /**
     * 时间格式化器
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    
    /**
     * 原子计数器，确保同一毫秒内的订单号唯一性
     */
    private static final AtomicLong COUNTER = new AtomicLong(0);
    
    /**
     * 随机数生成器
     */
    private static final Random RANDOM = new Random();
    
    /**
     * 生成唯一订单号
     * 
     * @return 唯一订单号
     */
    public static String generateOrderNumber() {
        // 获取当前时间戳
        String timestamp = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        
        // 获取计数器值（确保同一秒内的唯一性）
        long counter = COUNTER.incrementAndGet() % 1000000; // 限制在6位数内
        
        // 生成6位随机数
        int randomNum = RANDOM.nextInt(900000) + 100000; // 确保是6位数
        
        // 组合订单号：前缀 + 时间戳 + 计数器 + 随机数
        return String.format("%s%s%06d%d", ORDER_PREFIX, timestamp, counter, randomNum);
    }
    
    /**
     * 验证订单号格式是否正确
     * 
     * @param orderNumber 订单号
     * @return 是否符合格式
     */
    public static boolean isValidOrderNumber(String orderNumber) {
        if (orderNumber == null || orderNumber.length() != 26) {
            return false;
        }
        
        // 检查前缀
        if (!orderNumber.startsWith(ORDER_PREFIX)) {
            return false;
        }
        
        // 检查是否全部由字母和数字组成
        return orderNumber.matches("^[A-Z0-9]+$");
    }
    
    /**
     * 从订单号中提取时间信息
     * 
     * @param orderNumber 订单号
     * @return 时间字符串，格式：yyyy-MM-dd HH:mm:ss
     */
    public static String extractTimeFromOrderNumber(String orderNumber) {
        if (!isValidOrderNumber(orderNumber)) {
            return null;
        }
        
        try {
            // 提取时间戳部分（去掉前缀后的14位）
            String timestampStr = orderNumber.substring(ORDER_PREFIX.length(), ORDER_PREFIX.length() + 14);
            LocalDateTime dateTime = LocalDateTime.parse(timestampStr, DATE_TIME_FORMATTER);
            return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            return null;
        }
    }
}