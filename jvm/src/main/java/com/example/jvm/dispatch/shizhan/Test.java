package com.example.jvm.dispatch.shizhan;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;

import static java.lang.invoke.MethodHandles.lookup;

/**
 * 分派原则实战
 */
public class Test {


    class GrandFather {
        void thinking() {
            System.out.println("i am grandfather");
        }
    }

    class Father extends GrandFather {
        void thinking() {
            System.out.println("i am father");
        }
    }

    class Son extends Father {
        void thinking() {
        // 请读者在这里填入适当的代码（不能修改其他地方的代码）
        // 实现调用祖父类的thinking()方法，打印"i am grandfather"
            try {
//                MethodType mt = MethodType.methodType(void.class); MethodHandle mh = lookup().findSpecial(GrandFather.class,
//                        "thinking", mt, getClass());
//                mh.invoke(this);

                MethodType mt = MethodType.methodType(void.class);
                Field lookupImpl = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP"); lookupImpl.setAccessible(true);
                MethodHandle mh = ((MethodHandles.Lookup) lookupImpl.get(null)).findSpecial(GrandFather.class, "thinking",mt, getClass());
                mh.invoke(this);
            } catch (Throwable e) {
            }
        }


    }
    public static void main(String[] args) {
        (new Test().new Son()).thinking();
    }
}
