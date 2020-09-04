package com.baomidou.samples.transaction.manager;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.samples.transaction.entity.C;
import com.baomidou.samples.transaction.service.ServiceC;
import com.baomidou.samples.transaction.entity.A;
import com.baomidou.samples.transaction.entity.B;
import com.baomidou.samples.transaction.service.ServiceA;
import com.baomidou.samples.transaction.service.ServiceB;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Hccake 2020/9/4
 * @version 1.0
 */
@DS("master")
@RequiredArgsConstructor
@Component
public class TestManager {
    private final ServiceA serviceA;
    private final ServiceB serviceB;
    private final ServiceC serviceC;

    @Transactional(rollbackFor = Exception.class)
    public boolean insertAllDb(boolean throwError) {

        A a = new A();
        a.setName("testA");
        serviceA.save(a);

        B b = new B();
        b.setName("testB");
        serviceB.save(b);

        C c = new C();
        c.setName("testC");
        serviceC.save(c);

        if (throwError) {
            throw new RuntimeException();
        }
        return true;
    }


    public Map<String, List<?>> selectAllDb() {
        Map<String, List<?>> map = new HashMap<>();
        List<A> aList = serviceA.list();
        List<B> bList = serviceB.list();
        List<C> cList = serviceC.list();

        map.put("a", aList);
        map.put("b", bList);
        map.put("c", cList);

        return map;
    }

    @Transactional(rollbackFor = Exception.class)
    public void truncateAll() {
        serviceA.remove(null);
        serviceB.remove(null);
        serviceC.remove(null);
    }
}
