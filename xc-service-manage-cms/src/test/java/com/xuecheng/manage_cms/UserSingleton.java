package com.xuecheng.manage_cms;

/**
 * @author YHQ
 * @date 2019/12/24
 * 单例模式：一个对象在内存中只有唯一个实例。
 * 它有两个主要的特点：构造函数私有，它的唯一实例必须由自身创建。
 * 如果我们要使用单例对象，不能通过new去创建，要通过该对象的getInstance()方法获取这个唯一的实例。
 *
 * 多例模式：一个对象有多个实例，通过new创建。
 */
public class UserSingleton {
    /** 私有化构造器 */
    private UserSingleton() {
    }
    /** 对外提供公共的访问方法 */
    public static UserSingleton getInstance() {
        return UserSingletonHolder.INSTANCE;
    }
    /** 写一个静态内部类，里面实例化外部类 */
    private static class UserSingletonHolder {
        private static final UserSingleton INSTANCE = new UserSingleton();
    }

}
